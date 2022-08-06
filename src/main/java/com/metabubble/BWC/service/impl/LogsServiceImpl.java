package com.metabubble.BWC.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.metabubble.BWC.entity.Admin;
import com.metabubble.BWC.entity.Logs;
import com.metabubble.BWC.mapper.LogsMapper;
import com.metabubble.BWC.service.AdminService;
import com.metabubble.BWC.service.LogsService;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;

@Service
public class LogsServiceImpl extends ServiceImpl<LogsMapper, Logs>
        implements LogsService {

    @Autowired
    private LogsService logsService;

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private AdminService adminService;

    /**
     * 管理员日志
     * @param name 标题
     * @param content 内容
     * @author 晴天小杰
     */
    @Override
    public void saveLog(String name, String content) {
        Logs log = new Logs();
        Long adminId = (Long) request.getSession().getAttribute("admin");
        log.setAdminId(adminId);
        Admin adminServiceById = adminService.getById(adminId);
        log.setAdminName(adminServiceById.getName());
        log.setName(name);
        log.setContent(content);
        logsService.save(log);
    }
}
