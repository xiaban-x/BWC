package com.metabubble.BWC.dto.Imp;

import com.metabubble.BWC.dto.HomeDto;
import com.metabubble.BWC.dto.TaskDetailDto;
import com.metabubble.BWC.dto.TaskDto;
import com.metabubble.BWC.entity.Task;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;


/**
 * task类型转换
 */
@Mapper(componentModel = "spring")
public abstract class TaskDetailConverter {

    public static com.metabubble.BWC.dto.Imp.TaskDetailConverter INSTANCES = Mappers.getMapper(com.metabubble.BWC.dto.Imp.TaskDetailConverter.class);

    /**
     * 进行entity转换dto
     * @param task
     * @return
     */
    public abstract TaskDetailDto TaskToTaskDetailDto(Task task);
}
