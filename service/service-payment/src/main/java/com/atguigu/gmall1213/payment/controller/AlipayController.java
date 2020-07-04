package com.atguigu.gmall1213.payment.controller;

import com.alipay.api.AlipayApiException;
import com.alipay.api.internal.util.AlipaySignature;
import com.atguigu.gmall1213.common.result.Result;
import com.atguigu.gmall1213.model.enums.PaymentStatus;
import com.atguigu.gmall1213.model.enums.PaymentType;
import com.atguigu.gmall1213.model.payment.PaymentInfo;
import com.atguigu.gmall1213.payment.config.AlipayConfig;
import com.atguigu.gmall1213.payment.service.AlipayService;

import com.atguigu.gmall1213.payment.service.PaymentService;
import org.apache.tomcat.util.modeler.ParameterInfo;
import org.bouncycastle.jcajce.provider.symmetric.IDEA;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.Map;


@Controller
@RequestMapping("/api/payment/alipay")
public class AlipayController {

    @Autowired
    private AlipayService alipayService;
    @Autowired
    private PaymentService paymentService;
    @RequestMapping("submit/{orderId}")
    @ResponseBody
    public String submitAlipay(@PathVariable(value = "orderId") Long orderId) {
        String from = "";
        try {
            from = alipayService.aliPay(orderId);
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        return from;
    }

    @RequestMapping("callback/return")
    public String callbackReturn() {
        //重新定向展示订单页面
        return "redirect:" + AlipayConfig.return_order_url;


    }

    //异步回调如何处理  参考官网
    @RequestMapping("callback/notify")
    @ResponseBody
    public String callBackNotify(@RequestParam Map<String, String> paramMap) {
        System.out.println("已来人了  开始接客了");
        //获取交易状态
        String trade_status = paramMap.get("trade_status");
        String out_trade_no = paramMap.get("out_trade_no");
//        String app_id = paramMap.get("app_id");
//        String total_amount = paramMap.get("total_amount");
        boolean signVerified = false;
        try {
                //调用sdk验证签名
            signVerified = AlipaySignature.rsaCheckV1(paramMap, AlipayConfig.alipay_public_key, AlipayConfig.charset, AlipayConfig.sign_type);
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        if(signVerified){
            // TODO 验签成功后，按照支付结果异步通知中的描述，对支付结果中的业务内容进行二次校验，校验成功后在response中返回success并继续商户自身业务处理，校验失败返回failure
            // 在支付宝的业务通知中，只有交易通知状态为 TRADE_SUCCESS 或 TRADE_FINISHED 时，支付宝才会认定为买家付款成功。
            if ("TRADE_SUCCESS".equals(trade_status) || "TRADE_FINISHED".equals(trade_status)){
                // 还需要做一个判断，虽然你支付的状态判断完了，但是有没有这么一种可能，你的交易记录中的支付状态已经变成付款了，或者是关闭了，那么应该返回验签失败！
                // 查询交易记录对象 根据out_trade_no 查询
                PaymentInfo paymentInfo = paymentService.getPaymentInfo(out_trade_no, PaymentType.ALIPAY.name());
                if (paymentInfo.getPaymentStatus().equals(PaymentStatus.PAID.name()) ||
                        paymentInfo.getPaymentStatus().equals(PaymentStatus.ClOSED.name())){
                    return "failure";
                }
//                if (out_trade_no.equals(paymentInfo.getOutTradeNo() && (total_amout == paymentInfo.getTotalAmount() )&& app_id.equals(AlipayConfig.appId))) {
//
//                }

                //表示支付成功，此时才会更新交易状态
                paymentService.paySuccess(out_trade_no, PaymentType.ALIPAY.name(), paramMap);
                //发送消息通知订单模块， 并修改订单状态 ，进度状态
                return "success";
            }

        } else {

            return "failure";

        }
        return "failure";
    }

    // 发起退款！http://localhost:8205/api/payment/alipay/refund/20
    @RequestMapping("refund/{orderId}")
    @ResponseBody
    public Result refund(@PathVariable(value = "orderId")Long orderId) {
        // 调用退款接口
        boolean flag = alipayService.refund(orderId);

        return Result.ok(flag);
    }


    // 根据订单Id关闭订单
    @GetMapping("closePay/{orderId}")
    @ResponseBody
    public Boolean closePay(@PathVariable Long orderId){
        Boolean aBoolean = alipayService.closePay(orderId);
        return aBoolean;
    }
    // 查看是否有交易记录
    @RequestMapping("checkPayment/{orderId}")
    @ResponseBody
    public Boolean checkPayment(@PathVariable Long orderId){
        // 调用退款接口
        boolean flag = alipayService.checkPayment(orderId);
        return flag;
    }

    //通过outTradeNo  查询PaymentInfo 方法
    @GetMapping("getPaymentInfo/{outTradeNo}")
    @ResponseBody
    public PaymentInfo getPaymentInfo(@PathVariable String outTradeNo) {
        //通过交易编号 ， 与支付方式查询PaymentInfo
        PaymentInfo paymentInfo = paymentService.getPaymentInfo(outTradeNo, PaymentType.ALIPAY.name());
        if (null != paymentInfo) {
            return paymentInfo;
        }
        return null;
    }

}
