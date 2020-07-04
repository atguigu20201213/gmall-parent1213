package com.atguigu.gmall1213.activity.service.impl;

import com.atguigu.gmall1213.activity.mapper.SeckillGoodsMapper;
import com.atguigu.gmall1213.activity.service.SeckillGoodsService;
import com.atguigu.gmall1213.activity.util.CacheHelper;
import com.atguigu.gmall1213.common.constant.RedisConst;
import com.atguigu.gmall1213.common.result.Result;
import com.atguigu.gmall1213.common.result.ResultCodeEnum;
import com.atguigu.gmall1213.common.util.MD5;
import com.atguigu.gmall1213.model.activity.OrderRecode;
import com.atguigu.gmall1213.model.activity.SeckillGoods;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 服务实现层
 *
 * @author Administrator
 */
@Service

public class SeckillGoodsServiceImpl implements SeckillGoodsService {
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private SeckillGoodsMapper seckillGoodsMapper;

    /**
     * 查询全部  //不查数据库  因为秒杀商品会将数据加载到缓存，所以查询缓存就行
     */
    @Override
    public List<SeckillGoods> findAll() {
        //商品保存再缓存 redis -hash
        List<SeckillGoods> seckillGoodsList = redisTemplate.boundHashOps(RedisConst.SECKILL_GOODS).values();
        return seckillGoodsList;
    }


    /**
     * 根据ID获取实体
     *
     * @param id
     * @return
     */
    @Override
    public SeckillGoods getSeckillGoodsBySkuId(Long id) {
        //根据skuId　　查询秒杀对象信息　　ｒｅｄｉｓ　－　ｈａｓｈ　
        return (SeckillGoods) redisTemplate.boundHashOps(RedisConst.SECKILL_GOODS).get(id.toString());
    }

    @Override
    public void seckillOrder(Long skuId, String userId) {
        //判断状态位
        String state = (String) CacheHelper.get(skuId.toString());
        //说明商品已经售罄
        if ("0".equals(state)) {
            return;
        }
        //判断用户是否已经下单  如何防止重复下单
        //如果用户下单成功，我们会将用户下单信息放入缓存
        Boolean isExist = redisTemplate.opsForValue().setIfAbsent(RedisConst.SECKILL_USER + userId, skuId, RedisConst.SECKILL__TIMEOUT, TimeUnit.SECONDS);
        //继续判断   用户存在  第二次下单
        if (!isExist) {
            return;
        }
//        isExist = true   表示用户在缓存中没有存在  说明第一次下单
        //查看当前缓存 商品是否有剩余库存
        //存储商品库存的时候使用  redis leftPush
        String goodId = (String) redisTemplate.boundListOps(RedisConst.SECKILL_STOCK_PREFIX + skuId).rightPop();
        //如果goodid 为空   没有库存
        if (StringUtils.isEmpty(goodId)) {
//            通知其他更新状态位
            redisTemplate.convertAndSend("seckillpush", skuId + ":0");
            //没有库存 了// 卖没了 ，商品售罄

            return;
        }
        //如果不为空  有库存  将信息记录起来  orderRecode  当前订单信息
        OrderRecode orderRecode = new OrderRecode();
        //在下单的时候  ，如何控制每个用户只能购买一件商品，我们将商品数量写死
        orderRecode.setNum(1);
        orderRecode.setUserId(userId);
        //订单码的字符串    setOrderStr  自定义
        orderRecode.setOrderStr(MD5.encrypt(userId + skuId));
        //根据当前skuid  ,来获取
        orderRecode.setSeckillGoods(getSeckillGoodsBySkuId(skuId));

        //将预下单的数据放入缓存
        redisTemplate.boundHashOps(RedisConst.SECKILL_ORDERS).put(orderRecode.getUserId(), orderRecode);

        //更新库存
        this.updateStockCount(orderRecode.getSeckillGoods().getSkuId());
//        this.updateStockCount(skuId);

    }

    @Override
    public Result checkOrder(Long skuId, String userId) {
        //判断用户在缓存中 是否存在，
        Boolean isExist = redisTemplate.hasKey(RedisConst.SECKILL_USER + userId);
        //如果返回true
        if (isExist) {
            //判断用户是否下单
            //         redisTemplate.boundHashOps(RedisConst.SECKILL_ORDERS).put(orderRecode.getUserId(), orderRecode);
            Boolean flag = redisTemplate.boundHashOps(RedisConst.SECKILL_ORDERS).hasKey(userId);
            if (flag) {
                //说明抢单成功！
                OrderRecode orderRecode = (OrderRecode) redisTemplate.boundHashOps(RedisConst.SECKILL_ORDERS).get(userId);
                //返回对应的code码
                return Result.build(orderRecode, ResultCodeEnum.SECKILL_SUCCESS);
            }
        }
        //判断是否下单
        Boolean res = redisTemplate.boundHashOps(RedisConst.SECKILL_ORDERS_USERS).hasKey(userId);
        //说明下单成功
        if (res) {
            //获取下单成功的数据
            String orderId = (String) redisTemplate.boundHashOps(RedisConst.SECKILL_ORDERS_USERS).get(userId);

            //表示下单成功
            return Result.build(orderId, ResultCodeEnum.SECKILL_ORDER_SUCCESS);
        }
        //判断我们的 商品对应的状态位
        String state = (String) CacheHelper.get(skuId.toString());
        // 0 说明 售罄
        if ("0".equals(state)) {
            return Result.build(null, ResultCodeEnum.SECKILL_FAIL);
        }
        //给一个默认值
        return  Result.build(null, ResultCodeEnum.SECKILL_RUN);

    }

    //表示更新库存
    private void updateStockCount(Long skuId) {
        //库存储存在dedis - list 还有数据库中的一份
        //dedis - list  库存     不需要更新
        //数据库中    需要更新    数据库更新需要根据缓存中的数据
        Long count = redisTemplate.boundListOps(RedisConst.SECKILL_STOCK_PREFIX + skuId).size();
        //为了避免频繁更新数据库   是二的倍数时， 更新一次数据库
        if (count % 2 ==0) {
            //更新数据库  以缓存为基准
            SeckillGoods seckillGood = getSeckillGoodsBySkuId(skuId);
            seckillGood.setStockCount(count.intValue());
            seckillGoodsMapper.updateById(seckillGood);
            //缓存中秒杀商品对象的库存数据,需要更新的
            redisTemplate.boundHashOps(RedisConst.SECKILL_ORDERS).put(seckillGood.getSkuId().toString(), seckillGood);

        }

    }

}
