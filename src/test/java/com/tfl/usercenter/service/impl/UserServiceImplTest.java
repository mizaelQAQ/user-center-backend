package com.tfl.usercenter.service.impl;

import com.tfl.usercenter.model.domain.User;
import com.tfl.usercenter.service.UserService;
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest
class UserServiceImplTest {

    @Resource
    private UserService userService;

    @Test
    void searchUserByTags() {
        List<String> tagNameList = Arrays.asList("java","python"); //["java","c++","python"]
        List<User> users = userService.searchUserByTagsByMemory(tagNameList);
        Assert.assertNotNull(users);
    }
}