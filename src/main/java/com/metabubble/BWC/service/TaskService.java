package com.metabubble.BWC.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.metabubble.BWC.entity.Task;

public interface TaskService extends IService<Task> {

    //查询任务是否生效
    public Boolean checkTaskStatus(Long id);
    //接取任务后任务数量减一
    public void updateAmount(Long id);
    //查询用户当天是否接取过同一任务
    public Boolean checkOrders(Long userId,Long taskId);
}
