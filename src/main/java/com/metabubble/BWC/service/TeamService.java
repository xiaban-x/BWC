package com.metabubble.BWC.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.metabubble.BWC.entity.Orders;
import com.metabubble.BWC.entity.Team;

public interface TeamService extends IService<Team> {

    //向上一级返现
    public void cashbackForUserFromFirst(Long id);
    //向上二级返现
    public void cashbackForUserFromSecond(Long id);
}
