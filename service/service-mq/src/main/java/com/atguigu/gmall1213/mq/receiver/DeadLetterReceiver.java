package com.atguigu.gmall1213.mq.receiver;

import com.atguigu.gmall1213.mq.config.DeadLetterMqConfig;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Date;

@Component
@Configuration
public class DeadLetterReceiver {
    //队列监听  队列二
    @RabbitListener(queues = DeadLetterMqConfig.queue_dead_2)
    public void getMsg(String msg) {
        System.out.println("接收数据: \t" + msg);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        System.out.println("接受队列二 queue_dead_2: " + sdf.format(new Date()) + " Delay rece." + msg);
    }

}
