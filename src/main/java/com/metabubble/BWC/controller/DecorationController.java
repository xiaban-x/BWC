package com.metabubble.BWC.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.metabubble.BWC.common.R;
import com.metabubble.BWC.entity.Config;
import com.metabubble.BWC.entity.User;
import com.metabubble.BWC.service.ConfigService;
import com.metabubble.BWC.service.LogsService;
import com.metabubble.BWC.service.MerchantService;
import com.metabubble.BWC.service.MerchantTypeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 首页装修
 */
@RestController
@RequestMapping("/decoration")
@Slf4j
public class DecorationController {

    @Autowired
    private ConfigService configService;

    @Autowired
    private LogsService logsService;

    /**
     * 用户端首页轮播图展示
     * author 晴天小杰
     * @return
     */
    @GetMapping("/t/{type}")
    public R<List<Config>> getAll(@PathVariable Integer type){
        LambdaQueryWrapper<Config> queryWrapper =  new LambdaQueryWrapper();
        queryWrapper.eq(Config::getType,type);
        List<Config> configs = configService.list(queryWrapper);
        return R.success(configs);
    }

    /**
     * 用户端首页轮播图删除
     * author 晴天小杰
     * @param id 页面传来的配置id
     * @return
     */
    @DeleteMapping("/{id}")
    public R<String> delete(@PathVariable("id") Long id){
        Config CBI = configService.getById(id);
        configService.removeById(id);
        logsService.saveLog("删除装修","删除了"+CBI.getName()+"信息");
        return R.success("删除成功");
    }

    /**
     * 用户端首页轮播图添加
     * author 晴天小杰
     * @param config 页面传来的添加数据
     * @return
     */
    @PostMapping
    public R<String> save(@RequestBody Config config){
        configService.save(config);
        logsService.saveLog("添加装修","添加了"+config.getName()+"信息");
        return R.success("添加成功");
    }

    /**
     * 用户端首页轮播图根据id回显数据
     * author 晴天小杰
     * @param id 页面传来的配置id
     * @return
     */
    @GetMapping("/{id}")
    public R<Config> getById(@PathVariable("id") Long id){
        Config config = configService.getById(id);
        return R.success(config);
    }

    /**
     * 用户端首页轮播图根据id修改
     * author 晴天小杰
     * @param config 页面传来的配置数据
     * @return
     */
    @PutMapping
    public R<String> update(@RequestBody Config config){
        configService.updateById(config);
        logsService.saveLog("修改装修","修改了"+config.getName()+"基本信息");
        return R.success("修改成功");
    }


}
