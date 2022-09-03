package com.metabubble.BWC.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class TaskDetailDto {
    //任务id
    private Long id;

    //任务名称
    private String name;

    //商家名称
    private String merchantName;

    //商家地址
    private String merchantAddress;

    //平台类型：0为美团(默认)，1为饿了么
    private Integer platform;

    //商家照片
    private String merchantPic;

    //用户与商家距离
    private BigDecimal userToMerchantDistance;

    //任务剩余数量
    private Integer taskLeft;

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

    //任务链接
    private String link;

}
