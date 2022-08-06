package com.metabubble.BWC.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.metabubble.BWC.common.CustomException;
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
            if (task.getAmount()>0) {
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
        if (byId.getAmount()>0) {
            byId.setAmount(byId.getAmount() - 1);
            if (byId.getAmount()==0){
                byId.setStatus(0);
            }
            this.updateById(byId);
        }

    }
}
