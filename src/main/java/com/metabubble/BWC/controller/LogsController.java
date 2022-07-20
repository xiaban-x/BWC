package com.metabubble.BWC.controller;

import com.metabubble.BWC.common.R;
import com.metabubble.BWC.entity.Logs;
import com.metabubble.BWC.service.LogsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/logs")
public class LogsController {

    @Autowired
    private LogsService logService;

    /**
     * 查询日志
     * @return
     */
    @PostMapping
    public R<List<Logs>> saveLog(){
        List<Logs> logs = logService.list();
        return R.success(logs);
    }


}
