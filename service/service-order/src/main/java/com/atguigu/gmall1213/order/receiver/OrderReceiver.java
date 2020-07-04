package com.atguigu.gmall1213.order.receiver;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall1213.common.constant.MqConst;
import com.atguigu.gmall1213.common.service.RabbitService;
import com.atguigu.gmall1213.model.enums.PaymentStatus;
import com.atguigu.gmall1213.model.enums.ProcessStatus;
import com.atguigu.gmall1213.model.order.OrderInfo;
import com.atguigu.gmall1213.model.payment.PaymentInfo;
import com.atguigu.gmall1213.order.mapper.OrderInfoMapper;
import com.atguigu.gmall1213.order.service.OrderService;
import com.atguigu.gmall1213.payment.client.PaymentFeignClient;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rabbitmq.client.Channel;
import lombok.SneakyThrows;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.RedisServer;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.Map;

@Component
public class OrderReceiver {

    @Autowired
    private OrderService orderService;
    @Autowired
    private PaymentFeignClient paymentFeignClient;
    @Autowired
    private RabbitService rabbitService;

    /**
     * 取消订单消费者
     * 延迟队列，不能再这里做交换机与队列绑定
     *
     * @param orderId
     * @throws IOException
     */   //监听消息时获取订单Id
    @SneakyThrows
    @RabbitListener(queues = MqConst.QUEUE_ORDER_CANCEL)
    public void orderCancel(Long orderId, Message message, Channel channel) throws IOException {
        //判断订单iD  是否为空
        //这里不止要关闭交易的电商
        if (null != orderId) {
            //防止重复消费
            OrderInfo orderInfo = orderService.getById(orderId);
            //涉及到关闭 orderinfo   paymentInfo alipay

            //订单状态是未支付
            if (null != orderInfo && orderInfo.getOrderStatus().equals(ProcessStatus.UNPAID.getOrderStatus().name())) {
                //关闭过期订单
                // orderService.execExpiredOrder(orderId);
                //订单创建是就是未付款  判断是否有交易记录产生
                PaymentInfo paymentInfo = paymentFeignClient.getPaymentInfo(orderInfo.getOutTradeNo());
                if (null != paymentInfo && paymentInfo.getPaymentStatus().equals(PaymentStatus.UNPAID)) {
                    //先查询是否有交易记录  此时才会关闭交易记录  判断用户是否扫描二维码
                    Boolean aBoolean = paymentFeignClient.checkPayment(orderId);
                    if (aBoolean) {
                        //有交易记录    //关闭支付宝
                        Boolean flag = paymentFeignClient.closePay(orderId);
                        if (flag) {
                            // 关闭支付宝的订单成功 关闭 OrderInfo 表,paymentInfo
                            orderService.execExpiredOrder(orderId, "2");
                        } else {
                            // 关闭支付宝的订单失败，如果用户付款成功了，那么我们调用关闭接口是失败！
                            // 如果成功走正常流程
                            // 很极端，测试。。。。。
                            rabbitService.sendMessage(MqConst.EXCHANGE_DIRECT_PAYMENT_PAY, MqConst.ROUTING_PAYMENT_PAY, orderId);

                        }
                    } else {
                        //没有交易记录
                        orderService.execExpiredOrder(orderId, "2");
                    }
                } else {
                    //也就是说  在payment Info 中就根本没有交易记录

                    orderService.execExpiredOrder(orderId, "1");
                }

            }
        }
        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
    }


    // 订单支付，更改订单状态与通知扣减库存
    @SneakyThrows
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = MqConst.QUEUE_PAYMENT_PAY, durable = "true"),
            exchange = @Exchange(value = MqConst.EXCHANGE_DIRECT_PAYMENT_PAY),
            key = {MqConst.ROUTING_PAYMENT_PAY}
    ))
    public void updOrder(Long orderId, Message message, Channel channel) {
        // 判断orderId 不为空
        if (null != orderId) {
            // 更新订单的状态，还有进度的状态
            OrderInfo orderInfo = orderService.getById(orderId);
            // 判断状态
            if (null != orderInfo && orderInfo.getOrderStatus().equals(ProcessStatus.UNPAID.getOrderStatus().name())) {
                // 才准备更新数据
                orderService.updateOrderStatus(orderId, ProcessStatus.PAID);
                // 发送消息通知库存，准备减库存！
                orderService.sendOrderStatus(orderId);

            }
        }
        // 手动确认
        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
    }

    //准备写个监听减库存的消息队列
    @SneakyThrows
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = MqConst.QUEUE_WARE_ORDER, durable = "true"),
            exchange = @Exchange(value = MqConst.EXCHANGE_DIRECT_WARE_ORDER),
            key = {MqConst.ROUTING_WARE_STOCK}))
    public void updOrderStatus(String msgJson, Message message, Channel channel) {
        //获取json 数据
        if (StringUtils.isEmpty(msgJson)) {
            Map map = JSON.parseObject(msgJson, Map.class);

            String orderId = (String) map.get("orderId");
            String status = (String) map.get("status");
            //根据status 判断减库存结果
            if ("DEDUCTED".equals(status)) {
                //减库存成功，更新订单状态
                orderService.updateOrderStatus(Long.parseLong(orderId), ProcessStatus.WAITING_DELEVER);
            } else {
                //库存超卖了，如何处理
                //1 。 调用其他库存仓库货物进行补货 ，想办法补货，补库存
                //  2.  人工客服的介入   给你退款
                orderService.updateOrderStatus(Long.parseLong(orderId), ProcessStatus.STOCK_EXCEPTION);
            }
        }
        //手动确认
        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
    }
}
