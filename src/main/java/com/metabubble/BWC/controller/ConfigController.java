package com.metabubble.BWC.controller;

import com.metabubble.BWC.common.R;
import com.metabubble.BWC.entity.Config;
import com.metabubble.BWC.service.ConfigService;
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

//    /**
//     * 增加配置
//     * author cclucky
//     * @param config
//     * @return
//     */
//    @PostMapping
//    public R<String> save(@RequestBody Config config) {
//        configService.save(config);
//        return R.success("添加成功");
//    }

    /**
     * 更新配置
     * author cclucky
     * @param config
     * @return
     */
    @PutMapping
    public R<String> updateById(@RequestBody Config config) {
        configService.updateById(config);
        return R.success("修改成功");
    }

}