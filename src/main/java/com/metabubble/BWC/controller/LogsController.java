package com.metabubble.BWC.controller;

import com.metabubble.BWC.common.R;
import com.metabubble.BWC.entity.Logs;
import com.metabubble.BWC.service.LogsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/logs")
public class LogsController {

    @Autowired
    private LogsService logService;

    @PostMapping
    public R<String> saveLog(@RequestBody Logs logs){
        System.out.println(logs.toString());
        logService.save(logs);
        return R.success("保存日志");

    }
}
