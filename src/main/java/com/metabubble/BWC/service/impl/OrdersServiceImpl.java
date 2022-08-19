package com.metabubble.BWC.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.metabubble.BWC.common.CustomException;
import com.metabubble.BWC.entity.Orders;
import com.metabubble.BWC.entity.Task;
import com.metabubble.BWC.mapper.OrdersMapper;
import com.metabubble.BWC.service.OrdersService;
import com.metabubble.BWC.service.TaskService;
import com.metabubble.BWC.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Service
public class OrdersServiceImpl extends ServiceImpl<OrdersMapper, Orders>
        implements OrdersService {


    @Autowired
    private TaskService taskService;
    @Autowired
    private UserService userService;



    /**
     * 查询订单是否过期
     * @param id    订单id
     * @author leitianyu999
     */
    @Override
    //添加非事务处理使此方法直接生效
    @Transactional
    public Boolean updateStatusFormExpiredTime(Long id) {
        Orders orders = this.getById(id);
        LocalDateTime expiredTime = orders.getExpiredTime();
        LocalDateTime now = LocalDateTime.now();
        if (orders.getStatus()==0||orders.getStatus()==3){
            if (now.isAfter(expiredTime)) {
                orders.setStatus(8);
                this.updateById(orders);
                return false;
            }
            return true;
        }
        if (orders.getStatus()==2||orders.getStatus()==5){
            if(now.isAfter(expiredTime)){
                orders.setStatus(8);
                this.updateById(orders);
                return false;
            }
            return true;
        }
        throw new CustomException("订单状态错误");
    }

    @Override
    public Orders updateStatusFormExpiredTimeAndReturn(Orders orders) {
        if (orders.getStatus()==1||orders.getStatus()==4||orders.getStatus()==6||orders.getStatus()==7||orders.getStatus()==8){
            return orders;
        }
        LocalDateTime expiredTime = orders.getExpiredTime();
        LocalDateTime now = LocalDateTime.now();
        if (orders.getStatus()==0||orders.getStatus()==3){
            if (now.isAfter(expiredTime)) {
                orders.setStatus(8);
                this.updateById(orders);
                return orders;
            }
            return orders;
        }
        if (orders.getStatus()==2||orders.getStatus()==5){
            if(now.isAfter(expiredTime)){
                orders.setStatus(8);
                this.updateById(orders);
                return orders;
            }
            return orders;
        }
        throw new CustomException("订单"+orders.getId()+"状态错误!");
    }

    /**
     * 添加过期时间
     * @param orders
     * @author leitianyu999
     */
    @Override
    public Orders addExpiredTime(Orders orders) {
        if (orders.getStatus()==2||orders.getStatus()==4||orders.getStatus()==5) {
            LocalDate localDate = LocalDate.now();
            LocalDate localDate1 = localDate.plusDays(1);
            LocalTime localTime = LocalTime.of(9, 0);
            LocalDateTime localDateTime = LocalDateTime.of(localDate1, localTime);
            orders.setExpiredTime(localDateTime);
            return orders;
        }
        if (orders.getStatus()==0||orders.getStatus()==1||orders.getStatus()==3){
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime firstExpiredTime = now.plusMinutes(30);
            orders.setExpiredTime(firstExpiredTime);
            return orders;
        }
        throw new CustomException("订单添加过期时间错误");
    }

    /**
     * 用户一审机器审核
     * @param orders 订单对象
     * @return
     * @author leitianyu999
     */
    @Override
    public Boolean checkByMachine(Orders orders) {
        Task task = taskService.getById(orders.getTaskId());
        if (userService.checkGrade(orders.getUserId())){
            if (task.getMinConsumptionA().compareTo(new BigDecimal(String.valueOf(orders.getAmount())))==1){
                return false;
            }
            return true;
        }else {
            if (task.getMinConsumptionB().compareTo(new BigDecimal(String.valueOf(orders.getAmount())))==1){
                return false;
            }
                return true;
        }

    }


    /**
     * 跟新订单返现金额
     * @param orders
     * @return
     * @author leitianyu999
     */
    @Override
    @Transactional
    public Orders updateRebate(Orders orders) {
        //查询用户是否为会员
        Boolean aBoolean = userService.checkGrade(orders.getUserId());
        Task task = taskService.getById(orders.getTaskId());

        if (aBoolean){
            //更改为会员满减
            orders.setRebate(task.getRebateB());
        }else {
            //更改为非会员满减
            orders.setRebate(task.getRebateA());
        }
        //修改订单状态
        this.updateById(orders);
        return orders;
    }
}
