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

import java.util.List;
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
//        LambdaQueryWrapper<Logs> queryWrapper = new LambdaQueryWrapper<>();
        //添加排序条件 根据createTime进行逆向排序
        queryWrapperLogs.orderByDesc(Logs::getCreateTime);
        //添加条件查询
        //操作用户
        LambdaQueryWrapper<Admin> queryWrapperAdmin = new LambdaQueryWrapper<>();
        String adminNameTrue = null;
        if (adminName.length() != 0) {
            queryWrapperAdmin.eq(Admin::getName, adminName);
            Admin one = adminService.getOne(queryWrapperAdmin);
            //queryWrapperLogs.eq(one != null,Logs::getAdminId,one.getId());
            if (one != null){
                queryWrapperLogs.eq(Logs::getAdminId, one.getId());
                adminNameTrue = one.getName();
            }else {
                //表示无此人，查询为无
                queryWrapperLogs.eq(Logs::getId,0);
            }
        }
        //标题
        if (name.length() != 0){
            queryWrapperLogs.like(Logs::getName, name);
        }
        //内容
        if (content.length() != 0){
            queryWrapperLogs.like(Logs::getContent, content);
        }

        //执行查询 传入分页数据
        logService.page(pageInfo, queryWrapperLogs);

        //Dto对象拷贝
        Page<LogsDto> logsDtoPage = new Page<>();
        BeanUtils.copyProperties(pageInfo,logsDtoPage,"records");
        //查询用户id-》通过用户id查询用户名字-》展示用户名字
        List<Logs> records = pageInfo.getRecords();
        String finalAdminNameTrue = adminNameTrue;
        List<LogsDto> list = records.stream().map((item) -> { //stream流处理records
            LogsDto logsDto = new LogsDto();//存贮数据
            BeanUtils.copyProperties(item,logsDto);//对象拷贝
            logsDto.setAdminName(finalAdminNameTrue);//赋值名字
            return logsDto;
        }).collect(Collectors.toList());
        logsDtoPage.setRecords(list);

        return R.success(logsDtoPage);
    }




}
