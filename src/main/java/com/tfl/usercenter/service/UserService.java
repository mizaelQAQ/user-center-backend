package com.tfl.usercenter.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tfl.usercenter.model.domain.User;
import com.baomidou.mybatisplus.extension.service.IService;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 用户服务
 *
 */
public interface UserService extends IService<User> {

    /**
     * 用户注册
     *
     * @param userAccount   用户账户
     * @param userPassword  用户密码
     * @param checkPassword 校验密码
     * @param planetCode    星球编号
     * @return 新用户 id
     */
    long userRegister(String userAccount, String userPassword, String checkPassword, String planetCode);

    /**
     * 用户登录
     *
     * @param userAccount  用户账户
     * @param userPassword 用户密码
     * @param request
     * @return 脱敏后的用户信息
     */
    User userLogin(String userAccount, String userPassword, HttpServletRequest request);

    /**
     * 用户脱敏
     *
     * @param originUser
     * @return
     */
    User getSafetyUser(User originUser);

    /**
     * 获取登录用户
     *
     * @param request
     * @return
     */
    User getLoginUser(HttpServletRequest request);

    /**
     * 用户注销
     *
     * @param request
     * @return
     */
    int userLogout(HttpServletRequest request);

    /**
     * 根据标签搜索用户
     * @param tagNameList 用户拥有的标签
     * @return
     */
    //List<User> searchUserByTagsBySQL(List<String> tagNameList);

    /**
     * 根据标签搜索用户
     * @param tagNameList 用户拥有的标签
     * @return
     */
    List<User> searchUserByTagsByMemory(List<String> tagNameList);

    /**
     * 更新用户信息
     * @param user 更新后信息
     * @param loginUser  当前登录用户
     */
    int updateUser(User user, User loginUser);

    Page<User> recommendUsers(long pageNum, long pageSize, HttpServletRequest request);

    boolean isAdmin(HttpServletRequest request);
    boolean isAdmin(User user);
}
