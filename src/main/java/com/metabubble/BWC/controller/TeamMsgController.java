package com.metabubble.BWC.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.metabubble.BWC.common.BaseContext;
import com.metabubble.BWC.common.R;
import com.metabubble.BWC.dto.Imp.PageConverter;
import com.metabubble.BWC.dto.Imp.TeamConverter;
import com.metabubble.BWC.dto.TeamMsgDo;
import com.metabubble.BWC.entity.TeamMsg;
import com.metabubble.BWC.entity.User;
import com.metabubble.BWC.service.TeamMsgService;
import com.metabubble.BWC.service.UserService;
import com.metabubble.BWC.utils.MobileUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/teamMsg")
@Slf4j
public class TeamMsgController {

    @Autowired
    private TeamMsgService teamMsgService;
    @Autowired
    private UserService userService;

    /**
     * 获取团队信息
     * @param offset
     * @param limit
     * @author leitianyu999
     * @return
     */
    @GetMapping
    public R<Page> page(int offset, int limit ,int type){
        Long id = BaseContext.getCurrentId();

        Page<TeamMsg> page =new Page<>(offset,limit);

        LambdaQueryWrapper<TeamMsg> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TeamMsg::getUserId,id);
        queryWrapper.orderByDesc(TeamMsg::getCreateTime);
        if (type!=2){
            queryWrapper.eq(TeamMsg::getType,type);
        }else {
            queryWrapper.and(teamMsgLambdaQueryWrapper -> {
                teamMsgLambdaQueryWrapper.eq(TeamMsg::getType,0).or().eq(TeamMsg::getType,1);
            });
        }

        teamMsgService.page(page,queryWrapper);

        List<TeamMsg> records = page.getRecords();
        List<TeamMsg> collect = records.stream().map(item -> {
            if (item.getType() == 1 || item.getType() == 0) {
                item.setMsg(MobileUtils.blurPhone(item.getDownPhone()));
            }
            return item;
        }).collect(Collectors.toList());

        Page page1 = PageConverter.INSTANCES.PageToPage(page);
        page1.setRecords(collect);

        return R.success(page1);

    }

    /**
     * 管理端根据用户id查询团队明细
     * @param id
     * @param offset
     * @param limit
     * @param type
     * @return
     */
    @GetMapping("/admin")
    @Transactional
    public R<Page> Page(Long id, int offset, int limit ,int type){


        Page<TeamMsg> page =new Page<>(offset,limit);

        LambdaQueryWrapper<TeamMsg> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TeamMsg::getUserId,id);
        queryWrapper.orderByDesc(TeamMsg::getCreateTime);
        if (type!=4){
            queryWrapper.eq(TeamMsg::getType,type);
        }

        teamMsgService.page(page,queryWrapper);

        List<TeamMsg> records = page.getRecords();
        List<TeamMsgDo> collect = records.stream().map(item -> {
            TeamMsgDo teamMsgDo = TeamConverter.INSTANCES.TeamMsgToTeamMsgDo(item);
            if (teamMsgDo.getType() == 0 || teamMsgDo.getType() == 1) {
                LambdaQueryWrapper<User> queryWrapper1 = new LambdaQueryWrapper<>();
                queryWrapper1.eq(User::getTel, teamMsgDo.getDownPhone());
                User one = userService.getOne(queryWrapper1);
                teamMsgDo.setDownId(one.getId());
                teamMsgDo.setName(one.getName());
            }
            return teamMsgDo;
        }).collect(Collectors.toList());

        Page page1 = PageConverter.INSTANCES.PageToPage(page);
        page1.setRecords(collect);

        return R.success(page1);
    }





}
