package com.metabubble.BWC.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 商家任务
 */
@Data
public class TaskDto {

    //任务id
    private Long id;

    //商家id
    private Long merchantId;

    //非会员最低消费
    private BigDecimal minConsumptionA;

    //非会员满减额
    private BigDecimal rebateA;

    //会员最低消费
    private BigDecimal minConsumptionB;

    //会员满减额
    private BigDecimal rebateB;

    //任务要求
    private String requirement;

    //任务备注
    private String remark;

    //是否启用，0为禁用(默认)，1为启用
    private Integer status;
}