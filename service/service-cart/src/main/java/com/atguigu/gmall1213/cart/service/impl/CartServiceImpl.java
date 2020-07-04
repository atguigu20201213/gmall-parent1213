package com.atguigu.gmall1213.cart.service.impl;

import com.atguigu.gmall1213.cart.mapper.CartInfoMapper;
import com.atguigu.gmall1213.cart.service.CartAsyncService;
import com.atguigu.gmall1213.cart.service.CartService;
import com.atguigu.gmall1213.common.constant.RedisConst;
import com.atguigu.gmall1213.model.cart.CartInfo;
import com.atguigu.gmall1213.model.product.SkuInfo;
import com.atguigu.gmall1213.product.client.ProductFeignClient;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class CartServiceImpl implements CartService {


    @Autowired
    private CartInfoMapper cartInfoMapper;
    @Autowired
    private ProductFeignClient productFeignClient;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private CartAsyncService cartAsyncService;
    /*
      1.  判断当前要添加的商品，在购物车中是否存在！
          1.1  存在：
                  则商品的数量相加 {更新}
          1.2  不存在：
                  则直接添加到购物车 {插入}
       2. 无论你是存在，还是不存在，我们都需要更新缓存
       */
    @Override
    public void addToCart(Long skuId, String userId, Integer skuNum) {
        // 数据要想放入缓存，必须使用redis,同样应该有key=user:userId:cart
        // 获取购物车的key
        String cartKey = getCartKey(userId);
            //添加购物车之判断   service 优化
        if (!redisTemplate.hasKey(cartKey)) {

            loadCartCache(userId);
        }

        // redis 中使用什么数据类型来存储购物车？ hash 使用 hset(key,field,value)  key=user:userId:cart field=skuId value 商品数据
        // 查询谁的购物车，买个哪个商品！
        // select * from cart_info where userId = ? and skuId = ?

        CartInfo cartInfoExist = cartInfoMapper.selectOne(new QueryWrapper<CartInfo>().eq("user_id", userId).eq("sku_id",skuId));
        if (null != cartInfoExist) {
            // 购物车中有当前商品，更新数据
            cartInfoExist.setSkuNum(cartInfoExist.getSkuNum()+skuNum);
            // 因为在表中skuPrice 不存在初始化一个skuPrice
            // cartInfoExist.setSkuPrice(cartInfoExist.getCartPrice());
            // skuPrice = skuInfo.price
            cartInfoExist.setSkuPrice(productFeignClient.getSkuPrice(skuId));
            // 更新
            // cartInfoMapper.updateById(cartInfoExist);
            cartAsyncService.updateCartInfo(cartInfoExist);
            // 放入缓存
            // redisTemplate.boundHashOps(cartKey).put(skuId.toString(),cartInfoExist);

        } else {
            // 没有商品，第一次添加
            // 购物车中的数据，都是来自于商品详情，商品详情的数据是来自于servce-product.
            SkuInfo skuInfo = productFeignClient.getSkuInfoById(skuId);
            // 声明一个cartInfo 对象
            CartInfo cartInfo = new CartInfo();
            cartInfo.setSkuPrice(skuInfo.getPrice());
            cartInfo.setCartPrice(skuInfo.getPrice());

            cartInfo.setSkuNum(skuNum);
            cartInfo.setSkuName(skuInfo.getSkuName());

            cartInfo.setImgUrl(skuInfo.getSkuDefaultImg());
            cartInfo.setSkuId(skuId);
            cartInfo.setUserId(userId);

            // 新增数据
            // cartInfoMapper.insert(cartInfo);
            cartAsyncService.saveCartInfo(cartInfo);
            // 如果代码走到了这，说明cartInfoExist 是空。cartInfoExist 可能会被GC吃了。废物再利用
            cartInfoExist=cartInfo;
            // 放入缓存
            // redisTemplate.boundHashOps(cartKey).put(skuId.toString(),cartInfo);
        }
        //
//        redisTemplate.opsForHash().put(key, field, Value);
//        redisTemplate.opsForHash().putAll(key, Map);   map.put(feild,value)
//        redisTemplate.boundHashOps(Key).put(field,Value);
        //在缓存存储数据
        redisTemplate.boundHashOps(cartKey).put(skuId.toString(), cartInfoExist);
        //给缓存设置过期时间
        setCartKeyExpire(cartKey);
    }

    @Override
    public List<CartInfo> getCartList(String userId, String userTempId) {
        // 什么一个返回的集合对象
        List<CartInfo> cartInfoList = new ArrayList<>();

        // 未登录：临时用户Id 获取未登录的购物车数据
        if (StringUtils.isEmpty(userId)) {
            cartInfoList = getCartList(userTempId);
           // return cartInfoList;

        }

        //已登录  可能存在合并购物车  合并结果也是购物车的集合
            //合并之前 ，必须先知道购物车中是否有数据
        if (!StringUtils.isEmpty(userId)) {
            List<CartInfo> cartInfoNoLoginList = getCartList(userTempId);
            //未登录 说明购物车中有数据
            if (!CollectionUtils.isEmpty(cartInfoNoLoginList)) {
                //开始合并购物车数据
                cartInfoList=  mergeToCartList(cartInfoNoLoginList, userId);
                //合并之后还需要删除未登录购物车
                deleteCartList(userTempId);

            }
            //如果未登录数据库是空，直接返回登陆的购物车集合
            if (CollectionUtils.isEmpty(cartInfoNoLoginList)||StringUtils.isEmpty(userTempId)) {

                cartInfoList = getCartList(userId);
            }
        }
        return cartInfoList;
    }

    @Override
    public void checkCart(String userId, Integer isChecked, Long skuId) {
        //调用异步对象更新数据
        cartAsyncService.checkCart(userId, isChecked, skuId);
        //更新缓存
        String cartKey = getCartKey(userId);
        BoundHashOperations boundHashOperations = redisTemplate.boundHashOps(cartKey);
        //有商品skuID
        if (boundHashOperations.hasKey(skuId.toString())) {
            //根据skuId 获取对应的cartInfo
            CartInfo cartInfo = (CartInfo) boundHashOperations.get(skuId.toString());
            //对应修改选中状态
            cartInfo.setIsChecked(isChecked);
            //修改完成之后将修改好的cartInfo放入缓存

            boundHashOperations.put(skuId.toString(),cartInfo);

            //修改一下过期时间
            setCartKeyExpire(cartKey);

        }
    }

    @Override
    public void deleteCart(Long skuId, String userId) {
        //删除数据库
        cartAsyncService.deleteCartInfo(userId, skuId);
        //删除缓存  先获取缓存的key
        String cartKey = getCartKey(userId);
        BoundHashOperations boundHashOperations = redisTemplate.boundHashOps(cartKey);
        //判断商品skuid 在缓存中是否存在
        if (boundHashOperations.hasKey(skuId.toString())) {
            //如果匹配上，则删除
 			boundHashOperations.delete(skuId.toString());
        }
    }

    @Override
    public List<CartInfo> getCartCheckedList(String userId) {
       List<CartInfo> cartInfos = new ArrayList<>();

        //查询购物车列表是因为，送货清单是从购物车来的
        String cartKey = getCartKey(userId);
        //表示获取cartKey  下 所有的值  所有的数据
        List<CartInfo> cartInfoList = redisTemplate.opsForHash().values(cartKey);
        //判断里面是否有数据
        if (!CollectionUtils.isEmpty(cartInfoList)) {
            //遍历循环
            for (CartInfo cartInfo : cartInfoList) {
                //购物车中选中的商品才是送货清单
                if (cartInfo.getIsChecked().intValue() == 1) {
                    //提取数据
                    cartInfos.add(cartInfo);
                }
            }
        }
        return cartInfos;
    }

    //删除未登录购物车数据
    private void deleteCartList(String userTempId) {
        // 删除缓存 ， 一个是删除数据库


       // cartInfoMapper.delete(new QueryWrapper<CartInfo>().eq("user_id", userTempId));
        // 异步删除
        cartAsyncService.deleteCartInfo(userTempId);

         String cartKey = getCartKey(userTempId);
        //先判断 是否有这个key
        Boolean flag = redisTemplate.hasKey(cartKey);
        if (flag) {
            redisTemplate.delete(cartKey);
        }
    }


    /**
     * //合并购物车方法
     * @param cartInfoNoLoginList  未登录数据库数据
     * @param userId
     * @return
     */
    private List<CartInfo> mergeToCartList(List<CartInfo> cartInfoNoLoginList, String userId) {
        //先获取登录的购物车数据  根据userId
        List<CartInfo> cartListLogin = getCartList(userId);
        //登录购物车数据分为两种状态，一个是有循环遍历合并数据  ，一个是没有数据  ，直接插入数据库
        // 将登录的集合数据转换为map key=skuid ,value =cartInfo
        Map<Long, CartInfo> cartInfoMap = cartListLogin.stream().collect(Collectors.toMap(CartInfo::getSkuId, cartInfo -> cartInfo));
        //循环判断
        for (CartInfo cartInfoNoLogin : cartInfoNoLoginList) {
            //判断未登录的skuid
            Long skuId = cartInfoNoLogin.getSkuId();
            //判断登录的map 集合中的key = skuid   是否包含未登录购物车的 skuid
            if (cartInfoMap.containsKey(skuId)) {
                //登录和未登录有相同的商品，将数量相加，相加之后给登录
                CartInfo cartInfoLogin = cartInfoMap.get(skuId);
                cartInfoLogin.setSkuNum(cartInfoLogin.getSkuNum() + cartInfoNoLogin.getSkuNum());

                //添加细节问题   合并的时候， 只处理未登录状态下选中的商品
                if (cartInfoNoLogin.getIsChecked().intValue() == 1) {
                    //将购物车的商品也变为选中
                    cartInfoLogin.setIsChecked(1);
                }

                //合并时至少要更新数据库测操作
                cartAsyncService.updateCartInfo(cartInfoLogin);

            } else {
                //未登录数据在登录中没有或者不存在
                //未登录有临时用户id  此时将没有登录的用户id   变为登录的用户id
                cartInfoNoLogin.setUserId(userId);

//                cartInfoMapper.insert(cartInfoNoLogin);
                cartAsyncService.saveCartInfo(cartInfoNoLogin);

            }

        }
        //最终合并结果
        List<CartInfo> cartInfoList = loadCartCache(userId);

        return cartInfoList;

    }

    // 获取购物车列表
    private List<CartInfo> getCartList(String userId) {
        List<CartInfo> cartInfoList = new ArrayList<>();
        // 先看缓存，再看数据库并将数据放入缓存。
        if (StringUtils.isEmpty(userId)) {
            return null;
        }
        String cartKey = getCartKey(userId);
        // 获取缓存中的数据
        cartInfoList = redisTemplate.opsForHash().values(cartKey);
        // 判断集合中的数据是否存在
        if (!CollectionUtils.isEmpty(cartInfoList)){
            // cartInfoList 说明缓存中有数据 数据展示的时候，应该是有规则排序的，那么这个规则应该是什么？ 更新时间
            // 按照id进行排序
            cartInfoList.sort(new Comparator<CartInfo>() {
                // Comparator 比较器 - 自定义 内名内部类
                @Override
                public int compare(CartInfo o1, CartInfo o2) {
                    return o1.getId().compareTo(o2.getId());
                }
            });

            return cartInfoList;
        }else {
            // 说明缓存中没用数据
            cartInfoList= loadCartCache(userId);
            // 返回数据
            return cartInfoList;
        }
    }

    // 根据用户Id 查询数据库并将数据放入缓存。
    public List<CartInfo> loadCartCache(String userId) {
        // select * from cartInfo where user_id = userId;
        // 数据库中的数据
        List<CartInfo> cartInfoList = cartInfoMapper.selectList(new QueryWrapper<CartInfo>().eq("user_id", userId));
        // 数据库中一定会有这样的数据么？
        if (CollectionUtils.isEmpty(cartInfoList)){
            return cartInfoList;
        }
        // 如果不为空，那么将数据放入缓存
        // 声明一个map 集合来存储数据
        HashMap<String, CartInfo> map = new HashMap<>();
        // 获取到缓存key
        String cartKey = getCartKey(userId);
        // 循环遍历集合将map 中填入数据
        for (CartInfo cartInfo : cartInfoList) {
            // 之所以查询数据库是因为缓存中没用数据！是不是有可能会发生价格变动。
            cartInfo.setSkuPrice(productFeignClient.getSkuPrice(cartInfo.getSkuId()));
            // map.put(field,value);
            map.put(cartInfo.getSkuId().toString(),cartInfo);
        }
        //  redisTemplate.opsForHash().putAll(key,map); map.put(field,value);
        redisTemplate.opsForHash().putAll(cartKey,map);
        // 设置过期时间
        setCartKeyExpire(cartKey);
        return cartInfoList;
    }




    //设置过期时间
    private void setCartKeyExpire(String cartKey) {
        redisTemplate.expire(cartKey, RedisConst.USER_CART_EXPIRE, TimeUnit.SECONDS);
    }

    private String getCartKey(String userId) {
        //使用redis  必须有 key
        return RedisConst.USER_KEY_PREFIX + userId + RedisConst.USER_CART_KEY_SUFFIX;

    }
}
