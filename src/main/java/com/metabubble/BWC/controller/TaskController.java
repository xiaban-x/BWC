package com.metabubble.BWC.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.metabubble.BWC.common.R;
import com.metabubble.BWC.dto.Imp.TaskConverter;
import com.metabubble.BWC.dto.TaskDto;
import com.metabubble.BWC.entity.Task;
import com.metabubble.BWC.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/task")
public class TaskController {
    @Autowired
    TaskService taskService;

    @GetMapping(value={"/{offset}/{limit}"})
    public R<List<TaskDto>> getAll(@PathVariable Integer offset, @PathVariable Integer limit){

        Page<Task> page = new Page<>(offset, limit);
        List<Task> records = taskService.page(page).getRecords();
        List<TaskDto> taskDtos = new ArrayList<>();
        if (records != null){
            for(Task record:records){
                if (record !=null) {
                    TaskDto taskDto = TaskConverter.INSTANCES.TaskToTaskDto(record);
                    taskDtos.add(taskDto);
                }
            }
        }
        return R.success(taskDtos);
    }

    @PutMapping
    public R<String> update(@RequestBody Task task){
        boolean flag = taskService.updateById(task);
        if (flag){
            return R.success("修改成功");
        }else{
            return R.error("修改失败");
        }
    }

    @DeleteMapping("/{id}")
    public R<String> delete(@PathVariable Long id){
        boolean flag = taskService.removeById(id);
        if (flag){
            return R.success("删除成功");
        }else{
            return R.error("删除失败");
        }
    }

    @PostMapping
    public R<String> save(@RequestBody Task task){
        boolean flag = taskService.save(task);
        if (flag){
            return R.success("保存成功");
        }else{
            return R.error("保存失败");
        }
    }
}
