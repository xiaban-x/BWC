package com.metabubble.BWC.dto;

import com.metabubble.BWC.entity.Team;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class TeamDto extends Team {
    //团队所有成员
    private int allMember;

    //昨日收益
    private BigDecimal yesterdayWithdrawn;

}
