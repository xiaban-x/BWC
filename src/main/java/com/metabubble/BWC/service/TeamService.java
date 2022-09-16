package com.metabubble.BWC.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.metabubble.BWC.entity.Orders;
import com.metabubble.BWC.entity.Team;
import com.metabubble.BWC.entity.User;

import java.math.BigDecimal;

public interface TeamService extends IService<Team> {

    //向上一级返现
    public BigDecimal cashbackForUserFromFirst(Long id, String tel);
    //向上二级返现
    public BigDecimal cashbackForUserFromSecond(Long id,String tel);
    //创建团队表
    public void save(User user);
    //创建团队表并添加上级
    public void save(User user,String invitation);
    //团队添加上一级和上二级
    public void addTeamTop(User user,User topUser);
    //将一二级返现驳回
    public void overruleCashback(Orders orders, User user);
}
