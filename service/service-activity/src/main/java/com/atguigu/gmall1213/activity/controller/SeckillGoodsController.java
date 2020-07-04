package com.atguigu.gmall1213.activity.controller;

import com.atguigu.gmall1213.activity.service.SeckillGoodsService;
import com.atguigu.gmall1213.activity.util.CacheHelper;
import com.atguigu.gmall1213.activity.util.DateUtil;
import com.atguigu.gmall1213.common.constant.MqConst;
import com.atguigu.gmall1213.common.constant.RedisConst;
import com.atguigu.gmall1213.common.result.Result;
import com.atguigu.gmall1213.common.result.ResultCodeEnum;
import com.atguigu.gmall1213.common.service.RabbitService;
import com.atguigu.gmall1213.common.util.AuthContextHolder;
import com.atguigu.gmall1213.common.util.MD5;
import com.atguigu.gmall1213.model.activity.OrderRecode;
import com.atguigu.gmall1213.model.activity.SeckillGoods;
import com.atguigu.gmall1213.model.activity.UserRecode;
import com.atguigu.gmall1213.model.order.OrderDetail;
import com.atguigu.gmall1213.model.order.OrderInfo;
import com.atguigu.gmall1213.model.user.UserAddress;
import com.atguigu.gmall1213.order.client.OrderFeignClient;
import com.atguigu.gmall1213.product.client.ProductFeignClient;
import com.atguigu.gmall1213.user.client.UserFeignClient;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.*;


@RestController
@RequestMapping("/api/activity/seckill")
public class SeckillGoodsController {

    @Autowired
    private SeckillGoodsService seckillGoodsService;

    @Autowired
    private UserFeignClient userFeignClient;
    @Autowired
    private RabbitService rabbitService;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private OrderFeignClient orderFeignClient;

    @Autowired
    private ProductFeignClient productFeignClient;

    /**
     * 返回全部列表
     * 查询所有秒杀数据
     *
     * @return
     */
    @GetMapping("/findAll")
    public Result findAll() {
        return Result.ok(seckillGoodsService.findAll());
    }

    /**
     * 获取实体
     *
     * @param skuId
     * @return
     */
    @GetMapping("/getSeckillGoods/{skuId}")
    public Result getSeckillGoods(@PathVariable("skuId") Long skuId) {
        return Result.ok(seckillGoodsService.getSeckillGoodsBySkuId(skuId));
    }


    /**
     * 获取下单码
     *
     * @param skuId
     * @return
     */
    @GetMapping("auth/getSeckillSkuIdStr/{skuId}")
    public Result getSeckillSkuIdStr(@PathVariable("skuId") Long skuId, HttpServletRequest request) {
        //怎么样生成下单码 ，使用id  做MD5加密 加密之后这个字符串就是下单码
        String userId = AuthContextHolder.getUserId(request);
        //通过 当前商品id  查询到当前秒杀商品对象，看商品是否参与秒杀
        SeckillGoods seckillGoods = seckillGoodsService.getSeckillGoodsBySkuId(skuId);
        if (null != seckillGoods) {
            //判断当前商品是否参与秒杀 ，可以通过时间
            Date curTime = new Date();
            if (DateUtil.dateCompare(seckillGoods.getStartTime(), curTime) && DateUtil.dateCompare(curTime, seckillGoods.getEndTime())) {
                //可以动态生成，放在redis缓存
                //可以生成下单码
                if (StringUtils.isNoneEmpty(userId)) {
                    String encrypt = MD5.encrypt(userId);
                    return Result.ok(encrypt);
                }

            }
        }
        return Result.fail().message("获取下单码失败");
    }

