package com.metabubble.BWC.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class RechargeDto {
    //用户id
    private Long userId;

    //用户名
    private String name;

    //序列化
    private static final long serialVersionUID = 1L;

    //充值编号
    private Integer id;

    //充值金额
    private BigDecimal rechargeAmount;

    //状态 1待充值(默认)，2充值成功，3充值失败
    private Integer status;

    //创建时间
    private LocalDateTime createTime;


}
