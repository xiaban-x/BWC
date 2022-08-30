package com.metabubble.BWC.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.metabubble.BWC.entity.Admin;
import com.metabubble.BWC.entity.Orders;
import com.metabubble.BWC.entity.UserMsg;
import com.metabubble.BWC.mapper.AdminMapper;
import com.metabubble.BWC.mapper.UserMsgMapper;
import com.metabubble.BWC.service.AdminService;
import com.metabubble.BWC.service.UserMsgService;
import com.metabubble.BWC.utils.MobileUtils;
import org.springframework.stereotype.Service;

@Service
public class UserMsgServiceImpl extends ServiceImpl<UserMsgMapper, UserMsg>
        implements UserMsgService {
    @Override
    public void addUserCashback(Orders orders) {
        UserMsg userMsg = new UserMsg();
        userMsg.setUserId(orders.getUserId());
        String cashbackMsg = "任务"+orders.getId()+"返现"+orders.getRebate();
        userMsg.setType(2);
        userMsg.setMsg(cashbackMsg);
        this.save(userMsg);
    }

    @Override
    public void addWithdrawals(Long id, String amount) {
        UserMsg userMsg = new UserMsg();
        userMsg.setUserId(id);
        userMsg.setType(0);
        userMsg.setMsg(amount);
        this.save(userMsg);
    }


    @Override
    public void addRecharge(Long id, String amount) {
        UserMsg userMsg = new UserMsg();
        userMsg.setUserId(id);
        userMsg.setType(1);
        userMsg.setMsg(amount);
        this.save(userMsg);
    }

    @Override
    public void addCashback(Long id, String tel, String amount) {
        UserMsg userMsg = new UserMsg();
        userMsg.setUserId(id);
        userMsg.setType(2);
        String s = MobileUtils.blurPhone(tel);
        String cashbackMsg = "用户"+s+"返现"+amount;
        userMsg.setMsg(cashbackMsg);
        this.save(userMsg);
    }
}
