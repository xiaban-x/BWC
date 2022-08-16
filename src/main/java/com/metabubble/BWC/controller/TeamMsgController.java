package com.metabubble.BWC.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.metabubble.BWC.common.BaseContext;
import com.metabubble.BWC.common.R;
import com.metabubble.BWC.entity.TeamMsg;
import com.metabubble.BWC.service.TeamMsgService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/teamMsg")
@Slf4j
public class TeamMsgController {

    @Autowired
    private TeamMsgService teamMsgService;

    /**
     * 获取团队信息
     * @param offset
     * @param limit
     * @return
     */
    @GetMapping
    public R<Page> page(int offset, int limit ,int type){
        Long id = BaseContext.getCurrentId();

        Page<TeamMsg> page =new Page<>(offset,limit);

        LambdaQueryWrapper<TeamMsg> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TeamMsg::getUserId,id);
        queryWrapper.orderByDesc(TeamMsg::getCreateTime);
        if (!(type==4)){
            queryWrapper.eq(TeamMsg::getType,type);
        }

        teamMsgService.page(page,queryWrapper);

        return R.success(page);

    }







}
