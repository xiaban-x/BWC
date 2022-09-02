package com.metabubble.BWC.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.metabubble.BWC.common.BaseContext;
import com.metabubble.BWC.common.R;
import com.metabubble.BWC.entity.TeamMsg;
import com.metabubble.BWC.entity.UserMsg;
import com.metabubble.BWC.service.UserMsgService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/usermsg")
@Slf4j
public class UserMsgController {

    @Autowired
    private UserMsgService userMsgService;


    /**
     * 获取用户信息
     * @param offset
     * @param limit
     * @author leitianyu999
     * @return
     */
    @GetMapping
    public R<Page> page(int offset, int limit , int type){
        Long id = BaseContext.getCurrentId();

        Page<UserMsg> page =new Page<>(offset,limit);

        LambdaQueryWrapper<UserMsg> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserMsg::getUserId,id);
        queryWrapper.orderByDesc(UserMsg::getCreateTime);
        if (type!=3){
            queryWrapper.eq(UserMsg::getType,type);
        }

        userMsgService.page(page,queryWrapper);

        return R.success(page);

    }

    /**
     * 管理端根据id获取用户明细
     * @param id
     * @param offset
     * @param limit
     * @param type
     * @author leitianyu999
     * @return
     */
    @GetMapping("/admin")
    public R<Page> Page(Long id, int offset, int limit , int type){

        Page<UserMsg> page =new Page<>(offset,limit);

        LambdaQueryWrapper<UserMsg> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserMsg::getUserId,id);
        queryWrapper.orderByDesc(UserMsg::getCreateTime);
        if (type!=3){
            queryWrapper.eq(UserMsg::getType,type);
        }

        userMsgService.page(page,queryWrapper);

        return R.success(page);
    }


}
