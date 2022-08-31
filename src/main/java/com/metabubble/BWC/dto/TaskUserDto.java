package com.metabubble.BWC.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TaskUserDto {

    //主键id
    private Long id;

    //用户名
    private String name;

    //手机号
    private String tel;

    //订单状态，0为已下单(默认)；1为一审待审核；2为一审通过；3为一审未通过；4为二审待审核；5为二审未通过；6为已完成；7为订单取消；8为订单过期
    private Integer status;

    //下单时间
    private LocalDateTime createTime;
}
