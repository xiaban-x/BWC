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

import java.sql.Time;
import java.time.*;
import java.util.Calendar;
import java.util.Date;
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
     * @author leitianyu999
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
     * @param task 任务对象
     * @author leitianyu999
     * @return
     */
    @Override
    public Boolean checkOrders(Long userId, Task task) {
        LocalDate now = LocalDate.now();
        LocalTime time1 = LocalTime.of(0, 0,0);
        LocalTime time2 = LocalTime.of(23, 59,59);


        AtomicInteger atomicInteger = new AtomicInteger(task.getTimeInterval());
        int i = atomicInteger.decrementAndGet();

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH,-i);
        Date time = cal.getTime();
        LocalDate localDate = time.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDateTime begin = LocalDateTime.of(localDate, time1);
        LocalDateTime end = LocalDateTime.of(now, time2);

        LambdaQueryWrapper<Orders> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Orders::getUserId,userId);
        queryWrapper.eq(Orders::getTaskId,task.getId());
        queryWrapper.ge(Orders::getCreateTime,begin);
        queryWrapper.le(Orders::getCreateTime,end);

        int count = ordersService.count(queryWrapper);
        if (count!=0){
            return false;
        }else {
            return true;
        }
    }


    /**
     * 完成任务后任务完成数量加一
     * @author leitianyu999
     * @param orders
     */
    @Override
    public void addCompleted(Orders orders) {
        Task task = this.getById(orders.getTaskId());
        AtomicInteger atomicInteger = new AtomicInteger(task.getCompleted());
        int addAndGet = atomicInteger.addAndGet(1);
        task.setCompleted(addAndGet);
        this.updateById(task);
    }

    /**
     * 检查任务是否在可接取时间
     * @param task
     * @author leitianyu999
     * @return
     */
    @Override
    public Boolean checkTime(Task task) {
        LocalDateTime startTime = task.getStartTime();
        LocalDateTime endTime = task.getEndTime();
        LocalDateTime now = LocalDateTime.now();
        boolean after = now.isAfter(startTime);
        boolean before = now.isBefore(endTime);
        if (after&&before){
            return false;
        }
        return true;
    }

    /**
     * 检查任务是否在营业时间
     * @param task
     * @author leitianyu999
     * @return
     */
    @Override
    public Boolean checkBusinessTime(Task task) {
        Time businessStartTime = task.getBusinessStartTime();
        Time businessEndTime = task.getBusinessEndTime();
        Time now = Time.valueOf(LocalTime.now());
        boolean after = now.after(businessStartTime);
        boolean before = now.before(businessEndTime);
        if (after&&before){
            return false;
        }
        return true;
    }
}
