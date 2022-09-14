package com.metabubble.BWC.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.metabubble.BWC.entity.MerchantType;
import com.metabubble.BWC.entity.Orders;
import com.metabubble.BWC.entity.TeamMsg;

public interface TeamMsgService extends IService<TeamMsg> {

    //添加上下级类信息
    public void add(Long id,String tel,String msg);
    //添加提现信息
    public void addWithdrawals(Long id,String amount);
    //添加充值信息
    public void addRecharge(Long id,String amount);
    //添加团队返现信息
    public void addCashback(Long id,String tel,String amount);
}
