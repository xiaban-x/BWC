package com.metabubble.BWC.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.metabubble.BWC.entity.Task;
import com.metabubble.BWC.mapper.TaskMapper;
import com.metabubble.BWC.service.TaskService;
import org.springframework.stereotype.Service;

@Service
public class TaskServiceImpl extends ServiceImpl<TaskMapper, Task>
        implements TaskService {

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
            return true;
        }
        if (task.getStatus()==0){
            return false;
        }
        return null;
    }
}
