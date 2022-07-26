package com.metabubble.BWC.dto.Imp;

import com.metabubble.BWC.dto.TaskDto;
import com.metabubble.BWC.entity.Task;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;


/**
 * merchant类型转换
 */
@Mapper(componentModel = "spring")
public abstract class TaskConverter {

    public static TaskConverter INSTANCES = Mappers.getMapper(TaskConverter.class);

    /**
     * 进行entity转换dto
     * @param task
     * @return
     */
    public abstract TaskDto TaskToTaskDto(Task task);
}