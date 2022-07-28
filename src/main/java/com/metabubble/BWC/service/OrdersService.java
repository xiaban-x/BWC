package com.metabubble.BWC.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.metabubble.BWC.entity.Orders;

import java.time.LocalDateTime;

public interface OrdersService extends IService<Orders> {



    //判断订单是否过期
    public Boolean updateStatusFormExpiredTime(Long id);
    //添加订单过期时间
    public Orders addExpiredTime(Orders orders);
    //用户一审机器审核
    public Boolean checkByMachine(Orders orders);
    //跟新订单返现金额
    public Orders updateRebate(Orders orders);




}
