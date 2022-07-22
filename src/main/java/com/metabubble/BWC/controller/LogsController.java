package com.metabubble.BWC.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.metabubble.BWC.common.R;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.metabubble.BWC.entity.Admin;
import com.metabubble.BWC.entity.Logs;
import com.metabubble.BWC.entity.User;
import com.metabubble.BWC.service.AdminService;
import com.metabubble.BWC.service.LogsService;
import com.metabubble.BWC.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/logs")
public class LogsController {

    @Autowired
    private LogsService logService;

    @Autowired
    private AdminService adminService;

    /**
     * 查询日志
     * @return
     * @author 晴天小杰
     */
    @GetMapping("/page")
    public R<Page> page(int offset, int limit, String adminName, String name, String content) {
        //构造分页查询器
        Page<Logs> pageInfo = new Page<>(offset, limit);
        //构造条件构造器
        LambdaQueryWrapper<Logs> queryWrapperLogs = new LambdaQueryWrapper();
        //添加排序条件 根据createTime进行逆向排序
        queryWrapperLogs.orderByDesc(Logs::getCreateTime);
        //添加条件查询
        //操作用户
        LambdaQueryWrapper<Admin> queryWrapperAdmin = new LambdaQueryWrapper<>();
        queryWrapperAdmin.eq(adminName != null, Admin::getName, adminName);
        Admin one = adminService.getOne(queryWrapperAdmin);
        //queryWrapperLogs.eq(one != null,Logs::getAdminId,one.getId());
        if (one != null){
            queryWrapperLogs.eq(Logs::getAdminId, one.getId());
        }else {
            //表示无此人，查询为无
            queryWrapperLogs.eq(Logs::getId,0);
        }
        //标题
        queryWrapperLogs.like(name != null, Logs::getName, name);
        //内容
        queryWrapperLogs.like(content != null, Logs::getContent, content);
        //执行查询 传入分页数据
        logService.page(pageInfo, queryWrapperLogs);
        return R.success(pageInfo);
    }




}
