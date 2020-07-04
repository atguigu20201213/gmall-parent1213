package com.atguigu.gmall1213.order.service;

import com.atguigu.gmall1213.model.enums.ProcessStatus;
import com.atguigu.gmall1213.model.order.OrderInfo;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Map;

public interface OrderService extends IService<OrderInfo> {

    /**
     * 保存订单
     *
     * @param orderInfo
     * @return
     */
    Long saveOrderInfo(OrderInfo orderInfo);

    // 获取流水号  将流水号放入缓存
    //目的 是用userId  在缓存中相等玉 key
    String getTradeNo(String userId);

    // 比较流水号  传入用户id
    boolean checkTradeNo(String tradeNo, String userId);

    // 获取流水号
    void deleteTradeNo(String userId);

    // 验证库存
    boolean checkStock(Long skuId, Integer skuNum);

    //关闭过期订单
    void execExpiredOrder(Long orderId);

    /**
     * 根据订单Id 查询订单信息
     *
     * @param orderId
     * @return
     */
    OrderInfo getOrderInfo(Long orderId);

    //  更新订单的方法
    void updateOrderStatus(Long orderId, ProcessStatus paid);

    //发送消息通知库存，减库存
    void sendOrderStatus(Long orderId);

    //将orderInfo 转化为map 集合
    Map initWareOrder(OrderInfo orderInfo);

    /**
     * 拆单方法
     *
     * @param
     * @param wareSkuMap
     * @return
     */
    List<OrderInfo> orderSplit(long orderId, String wareSkuMap);

    //关闭过期订单方法
    void execExpiredOrder(Long orderId, String flag);
}
