package com.metabubble.BWC.dto.Imp;

import com.metabubble.BWC.dto.TaskDetailDto;
import com.metabubble.BWC.dto.TeamDto;
import com.metabubble.BWC.dto.TeamMsgDo;
import com.metabubble.BWC.entity.Task;
import com.metabubble.BWC.entity.Team;
import com.metabubble.BWC.entity.TeamMsg;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * team类型转换
 */
@Mapper(componentModel = "spring")
public abstract class TeamConverter {


    public static com.metabubble.BWC.dto.Imp.TeamConverter INSTANCES = Mappers.getMapper(com.metabubble.BWC.dto.Imp.TeamConverter.class);

    /**
     * 进行entity转换dto
     * @param team
     * @return
     */
    public abstract TeamDto TeamToTeamDto(Team team);



    public abstract TeamMsgDo TeamMsgToTeamMsgDo(TeamMsg teamMsg);
}
