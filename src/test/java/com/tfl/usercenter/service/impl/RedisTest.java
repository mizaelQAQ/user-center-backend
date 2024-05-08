package com.tfl.usercenter.service.impl;

import com.tfl.usercenter.model.domain.User;
import com.tfl.usercenter.service.UserService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.util.StopWatch;

import javax.annotation.Resource;
@SpringBootTest
public class RedisTest {

    @Resource
    private RedisTemplate redisTemplate;

    @Test
    public void testRedis() {
        ValueOperations valueOperations = redisTemplate.opsForValue();
        valueOperations.set("MyString","dog");
        Object myString = valueOperations.get("MyString");
        Assertions.assertTrue("dog".equals(myString)) ;
    }


}
