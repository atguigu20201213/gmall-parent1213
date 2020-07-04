package com.atguigu.gmall1213.gateway.config;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;

@Configuration
public class KeyResolverConfig {

    @Bean
    public KeyResolver ipKeyResolver() {
        System.out.println("使用ip 限流.........");
        return exchange -> Mono.just(exchange.getRequest().getRemoteAddress().getHostName());
    }
    //    限流方式在配置文件中 只能存在一种
    //    @Bean
    //    public KeyResolver apiKeyResolver() {
    //        return exchange -> Mono.just(exchange.getRequest().getPath().value());
    //    }

}
