package com.metabubble.BWC.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.metabubble.BWC.entity.Admin;
import com.metabubble.BWC.entity.Orders;
import com.metabubble.BWC.entity.UserMsg;

public interface UserMsgService extends IService<UserMsg> {


    //添加用户任务返现信息
    public void addUserCashback(Orders orders);
    //添加提现信息
    public void addWithdrawals(Long id,String amount);
    //添加充值信息
    public void addRecharge(Long id,String amount);
    //添加团队返现信息
    public void addCashback(Long id,String tel,String amount);
    //添加用户任务驳回返现信息
    public void overruleUserCashback(Orders orders);
    //添加团队驳回返现信息
    public void overruleCashback(Long id,String tel,String amount);
}
