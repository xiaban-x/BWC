package com.metabubble.BWC;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.metabubble.BWC.common.ManageSession;
import com.metabubble.BWC.entity.Config;
import com.metabubble.BWC.service.ConfigService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.annotation.PostConstruct;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.List;
import java.util.TimeZone;

@Slf4j//开启日志
@SpringBootApplication//开启SpringBoot事务
@ServletComponentScan//对Servlet组件扫描
@EnableTransactionManagement //开启管理事务（多张表处理）
@EnableCaching //开启缓存注解
@EnableScheduling //开启定时任务
public class BWCApplication {
    public static void main(String[] args) {
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Shanghai"));
        SpringApplication.run(BWCApplication.class,args);
        log.info("项目启动成功");
        System.out.println(LocalDateTime.now());
    }
    /* 全局session变量 */
    public static ManageSession manageSession;
    /*
     * 解决druid 日志报错：discard long time none received connection:xxx
     * */
    static {
        System.setProperty("druid.mysql.usePingMethod","false");
    }

    @Autowired
    private ConfigService configService;
    //定时调用客服接口（解决数据库懒加载问题）
    @PostConstruct //启动项目先执行
    @Scheduled(cron="0 0 0/7 * * ?")   //每7小时执行一次
    public void execute(){
        LambdaQueryWrapper<Config> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Config::getType,9);
        configService.list(queryWrapper);
    }


}
