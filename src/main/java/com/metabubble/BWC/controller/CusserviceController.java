package com.metabubble.BWC.controller;

import com.metabubble.BWC.common.R;
import com.metabubble.BWC.entity.Cusservice;
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
    private CusserviceService cusserviceService;

    /**
     * 客服信息展示
     * cacheable: 缓存注释
     * value: 缓存名称
     * unless: 条件满足时不缓存数据
     * author 晴天小杰
     * @return
     */
    @GetMapping
    //注释原因：添加缓存需配合configController类修改功能的缓存更新
    @Cacheable(value = "cusserviceMsg",unless = "#result == null")
    public R<List<Cusservice>> getCusservice(){
        List<Cusservice> cusserviceMsg = cusserviceService.list();
        return R.success(cusserviceMsg);
    }



}
