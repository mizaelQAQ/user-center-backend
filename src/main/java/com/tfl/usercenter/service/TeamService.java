package com.tfl.usercenter.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tfl.usercenter.model.domain.Team;
import com.tfl.usercenter.model.domain.User;

/**
* @author u
* @description 针对表【team(队伍)】的数据库操作Service
* @createDate 2024-05-08 10:47:36
*/
public interface TeamService extends IService<Team> {
    /**
     * 添加队伍
     * @param team
     * @return
     */
    Long addTeam(Team team, User loginUser);
}