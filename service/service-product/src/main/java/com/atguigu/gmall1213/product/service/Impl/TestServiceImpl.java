package com.atguigu.gmall1213.product.service.Impl;

import com.alibaba.csp.sentinel.util.StringUtil;
import com.atguigu.gmall1213.product.service.TestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class TestServiceImpl implements TestService {
    @Autowired
    private StringRedisTemplate redisTemplate;

    @Override
    public synchronized void testLock() {
        //获取缓存中的key  如果不为空 +1 操作   否则返回为空
        String num = redisTemplate.opsForValue().get("num");
        if (StringUtil.isEmpty(num)) {
             return;
        }

        int number = Integer.parseInt(num);

        redisTemplate.opsForValue().set("num",String.valueOf(++number));
    }
}
