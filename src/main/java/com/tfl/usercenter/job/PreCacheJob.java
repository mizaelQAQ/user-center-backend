package com.tfl.usercenter.job;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tfl.usercenter.model.domain.User;
import com.tfl.usercenter.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class PreCacheJob {

    @Resource
    private UserService userService;
    @Resource
    private RedisTemplate<String,Object> redisTemplate;

    @Resource
    private RedissonClient redissonClient;
    //重点用户
    private List<Long> mainUserList = Arrays.asList(1L);

    @Scheduled(cron = "0 1 16 * * *")
    public void preCache() {
        RLock lock = redissonClient.getLock("dengta:precachejob:recommend:docache:lock");
        try {
            if (lock.tryLock(0,300000L,TimeUnit.MILLISECONDS)) {
                for (Long userId : mainUserList) {
                    String redisKey = String.format("dengta:user:recommend:%s", userId);
                    ValueOperations<String, Object> valueOperations = redisTemplate.opsForValue();
                    //无缓存，查数据库
                    QueryWrapper<User> queryWrapper = new QueryWrapper<>();
                    Page<User> userList = userService.page(new Page<>(1,20), queryWrapper);
                    //写缓存
                    try {
                        valueOperations.set(redisKey, userList,30000, TimeUnit.MILLISECONDS);
                    } catch (Exception e) {
                        log.error("redis set key error",e);
                    }
                }

            }
        } catch (InterruptedException e) {
            log.error("redis lock error",e);
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }


    }
}

