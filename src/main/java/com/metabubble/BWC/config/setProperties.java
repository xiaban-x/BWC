package com.metabubble.BWC.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

/**
 * @author 17597
 * @version 1.0
 * @description TODO
 * @date 2023/3/11 16:09
 */
@Configuration
public class setProperties {
    /*
     * 解决druid 日志报错：discard long time none received connection:xxx
     * */
    @PostConstruct
    public void setProperties(){
        System.setProperty("druid.mysql.usePingMethod","false");
    }
}
