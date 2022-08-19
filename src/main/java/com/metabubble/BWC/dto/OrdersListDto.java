package com.metabubble.BWC.dto;


import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class OrdersListDto {

    //订单id
    private Long id;

    //订单状态，0为已下单(默认)；1为一审待审核；2为一审通过；3为一审未通过；4为二审待审核；5为二审未通过；6为已完成；7为订单取消；8为订单过期
    private Integer status;

    //商家名称
    private String merchantName;

    //商家图片
    private String pic;

    //任务名称
    private String taskName;

    //用户等级，0为普通用户(默认)；1为会员
    private Integer grade;

    //最低消费
    private BigDecimal minConsumption;

    //返现金额
    private BigDecimal rebate;

    //创建时间
    private LocalDateTime createTime;
}
