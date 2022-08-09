package com.metabubble.BWC.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CDto {

    //提现次数
    private BigDecimal cashableTimes;

    //提现金额
    private BigDecimal allAmount;

    //待提现次数
    private BigDecimal waitingTimes;

}