    /* 根据用户和商品ID实现秒杀下单
     *
     * @param skuId
     * @return
     */
    @SneakyThrows
    @PostMapping("auth/seckillOrder/{skuId}")
    public Result seckillOrder(@PathVariable("skuId") Long skuId, HttpServletRequest request) {
        //获取下单码
        String skuIdStr = request.getParameter("skuIdStr");
        //判断下单码是否正确！   null  0  1
        String userId = AuthContextHolder.getUserId(request);
        if (!skuIdStr.equals(MD5.encrypt(userId))) {
            //下单码没有验证通过
            return Result.build(null, ResultCodeEnum.SECKILL_ILLEGAL);
        }
        //验证状态位 获取秒杀商品所对应的状态位
        //
        String state
                = (String) CacheHelper.get(skuId.toString());
        if (StringUtils.isEmpty(state)) {
            //请求不合法
            return Result.build(null, ResultCodeEnum.SECKILL_ILLEGAL);
        }
        // 表示能够下单
        if ("1".equals(state)) {
            //记录用户
            UserRecode userRecode = new UserRecode();
            userRecode.setUserId(userId);
            userRecode.setSkuId(skuId);

            //将消息发送消息队列
            rabbitService.sendMessage(MqConst.EXCHANGE_DIRECT_SECKILL_USER, MqConst.ROUTING_SECKILL_USER, userRecode);
        } else {
            //请求不合法  0  表示没有商品了
            return Result.build(null, ResultCodeEnum.SECKILL_ILLEGAL);

        }

        return Result.ok();

    }

    /**
     * 查询秒杀状态
     *
     * @return
     */
    @GetMapping("auth/checkOrder/{skuId}")
    public Result checkOrder(@PathVariable("skuId") Long skuId, HttpServletRequest request) {
        //当前登录用户   获取用户id
        String userId = AuthContextHolder.getUserId(request);
        //调用服务层检查订单方法
        return seckillGoodsService.checkOrder(skuId, userId);
    }

    /**
     * 秒杀确认订单
     *
     * @param request
     * @return
     */
    @GetMapping("auth/trade")
    public Result trade(HttpServletRequest request) {
        // 获取到用户收货地址列表
        String userId = AuthContextHolder.getUserId(request);
        //调用USERFEIGNcLIENt
        List<UserAddress> userAddressList = userFeignClient.findUserAddressListByUserId(userId);

        //显示收货清单  本质就是秒杀的商品
        // 先得到用户想要购买的商品！
        OrderRecode orderRecode = (OrderRecode) redisTemplate.boundHashOps(RedisConst.SECKILL_ORDERS).get(userId);

        //判断
        if (null == orderRecode) {
            return Result.fail().message("非法操作！下单失败");
        }
        //获取秒杀的商品
        SeckillGoods seckillGoods = orderRecode.getSeckillGoods();
        //要给数据复制I



        // 声明一个集合来存储订单明细
        ArrayList<OrderDetail> orderDetailList = new ArrayList<>();
        OrderDetail orderDetail = new OrderDetail();
        orderDetail.setSkuId(seckillGoods.getSkuId());
        orderDetail.setSkuName(seckillGoods.getSkuName());
        orderDetail.setImgUrl(seckillGoods.getSkuDefaultImg());
        orderDetail.setSkuNum(orderRecode.getNum());
        orderDetail.setOrderPrice(seckillGoods.getCostPrice());
        // 添加到集合
        orderDetailList.add(orderDetail);

        //订单总金额
        // 计算总金额
        OrderInfo orderInfo = new OrderInfo();
        orderInfo.setOrderDetailList(orderDetailList);
        orderInfo.sumTotalAmount();

        // 订单页面需要存储这些可以
        Map<String, Object> map = new HashMap<>();
        //住址
        map.put("userAddressList", userAddressList);
        //订单明细
        map.put("detailArrayList", orderDetailList);
        // 保存总金额
        map.put("totalAmount", orderInfo.getTotalAmount());
        return Result.ok(map);


    }
    /**
     * 秒杀提交订单
     *
     * @param orderInfo
     * @return
     */
    @PostMapping("auth/submitOrder")
    public Result submitOrder(@RequestBody OrderInfo orderInfo, HttpServletRequest request) {
        //获取用户名 id
        String userId = AuthContextHolder.getUserId(request);

        //数据都在缓存中
        OrderRecode orderRecode = (OrderRecode) redisTemplate.boundHashOps(RedisConst.SECKILL_ORDERS).get(userId);
        if (null == orderRecode) {
            return Result.fail().message("非法操作");
        }

        orderInfo.setUserId(Long.parseLong(userId));

        Long orderId = orderFeignClient.submitOrder(orderInfo);
        if (null == orderId) {
            return Result.fail().message("下单失败，请重新操作");
        }

        //删除下单信息
        redisTemplate.boundHashOps(RedisConst.SECKILL_ORDERS).delete(userId);
        //下单记录
        redisTemplate.boundHashOps(RedisConst.SECKILL_ORDERS_USERS).put(userId, orderId.toString());

        return Result.ok(orderId);
    }


}


























