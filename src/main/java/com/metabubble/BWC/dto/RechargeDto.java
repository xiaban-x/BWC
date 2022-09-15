package com.metabubble.BWC.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class RechargeDto {
    //用户id
    private Long userId;

    //手机号
    private String tel;

    //用户名
    private String name;

    //充值方式 0零钱（默认） 1后台  2支付宝
    private Integer rechargeType;

    //序列化
    private static final long serialVersionUID = 1L;

    //充值编号
    private Integer id;

    //充值金额
    private BigDecimal rechargeAmount;

    //充值天数
    private Integer days;

    //会员过期日期
    //格式：2021-07-15 05:16:58
    private LocalDateTime membershipExpTime;

    //状态 1待充值(默认)，2充值成功，3充值失败
    private Integer status;

    //创建时间
    private LocalDateTime createTime;


}
