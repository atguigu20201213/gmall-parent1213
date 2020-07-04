package com.atguigu.gmall1213.mq.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.management.Query;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class DeadLetterMqConfig {

    public static final String exchange_dead = "exchange.dead";
    public static final String routing_dead_1 = "routing.dead.1";
    public static final String routing_dead_2 = "routing.dead.2";
    public static final String queue_dead_1 = "queue.dead.1";
    public static final String queue_dead_2 = "queue.dead.2";

    @Bean
    public DirectExchange exchange(){

        return new DirectExchange(exchange_dead, true, false, null);
    }
    @Bean
    public Queue queue1() {
        //配置相关参数
        //设置  如果队列一  出现问题  参数转到  2
        HashMap<String, Object> map = new HashMap<>();
        //参数绑定
        map.put("x-dead-letter-exchange", exchange_dead);
        map.put("x-dead-letter-routing-key", routing_dead_2);
        //方式二 设置同一时间
        map.put("x-message-ttl", 10 * 1000);

        return new Queue(queue_dead_1,true, false, false, map);
    }
    @Bean
    public Binding binding() {
        //将队列一  通过routing_dead_1  key 绑定到     exchange_dead
        return BindingBuilder.bind(queue1()).to(exchange()).with(routing_dead_1);
    }
    //这个队列二  就是普通队列
    @Bean
    public Queue queue2() {

        return new Queue(queue_dead_2, true, false, false);
    }
    //设置队列二的绑定规则
    @Bean
    public  Binding binding2(){

        return BindingBuilder.bind(queue2()).to(exchange()).with(routing_dead_2);
    }


}