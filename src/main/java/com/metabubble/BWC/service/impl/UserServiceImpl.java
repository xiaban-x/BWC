package com.metabubble.BWC.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.metabubble.BWC.entity.Admin;
import com.metabubble.BWC.entity.Orders;
import com.metabubble.BWC.entity.Task;
import com.metabubble.BWC.entity.User;
import com.metabubble.BWC.mapper.AdminMapper;
import com.metabubble.BWC.mapper.UserMapper;
import com.metabubble.BWC.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
        implements UserService {


    @Autowired
    private TaskService taskService;
    @Autowired
    private TeamService teamService;
    @Autowired
    private OrdersService ordersService;


    /**
     * 用户完成订单返现
     * @param orders 订单
     * @author leitianyu999
     */
    @Override
    @Transactional
    public void cashback(Orders orders) {
        Long userId = orders.getUserId();
        BigDecimal rebate = orders.getRebate();
        User user = this.getById(userId);
        user.setCashableAmount(user.getCashableAmount().add(rebate));
        user.setSavedAmount(user.getSavedAmount().add(rebate));
        this.updateById(user);
    }

    /**
     * 查询用户会员是否过期并更改
     * @param id 用户id
     * @return
     * @author leitianyu999
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Boolean checkGrade(Long id) {
        User byId = this.getById(id);
        //判断是否过期
        boolean after = byId.getMembershipExpTime().isAfter(LocalDateTime.now());
        //更改会员等级为0
        if (after==false){
            byId.setGrade(0);
        }
        //更改会员等级为1
        if (after){
            byId.setGrade(1);
        }
        this.updateById(byId);
        return after;
    }



//    @Override
//    public void cashbackForUserFromSecond(Long id) {
//        User user = this.getById(id);
//        BigDecimal bigDecimal = BigDecimal.valueOf(0.2);
//        user.setCashableAmount(user.getCashableAmount().add(bigDecimal));
//    }
//
//    @Override
//    public void cashbackForUserFromFirst(Long id) {
//        User user = this.getById(id);
//        BigDecimal bigDecimal = BigDecimal.valueOf(0.5);
//        user.setCashableAmount(user.getCashableAmount().add(bigDecimal));
//    }
}
