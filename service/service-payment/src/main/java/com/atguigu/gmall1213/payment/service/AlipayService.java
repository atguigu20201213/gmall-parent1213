package com.atguigu.gmall1213.payment.service;

import com.alipay.api.AlipayApiException;

public interface AlipayService {

    String aliPay(Long orderId) throws AlipayApiException;

 //退款功能
    boolean refund(Long orderId);
    /***
     * 关闭交易
     * @param orderId
     * @return
     */
    Boolean closePay(Long orderId);
// * 根据订单查询是否支付成功！
    Boolean checkPayment(Long orderId);

}
