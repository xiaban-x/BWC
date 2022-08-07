package com.metabubble.BWC.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.metabubble.BWC.common.R;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.metabubble.BWC.dto.LogsDto;
import com.metabubble.BWC.entity.Admin;
import com.metabubble.BWC.entity.Logs;
import com.metabubble.BWC.service.AdminService;
import com.metabubble.BWC.service.LogsService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
        LambdaQueryWrapper<Logs> queryWrapperLogs = new LambdaQueryWrapper<>();
        //添加排序条件 根据createTime进行逆向排序
        queryWrapperLogs.orderByDesc(Logs::getCreateTime);
        //添加条件查询
        //操作用户
        queryWrapperLogs.like(adminName != null,Logs::getAdminName,adminName);
        //标题
        queryWrapperLogs.like(name != null ,Logs::getName, name);
        //内容
        queryWrapperLogs.like(content != null ,Logs::getContent, content);

        //执行查询 传入分页数据
        Page<Logs> page = logService.page(pageInfo, queryWrapperLogs);

/* 因为Dto废弃暂时注释

        //Dto对象拷贝
        Page<LogsDto> logsDtoPage = new Page<>();
        BeanUtils.copyProperties(pageInfo,logsDtoPage,"records");
        //查询用户id-》通过用户id查询用户名字-》展示用户名字
        List<Logs> records = pageInfo.getRecords();
        List<LogsDto> list = records.stream().map((item) -> { //stream流处理records
            LogsDto logsDto = new LogsDto();//存贮数据
            BeanUtils.copyProperties(item,logsDto);//对象拷贝
            Long adminId = item.getAdminId();//管理员id
            Admin adminMsg = adminService.getById(adminId);//管理员信息
            if (adminMsg != null){
                String adminMsgName = adminMsg.getName();//管理员名字
                logsDto.setAdminName(adminMsgName);//赋值名字
            }
            return logsDto;
        }).collect(Collectors.toList());
        logsDtoPage.setRecords(list);
*/

        return R.success(pageInfo);
    }




}
