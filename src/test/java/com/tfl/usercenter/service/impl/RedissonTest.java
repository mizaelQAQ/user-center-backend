package com.tfl.usercenter.service.impl;

import org.junit.jupiter.api.Test;
import org.redisson.api.RList;
import org.redisson.api.RedissonClient;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

@SpringBootTest
public class RedissonTest {
    @Resource
    RedissonClient redisson;

    @Test
    public void testRedisson() {
        RList<String> list = redisson.getList("test-list");
        list.add("doro");
        System.out.println(list.get(0));
    }
}
