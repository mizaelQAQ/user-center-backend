package com.tfl.usercenter.service.impl;
import java.util.Date;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tfl.usercenter.common.ErrorCode;
import com.tfl.usercenter.exception.BusinessException;
import com.tfl.usercenter.mapper.TeamMapper;
import com.tfl.usercenter.model.domain.Team;
import com.tfl.usercenter.model.domain.User;
import com.tfl.usercenter.model.domain.UserTeam;
import com.tfl.usercenter.model.dto.TeamQuery;
import com.tfl.usercenter.model.enums.TeamStatusEnum;
import com.tfl.usercenter.model.request.TeamJoinRequest;
import com.tfl.usercenter.model.request.TeamQuitRequest;
import com.tfl.usercenter.model.request.TeamUpdateRequest;
import com.tfl.usercenter.model.vo.TeamUserVO;
import com.tfl.usercenter.model.vo.UserVO;
import com.tfl.usercenter.service.TeamService;
import com.tfl.usercenter.service.UserService;
import com.tfl.usercenter.service.UserTeamService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;

import javax.annotation.Resource;
import java.util.*;

@Service
@Transactional(rollbackFor = Exception.class)
public class TeamServiceImpl extends ServiceImpl<TeamMapper, Team>
    implements TeamService {

    @Resource
    private UserService userService;

    @Resource
    private UserTeamService userTeamService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long addTeam(Team team, User loginUser) {
        //请求参数是否为空？
        if (team == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //是否登录，未登录不允许创建
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        long userId = loginUser.getId();
        //校验信息
        //队伍人数 > 1 且 <= 20
        int maxNum = Optional.ofNullable(team.getMaxNum()).orElse(0);
        if (maxNum < 1 || maxNum > 20) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //队伍标题 <= 20
        String name = team.getName();
        if (StringUtils.isNotBlank(name) && name.length() > maxNum) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //描述 <= 512
        String description = team.getDescription();
        if (StringUtils.isNotBlank(description) && description.length() > 512) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //status 是否公开（int）不传默认为 0（公开）
        Integer status = Optional.ofNullable(team.getStatus()).orElse(0);
        TeamStatusEnum statusEnum = TeamStatusEnum.getEnumByValue(status);
        if (statusEnum == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //如果 status 是加密状态，一定要有密码，且密码 <= 32
        String password = team.getPassword();
        if (TeamStatusEnum.SECRET.equals(statusEnum)) {
            if (StringUtils.isBlank(password) || password.length() > 32) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR);
            }
        }
        //超时时间 > 当前时间
        Date expireTime = team.getExpireTime();
        if (new Date().after(expireTime)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //校验用户最多创建 5 个队伍
        //TODO 可能同时创建多个队伍
        QueryWrapper<Team> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userId",userId);
        long hasTeamNum = this.count(queryWrapper);
        if (hasTeamNum > 5) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //插入队伍信息到队伍表
        team.setUserId(userId);
        boolean result = this.save(team);
        Long teamId = team.getId();
        if (!result || teamId == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        //插入用户  => 队伍关系到关系表
        UserTeam userTeam = new UserTeam();
        userTeam.setUserId(userId);
        userTeam.setTeamId(teamId);
        userTeam.setJoinTime(new Date());
        result = userTeamService.save(userTeam);
        if (!result) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        return teamId;
    }

    @Override
    public List<TeamUserVO> listTeams(TeamQuery teamQuery,boolean isAdmin) {
        QueryWrapper<Team> queryWrapper = new QueryWrapper<>();
        // 根据id查询
        Long id = teamQuery.getId();
        if (id != null && id > 0) {
            queryWrapper.eq("id", id);
        }
        // 搜索关键词（名称和描述)
        String searchText = teamQuery.getSearchText();
        if (StringUtils.isNotBlank(searchText)) {
            queryWrapper.and(qw -> qw.like("name", searchText).or().like("description", searchText));
        }
        // 根据名称查询
        String name = teamQuery.getName();
        if (StringUtils.isNotBlank(name)) {
            queryWrapper.like("name", name);
        }
        // 根据描述查询
        String description = teamQuery.getDescription();
        if (StringUtils.isNotBlank(description)) {
            queryWrapper.like("description", description);
        }

        // 根据最大人数查询
        Integer maxNum = teamQuery.getMaxNum();
        if (maxNum != null && maxNum > 0) {
            queryWrapper.eq("maxNum", maxNum);
        }
        // 根据创建人查询
        Long userId = teamQuery.getUserId();
        if (userId != null && userId > 0) {
            queryWrapper.eq("userId", userId);
        }
        // 根据状态查询
        Integer status = teamQuery.getStatus();
        TeamStatusEnum statusEnum = TeamStatusEnum.getEnumByValue(status);
        if (statusEnum == null) {
            statusEnum = TeamStatusEnum.PUBLIC;
        }
        if (!isAdmin && !statusEnum.equals(TeamStatusEnum.PUBLIC)) {
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        if (status != null && status > -1) {
            queryWrapper.eq("status", statusEnum.getValue());
        }
        // 不展示已过期的队伍
        queryWrapper.and(qw -> qw.gt("expireTime", new Date())
                .or().isNull("expireTime")
        );

        List<Team> teamList = this.list(queryWrapper);
        if (CollectionUtils.isEmpty(teamList)) {
            return Collections.emptyList();
        }

        List<TeamUserVO> teamUserVOList = new ArrayList<>();
        //关联查询创建人信息
        for (Team team : teamList) {
            Long userId1 = team.getUserId();
            if (userId1 == null) {
                continue;
            }
            User user = userService.getById(userId1);
            TeamUserVO teamUserVO = new TeamUserVO();
            BeanUtils.copyProperties(team, teamUserVO);
            //脱敏用户信息
            if (user != null){
                UserVO userVO = new UserVO();
                BeanUtils.copyProperties(user, userVO);
                teamUserVO.setCreateUser(userVO);
            }
            teamUserVOList.add(teamUserVO);
        }
        return teamUserVOList;
    }

    @Override
    public boolean updateTeam(@RequestBody TeamUpdateRequest teamUpdateRequest, User loginUser) {
         Long id = teamUpdateRequest.getId();

         Team oldTeam = getTeamById(id);
         //只有管理员和队伍创建者可修改
         if (oldTeam.getUserId() != loginUser.getId() && !userService.isAdmin(loginUser)) {
             throw new BusinessException(ErrorCode.NO_AUTH);
         }
        TeamStatusEnum statusEnum = TeamStatusEnum.getEnumByValue(teamUpdateRequest.getStatus());
         if (statusEnum == null) {
             throw new BusinessException(ErrorCode.PARAMS_ERROR);
         }
         if (statusEnum.equals(TeamStatusEnum.SECRET) && StringUtils.isBlank(teamUpdateRequest.getPassword())) {
             throw new BusinessException(ErrorCode.PARAMS_ERROR,"加密房间必须设置密码");
         }

        Team updateTeam = new Team();
         BeanUtils.copyProperties(teamUpdateRequest, updateTeam);
        return this.updateById(updateTeam);
    }

    @Override
    public boolean joinTeam(TeamJoinRequest teamJoinRequest,User loginUser) {
        Long id = teamJoinRequest.getId();
        long userId = loginUser.getId();

        // 1. 用户最多加入 5 个队伍
        QueryWrapper<UserTeam> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userId", userId);
        long count = userTeamService.count(queryWrapper);
        if (count >= 5) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"最多加入5个队伍");
        }
        // 2. 队伍必须存在，只能加入未满、未过期的队伍
        Team team = getTeamById(id);
        Long teamId = team.getId();
        queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("teamId", teamId);
        long teamHasJoinNum = userTeamService.count(queryWrapper);
        if (teamHasJoinNum >= team.getMaxNum()) {
            throw new BusinessException(ErrorCode.NULL_ERROR,"队伍已满");
        }
        //  4. 禁止加入私有的队伍
        TeamStatusEnum teamStatusEnum = TeamStatusEnum.getEnumByValue(team.getStatus());
        if (teamStatusEnum.equals(TeamStatusEnum.PRIVATE)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"禁止进入私有队伍");
        }
        //  5. 如果加入的队伍是加密的，必须密码匹配才可以
        if (TeamStatusEnum.SECRET.equals(teamStatusEnum)) {
            if (!team.getPassword().equals(teamJoinRequest.getPassword())){
                throw new BusinessException(ErrorCode.NULL_ERROR,"密码错误");
            }
        }
        // 3. 不能加入自己的队伍，不能重复加入已加入的队伍（幂等性）
        checkHasJoinTeam(teamId, userId,false);
        // 6 新增队伍 - 用户关联信息
        UserTeam userTeam = new UserTeam();
        userTeam.setUserId(userId);
        userTeam.setTeamId(teamId);
        userTeam.setJoinTime(new Date());
        UpdateWrapper<Team> teamUpdateWrapper = new UpdateWrapper<>();
        teamUpdateWrapper.eq("id", teamId).setSql("num = num + 1");
        this.update(teamUpdateWrapper);
        return userTeamService.save(userTeam);
    }

    private void checkHasJoinTeam(long teamId, long userId,boolean expected) {
        QueryWrapper<UserTeam> userTeamQueryWrapper = new QueryWrapper<>();
        userTeamQueryWrapper.eq("userId", userId);
        userTeamQueryWrapper.eq("teamId", teamId);
        long hasJoinTeam = userTeamService.count(userTeamQueryWrapper);
        if (hasJoinTeam > 0 && !expected) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"用户已加入该队伍");
        }
    }

    private Team getTeamById(Long id) {
        if (id == null || id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Team team = this.getById(id);
        if (team == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR,"队伍不存在");
        }
        return team;
    }

    @Override
    public boolean quitTeam(TeamQuitRequest teamQuitRequest, User loginUser) {
        // 校验请求参数
        long userId = loginUser.getId();
        // 校验队伍是否存在
        Long id = teamQuitRequest.getId();

        Team team = getTeamById(id);
        Long teamId = team.getId();
        // 校验我是否已加入队伍
        checkHasJoinTeam(id, userId,true);
        // 如果队伍
        // 只剩一人，队伍解散
        QueryWrapper<UserTeam> queryWrapper = new QueryWrapper<>();
        if (team.getHasJoinNum() <= 1) {
            this.removeById(team);
        } else {  // 还有其他人
            queryWrapper = new QueryWrapper<>();
            Team updateTeam = new Team();
            // 如果是队长退出队伍，权限转移给第二早加入的用户 —— 先来后到（只用取 id 最小的 2 条数据）
            if (team.getUserId().equals(loginUser.getId())) {
                queryWrapper.eq("teamId", teamId);
                queryWrapper.last("order by id asc limit 2");
                List<UserTeam> userTeamList = userTeamService.list(queryWrapper);
                if (CollectionUtils.isEmpty(userTeamList) || userTeamList.size() <= 1) {
                    throw new BusinessException(ErrorCode.SYSTEM_ERROR);
                }
                UserTeam nextUserTeam = userTeamList.get(1);
                Long nextUserId = nextUserTeam.getUserId();
                updateTeam.setUserId(nextUserId);
            }
            // 非队长，自己退出队伍

            updateTeam.setId(teamId);
            updateTeam.setHasJoinNum(team.getHasJoinNum() - 1);

            this.updateById(updateTeam);
        }
        queryWrapper = new QueryWrapper<UserTeam>().eq("teamId", teamId).eq("userId", userId);
        return userTeamService.remove(queryWrapper);
    }
    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean deleteTeam(long id, User loginUser) {
        Team team = getTeamById(id);
        // 是否是队长
        if (loginUser.getId() != team.getUserId()) {
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        Long teamId = team.getId();
        QueryWrapper<UserTeam> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("teamId", teamId);
        boolean result = userTeamService.remove(queryWrapper);
        if (!result) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"删除关联信息失败");
        }
        return  this.removeById(team);
    }
}