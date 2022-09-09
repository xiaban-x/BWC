package com.metabubble.BWC;

import com.metabubble.BWC.common.ManageSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.time.LocalDateTime;
import java.util.TimeZone;

@Slf4j//开启日志
@SpringBootApplication//开启SpringBoot事务
@ServletComponentScan//对Servlet组件扫描
@EnableTransactionManagement //开启管理事务（多张表处理）
@EnableCaching //开启缓存注解
public class BWCApplication {
    public static void main(String[] args) {
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Shanghai"));
        SpringApplication.run(BWCApplication.class,args);
        log.info("项目启动成功");
        System.out.println(LocalDateTime.now());
    }
    /* 全局session变量 */
     public static ManageSession manageSession;
}
