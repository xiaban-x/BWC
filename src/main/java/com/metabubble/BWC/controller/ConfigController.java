package com.metabubble.BWC.controller;

import com.metabubble.BWC.common.BaseContext;
import com.metabubble.BWC.common.R;
import com.metabubble.BWC.entity.Config;
import com.metabubble.BWC.service.AdminService;
import com.metabubble.BWC.service.ConfigService;
import com.metabubble.BWC.service.LogsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;


/**
 * 配置
 */
@RestController
@RequestMapping("/config")
@Slf4j
public class ConfigController {

    /**
     * 定义服务类
     * author cclucky
     */
    @Autowired
    private ConfigService configService;

    @Autowired
    private LogsService logsService;

    @Autowired
    private AdminService adminService;

    /**
     * 查询所有配置
     * author cclucky
     * @return
     */
    @GetMapping
    public R<List<Config>> getAll() {
        List<Config> configList = configService.list();
        return R.success(configList);
    }

    /**
     * 一个根据id获取内容的静态类，不设置接口，可调用
     * author cclucky
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public R<String> getContentById(@PathVariable Long id) {
        return configService.getContentById(id);
    }

    /**
     * 更新配置
     * author cclucky
     * @param config
     * @return
     */
    @PutMapping
    public R<String> updateById(@RequestBody Config config) {
        Long id = BaseContext.getCurrentId();
        configService.updateById(config);
        logsService.saveLog("修改配置信息", adminService.getById(id).getName() + "修改了配置信息");
        return R.success("修改成功");
    }

    @GetMapping("/getContents/{id}")
    public R<Config> getContentsById(@PathVariable Long id) {
        Config contents = configService.getById(id);
        return R.success(contents);
    }
}