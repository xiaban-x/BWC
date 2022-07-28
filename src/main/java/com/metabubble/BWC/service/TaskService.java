package com.metabubble.BWC.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.metabubble.BWC.entity.Task;

public interface TaskService extends IService<Task> {

    //查询任务是否生效
    public Boolean checkTaskStatus(Long id);

}
