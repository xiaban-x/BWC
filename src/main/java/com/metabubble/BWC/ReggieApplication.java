package com.metabubble.BWC;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Slf4j//开启日志
@SpringBootApplication//开启SpringBoot事务
@ServletComponentScan//对Servlet组件扫描
@EnableTransactionManagement //开启管理事务（多张表处理）
@EnableCaching //开启缓存注解
public class ReggieApplication {
    public static void main(String[] args) {
        SpringApplication.run(ReggieApplication.class,args);
        log.info("项目启动成功");
    }
}
