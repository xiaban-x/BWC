package com.metabubble.BWC.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.metabubble.BWC.common.R;
import com.metabubble.BWC.entity.Config;
import com.metabubble.BWC.entity.Cusservice;
import com.metabubble.BWC.service.ConfigService;
import com.metabubble.BWC.service.CusserviceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 客服
 */
@RestController
@RequestMapping("/cusservice")
@Slf4j
public class CusserviceController {

    @Autowired
    private ConfigService configService;

    /**
     * 客服信息展示
     * type 配置类型，6为微信客服，7为QQ客服，8为客服邮箱，9为客服电话
     * author 晴天小杰
     * @return
     */
    @GetMapping
    public R<List<Config>> getCusservice(Integer type){
        LambdaQueryWrapper<Config> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Config::getType,type);
        List<Config> cusserviceMsg = configService.list(queryWrapper);
        return R.success(cusserviceMsg);
    }



}
