package com.tfl.usercenter.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tfl.usercenter.model.domain.UserTeam;
import com.tfl.usercenter.service.UserTeamService;
import com.tfl.usercenter.mapper.UserTeamMapper;
import org.springframework.stereotype.Service;

/**
* @author u
* @description 针对表【user_team(用户队伍关系)】的数据库操作Service实现
* @createDate 2024-05-08 10:50:26
*/
@Service
public class UserTeamServiceImpl extends ServiceImpl<UserTeamMapper, UserTeam>
    implements UserTeamService{

}




