package com.metabubble.BWC.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.metabubble.BWC.common.R;
import com.metabubble.BWC.entity.User;
import com.metabubble.BWC.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

/**
 * 数据中心
 * 操作日志
 */

@RestController
@RequestMapping("/data")
@Slf4j
public class DataController {

    @Autowired
    private UserService userService;

    /**
     * 查询日、月、年新增用户与总用户
     * author 晴天小杰
     * @return
     */
    @GetMapping("/getUser")
    public R<Map> getuser(){
        //查询总用户数
        //SQL:select count(*) from user;
        Map<String,Integer> maps = new HashMap<>();
        int allUser = userService.count();
        maps.put("allUser",allUser);

        //查询年新增用户数
        //SQL:select * from user where year(create_time)=2022 and month(create_time)=7
        //                                                    and day(create_time)=17
        Calendar calendar = Calendar.getInstance();
        LambdaQueryWrapper<User> queryWrapperYear = new LambdaQueryWrapper<>();
        int year = calendar.get(Calendar.YEAR);
        queryWrapperYear.apply("year(create_time)="+year);
        Integer countYear = userService.count(queryWrapperYear);
        maps.put("yearNewUser",countYear);

        //查询月新增用户数
        LambdaQueryWrapper<User> queryWrapperMonth = new LambdaQueryWrapper<>();
        int month = calendar.get(Calendar.MONTH) + 1;
        queryWrapperMonth.apply("year(create_time)="+year);
        queryWrapperMonth.apply("month(create_time)="+month);
        Integer countMonth = userService.count(queryWrapperMonth);
        maps.put("monthNewUser",countMonth);

        //查询日新增用户数
        LambdaQueryWrapper<User> queryWrapperDay = new LambdaQueryWrapper<>();
        int day = calendar.get(Calendar.DATE);
        queryWrapperDay.apply("year(create_time)="+year);
        queryWrapperDay.apply("month(create_time)="+month);
        queryWrapperDay.apply("day(create_time)="+day);
        Integer countDay = userService.count(queryWrapperDay);
        maps.put("dayNewUser",countDay);

        return R.success(maps);
    }

    /**
     * 查询日、月、年支出与总支出
     * author 晴天小杰
     * @return
     */
    @GetMapping("/getAmount")
    public R<Map> getMoney(){
        //查询总支出数
        //SELECT sum(price) as sumAll FROM t_order
        Map<String, BigDecimal> maps = new HashMap<>();
        QueryWrapper queryWrapperAll = new QueryWrapper<>();
        queryWrapperAll.select("sum(withdrawn_amount) as withdrawnAmount");
        User allAmount = userService.getOne(queryWrapperAll);
        if (allAmount != null) {
            maps.put("allAmount",allAmount.getWithdrawnAmount());
        }else {
            maps.put("allAmount", BigDecimal.valueOf(0));
        }

        //查询年支出数
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        QueryWrapper queryWrapperYear = new QueryWrapper<>();
        queryWrapperYear.apply("year(create_time)="+year);
        queryWrapperYear.select("sum(withdrawn_amount) as withdrawnAmount");
        User yearAmount = userService.getOne(queryWrapperYear);
        if (yearAmount != null) {
            maps.put("yearAmount",yearAmount.getWithdrawnAmount());
        }else {
            maps.put("yearAmount", BigDecimal.valueOf(0));
        }

        //查询月支出数
        QueryWrapper queryWrapperMonth = new QueryWrapper<>();
        int month = calendar.get(Calendar.MONTH) + 1;
        queryWrapperMonth.apply("year(create_time)="+year);
        queryWrapperMonth.apply("month(create_time)="+month);
        queryWrapperMonth.select("sum(withdrawn_amount) as withdrawnAmount");
        User monthAmount = userService.getOne(queryWrapperMonth);
        if (monthAmount != null) {
            maps.put("monthAmount",monthAmount.getWithdrawnAmount());
        }else {
            maps.put("monthAmount", BigDecimal.valueOf(0));
        }

        //查询日支出数
        QueryWrapper queryWrapperDay = new QueryWrapper<>();
        int day = calendar.get(Calendar.DATE);
        queryWrapperDay.apply("year(create_time)="+year);
        queryWrapperDay.apply("month(create_time)="+month);
        queryWrapperDay.apply("day(create_time)="+day);
        queryWrapperDay.select("sum(withdrawn_amount) as withdrawnAmount");
        User dayAmount = userService.getOne(queryWrapperDay);
        if (dayAmount != null){
            maps.put("dayAmount",dayAmount.getWithdrawnAmount());
        }else {
            maps.put("dayAmount", BigDecimal.valueOf(0));
        }
        return R.success(maps);
    }






}
