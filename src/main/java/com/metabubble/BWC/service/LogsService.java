package com.metabubble.BWC.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.metabubble.BWC.entity.Logs;

import javax.servlet.http.HttpServletRequest;

public interface LogsService extends IService<Logs> {

    //存储管理员操作日志  (标题， 内容)
    public void saveLog(String name, String content);

}
