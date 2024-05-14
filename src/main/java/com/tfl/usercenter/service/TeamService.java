package com.tfl.usercenter.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tfl.usercenter.model.domain.Team;
import com.tfl.usercenter.model.domain.User;
import com.tfl.usercenter.model.dto.TeamQuery;
import com.tfl.usercenter.model.request.TeamJoinRequest;
import com.tfl.usercenter.model.request.TeamQuitRequest;
import com.tfl.usercenter.model.request.TeamUpdateRequest;
import com.tfl.usercenter.model.vo.TeamUserVO;

import java.util.List;

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

    /**
     * 队伍列表
     * @param teamQuery
     * @return
     */
    List<TeamUserVO> listTeams(TeamQuery teamQuery,boolean isAdmin);

    /**
     * 更新队伍
     * @param teamUpdateRequest
     * @return
     */
    boolean updateTeam(TeamUpdateRequest teamUpdateRequest, User loginUser);

    /**
     * 加入队伍
     * @param teamJoinRequest
     * @return
     */
    boolean joinTeam(TeamJoinRequest teamJoinRequest,User loginUser);

    boolean quitTeam(TeamQuitRequest teamQuitRequest, User loginUser);

    boolean deleteTeam(long id, User loginUser);
}
