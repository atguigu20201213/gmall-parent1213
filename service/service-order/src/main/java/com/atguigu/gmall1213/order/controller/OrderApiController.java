package com.atguigu.gmall1213.order.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.nacos.client.naming.utils.CollectionUtils;
import com.atguigu.gmall1213.cart.client.CartFeignClient;
import com.atguigu.gmall1213.common.result.Result;
import com.atguigu.gmall1213.common.util.AuthContextHolder;
import com.atguigu.gmall1213.model.cart.CartInfo;
import com.atguigu.gmall1213.model.order.OrderDetail;
import com.atguigu.gmall1213.model.order.OrderInfo;
import com.atguigu.gmall1213.model.user.UserAddress;
import com.atguigu.gmall1213.order.service.OrderService;
import com.atguigu.gmall1213.product.client.ProductFeignClient;
import com.atguigu.gmall1213.user.client.UserFeignClient;

import io.lettuce.core.ConnectionFuture;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;

@RestController
@RequestMapping("api/order")
public class OrderApiController {
    @Autowired
    private UserFeignClient userFeignClient;

    @Autowired
    private CartFeignClient cartFeignClient;

    @Autowired
    private OrderService orderService;
    @Autowired
    private ProductFeignClient productFeignClient;
    @Autowired
    private ThreadPoolExecutor threadPoolExecutor;

    /**
     * 确认订单 在网关中做的拦截 需要登录才能访问
     *
     * @param request
     * @return
     */
    @GetMapping("auth/trade")
    public Result<Map<String, Object>> trade(HttpServletRequest request) {
        // 登录之后获取的用户ID
        String userId = AuthContextHolder.getUserId(request);
        // 获取用户地址列表，根据用户Id
        List<UserAddress> userAddressList = userFeignClient.findUserAddressListByUserId(userId);
        // 获取送货清单
        List<CartInfo> cartCheckedList = cartFeignClient.getCartCheckedList(userId);
        // 声明一个orderDetail 集合
        List<OrderDetail> orderDetailList = new ArrayList<>();
        int totalNum = 0;
        // 循环遍历 将数据赋值给orderDetail
        if (!CollectionUtils.isEmpty(cartCheckedList)) {
            // 循环遍历
            for (CartInfo cartInfo : cartCheckedList) {
                // 将cartInfo 赋值给 orderDetail
                OrderDetail orderDetail = new OrderDetail();
                orderDetail.setImgUrl(cartInfo.getImgUrl());
                orderDetail.setOrderPrice(cartInfo.getSkuPrice());
                orderDetail.setSkuName(cartInfo.getSkuName());
                orderDetail.setSkuNum(cartInfo.getSkuNum());
                orderDetail.setSkuId(cartInfo.getSkuId());

                // 计算每个商品的总个数
                totalNum += cartInfo.getSkuNum();
                // 将每一个orderDetail 添加到集合中
                orderDetailList.add(orderDetail);
            }
        }

        // 声明一个map 集合 来处理数据

        Map<String, Object> map = new HashMap<>();
        // 存储订单明细
        map.put("detailArrayList", orderDetailList);
        // 存储收货地址
        map.put("userAddressList", userAddressList);

        // 存储总金额
        OrderInfo orderInfo = new OrderInfo();
        orderInfo.setOrderDetailList(orderDetailList);

        // 计算总金额
        orderInfo.sumTotalAmount();
        map.put("totalAmount", orderInfo.getTotalAmount());
        // 存储商品的件数,计算大的商品有多少
        map.put("totalNum", orderDetailList.size());
        // 获取流水号
        String tradeNo = orderService.getTradeNo(userId);
        // 保存 readeNo
        map.put("tradeNo", tradeNo);

        // 计算小件数量
        //        map.put("totalNum", totalNum);

        return Result.ok(map);
    }

