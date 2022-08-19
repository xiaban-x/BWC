package com.metabubble.BWC.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.metabubble.BWC.common.CustomException;
import com.metabubble.BWC.entity.Orders;
import com.metabubble.BWC.entity.Task;
import com.metabubble.BWC.mapper.TaskMapper;
import com.metabubble.BWC.service.OrdersService;
import com.metabubble.BWC.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.xml.crypto.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class TaskServiceImpl extends ServiceImpl<TaskMapper, Task>
        implements TaskService {

    @Autowired
    private OrdersService ordersService;

    /**
     * 查询任务是否有效
     * @param id
     * @return
     * @author leitianyu999
     */
    @Override
    public Boolean checkTaskStatus(Long id) {
        Task task = this.getById(id);
        if (task.getStatus()==1){
            if (task.getTaskLeft()>0) {
                return true;
            }else {
                task.setStatus(0);
                this.updateById(task);
                return false;
            }
        }
        if (task.getStatus()==0){
            return false;
        }
        throw new CustomException("任务出错");
    }

    /**
     * 任务数量减一
     * @param id
     */
    @Override
    public void updateAmount(Long id) {
        Task byId = this.getById(id);
        if (byId.getTaskLeft()>0) {
            byId.setTaskLeft(byId.getTaskLeft() - 1);
            if (byId.getTaskLeft()==0){
                byId.setStatus(0);
            }
            this.updateById(byId);
        }

    }

    /**
     * 查询用户当天是否接取过同一任务
     * @param userId 用户id
     * @param taskId 任务id
     * @return
     */
    @Override
    public Boolean checkOrders(Long userId, Long taskId) {
        LocalDate now = LocalDate.now();
        LocalTime time1 = LocalTime.of(0, 0,0);
        LocalTime time2 = LocalTime.of(23, 59,59);
        LocalDateTime begin = LocalDateTime.of(now, time1);
        LocalDateTime end = LocalDateTime.of(now, time2);

        LambdaQueryWrapper<Orders> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Orders::getUserId,userId);
        queryWrapper.eq(Orders::getTaskId,taskId);
        queryWrapper.ge(Orders::getCreateTime,begin);
        queryWrapper.le(Orders::getCreateTime,end);

        int count = ordersService.count(queryWrapper);
        if (count!=0){
            return false;
        }else {
            return true;
        }
    }

    @Override
    public void addCompleted(Orders orders) {
        Task task = this.getById(orders.getTaskId());
        AtomicInteger atomicInteger = new AtomicInteger(task.getCompleted());
        int addAndGet = atomicInteger.addAndGet(1);
        task.setCompleted(addAndGet);
        this.updateById(task);
    }
}
