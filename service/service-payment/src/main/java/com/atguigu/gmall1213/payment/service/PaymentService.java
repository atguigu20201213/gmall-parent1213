package com.atguigu.gmall1213.payment.service;

import com.atguigu.gmall1213.model.order.OrderInfo;
import com.atguigu.gmall1213.model.payment.PaymentInfo;

import java.util.Map;

public interface PaymentService {
    /**
     * 保存交易记录  数据来源应该是 orderInfo
          * @param orderInfo
     * @param paymentType 支付类型（1：微信 2：支付宝）
     */
     void savePaymentInfo(String paymentType, OrderInfo orderInfo);
    //获取交易记录信息
    PaymentInfo getPaymentInfo(String outTradeNo, String name);
    //支付成功更改交易记录
    void paySuccess(String outTradeNo, String name, Map<String, String> paramMap);
    //更新交易信息
    void updatePaymentInfo(String outTradeNo, PaymentInfo paymentInfo);

    //关闭支付宝交易
    void closePayment(Long orderId);
}
