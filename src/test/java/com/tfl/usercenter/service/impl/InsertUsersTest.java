package com.tfl.usercenter.service.impl;

import com.tfl.usercenter.model.domain.User;
import com.tfl.usercenter.service.UserService;
import org.springframework.util.StopWatch;

import javax.annotation.Resource;

public class InsertUsersTest {

    @Resource
    private UserService userService;

    public void doInsertUsers() {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        final int INSERT_NUM = 10000;
        for (int i = 0; i < INSERT_NUM; i++) {
            User user = new User();
            user.setUsername("假用户");
            user.setTags("");
            user.setUserAccount("fakeAcc");
            user.setAvatarUrl("https://i0.hdslb.com/bfs/article/9b371d22e134cf7bcaf6f8908f1a550b19482903.gif@!web-article-pic.avif");
            user.setGender(0);
            user.setUserPassword("12345678");
            user.setPhone("123");
            user.setEmail("123@qq.com");
            user.setUserStatus(0);
            user.setUserRole(0);
            user.setPlanetCode("1111111");

        }
        stopWatch.stop();
        System.out.println(stopWatch.getTotalTimeMillis());
    }
}
