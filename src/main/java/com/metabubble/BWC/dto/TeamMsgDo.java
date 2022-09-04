package com.metabubble.BWC.dto;

import com.metabubble.BWC.entity.TeamMsg;
import lombok.Data;

@Data
public class TeamMsgDo extends TeamMsg {


    private Long downId;


    private String name;
}
