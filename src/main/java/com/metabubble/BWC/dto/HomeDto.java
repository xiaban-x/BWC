package com.metabubble.BWC.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class HomeDto {
    //任务id
    private Long id;

    //商家id
    private Long merchantId;

    //商家名字
    private String merchantName;

    //任务名称
    private String name;

    //用户到商家的距离
    private BigDecimal userToMerchantDistance;

    //平台类型：0为美团(默认)，1为饿了么
    private Integer platform;

    //商家图片
    private String merchantPic;

    //任务剩余量
    private Integer taskLeft;

    //非会员最低消费
    private BigDecimal minConsumptionA;

    //非会员满减额
    private BigDecimal rebateA;

    //会员最低消费
    private BigDecimal minConsumptionB;

    //会员满减额
    private BigDecimal rebateB;

}
