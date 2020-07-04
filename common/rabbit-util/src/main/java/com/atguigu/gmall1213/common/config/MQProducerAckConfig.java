package com.atguigu.gmall1213.common.config;

import com.alibaba.fastjson.JSON;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PostMapping;

import javax.annotation.PostConstruct;

@Component
public class MQProducerAckConfig
    implements RabbitTemplate.ConfirmCallback, RabbitTemplate.ReturnCallback {
  @Autowired private RabbitTemplate rabbitTemplate;
  // 初始化   RabbitTemplate return confirm
  @PostConstruct
  public void init() {
    rabbitTemplate.setConfirmCallback(this);
    rabbitTemplate.setReturnCallback(this);
  }

  /**
   * 消息的 确认机制 只确认消息是否正确到达交换机中，
   *
   * @param correlationData
   * @param ack
   * @param cause
   */
  @Override
  public void confirm(CorrelationData correlationData, boolean ack, String cause) {
    if (ack) {
      System.out.println("消息发送成功！");
    } else {
      System.out.println("消息发送失败！");
    }
  }

  /**
   * 判断消息有没有正确到达队列 时， 会去触发returnedMessage （）如果消息正确到达 则不会走returnedMessage 方法
   *
   * @param message 消息
   * @param replyCode 应答码
   * @param replyText 应答对应的内容
   * @param exchange 交换机
   * @param routingKey 路由键
   */
  @Override
  public void returnedMessage(
      Message message, int replyCode, String replyText, String exchange, String routingKey) {
    // 反序列化对象输出
    System.out.println("消息主体: " + new String(message.getBody()));
    System.out.println("应答码: " + replyCode);
    System.out.println("描述：" + replyText);
    System.out.println("消息使用的交换器 exchange : " + exchange);
    System.out.println("消息使用的路由键 routing : " + routingKey);
  }
}