    // 提交订单
    @PostMapping("auth/submitOrder")
    public Result submitOrder(@RequestBody OrderInfo orderInfo, HttpServletRequest request) {
        List<String> errorList = new ArrayList<>();
        // 用户id
        String userId = AuthContextHolder.getUserId(request);
        orderInfo.setUserId(Long.parseLong(userId));

        // Long orderId = orderService.saveOrderInfo(orderInfo);
        // 返回Id

        String tradeNo = request.getParameter("tradeNo");
        // 开始比较
        boolean flag = orderService.checkTradeNo(tradeNo, userId);
        if (!flag) {

            return Result.fail().message("不能回退无刷新重复提交数据");
        }


        // 使用异步编排来执行
        // 声明一个集合来储存异步编排对象
        ArrayList<CompletableFuture> futureList = new ArrayList<>();
        // 验证库存：
        List<OrderDetail> orderDetailList = orderInfo.getOrderDetailList();
        if (!CollectionUtils.isEmpty(orderDetailList)) {

            for (OrderDetail orderDetail : orderDetailList) {
                // 开启一个异步编排
                CompletableFuture<Void> checkStockCompletableFuture =
                        CompletableFuture.runAsync(
                                () -> {
                                    boolean result =
                                            orderService.checkStock(orderDetail.getSkuId(), orderDetail.getSkuNum());
                                    if (!result) {
                                        //       return Result.fail().message(orderDetail.getSkuName() + "库存不足！");

                                        errorList.add(orderDetail.getSkuName() + "库存不足");
                                    }
                                },
                                threadPoolExecutor);
                futureList.add(checkStockCompletableFuture);

                // 利用另一个异步编排
                CompletableFuture<Void> skuPriceCompletableFuture =
                        CompletableFuture.runAsync(
                                () -> {
                                    BigDecimal skuPrice = productFeignClient.getSkuPrice(orderDetail.getSkuId());
                                    if (orderDetail.getOrderPrice().compareTo(skuPrice) != 0) {
                                        cartFeignClient.loadCartCache(userId);
                                        errorList.add(orderDetail.getSkuName() + "价格有变动重新下单");
                                    }
                                },
                                threadPoolExecutor);
                futureList.add(skuPriceCompletableFuture);
            }
        }
        // 合并线程
        CompletableFuture.allOf(futureList.toArray(new CompletableFuture[futureList.size()])).join();

        if (errorList.size() > 0) {
            // 获取异常集合的数据
            return Result.fail().message(StringUtils.join(errorList, ","));
        }


        // 删除流水号
        orderService.deleteTradeNo(userId);
        Long orderId = orderService.saveOrderInfo(orderInfo);

        return Result.ok(orderId);
    }

    /**
     * 内部调用获取订单
     *
     * @param orderId
     * @return
     */
    @GetMapping("inner/getOrderInfo/{orderId}")
    public OrderInfo getOrderInfo(@PathVariable(value = "orderId") Long orderId) {
        return orderService.getOrderInfo(orderId);
    }

    @RequestMapping("orderSplit")
    public String orderSplit(HttpServletRequest request) {
        String orderId = request.getParameter("orderId");
        //仓库编号和商品的对应关系
        String wareSkuMap = request.getParameter("wareSkuMap");

        //参考接口文档
        List<OrderInfo> subOrderInfoList = orderService.orderSplit(Long.parseLong(orderId), wareSkuMap);

        //声明储存map 集合
        ArrayList<Map> mapList = new ArrayList<>();
//子订单集合 中的部分数据
        for (OrderInfo orderInfo : subOrderInfoList) {
            Map map = orderService.initWareOrder(orderInfo);
            mapList.add(map);
        }

        //返回子订单中的json 字符串
        return JSON.toJSONString(mapList);
    }

    /**
     * 秒杀提交订单，秒杀订单不需要做前置判断，直接下单
     * @param orderInfo
     * @return
     */
    //将前台获取到的json 字符串变为java 对象
    @PostMapping("inner/seckill/submitOrder")
    public Long submitOrder(@RequestBody OrderInfo orderInfo) {
        //调用保存订单方法
        Long orderId = orderService.saveOrderInfo(orderInfo);
        //返回订单id
        return orderId;
    }

}
