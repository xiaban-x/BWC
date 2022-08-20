package com.metabubble.BWC.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * user的dto
 */
@Data
public class UserDto {


    //序列化
    private static final long serialVersionUID = 1L;

    //主键id
    private Long id;

    //下级邀请码
    private String downId;

    //用户名
    private String name;

    //可提现金额
    private BigDecimal cashableAmount;

    //已提现金额
    private BigDecimal withdrawnAmount;

    //已节省金额
    private BigDecimal savedAmount;

    //微信号
    private String wxId;

    //手机号
    private String tel;

    //支付宝名称
    private String aliPayName;

    //头像文件名
    private String avatar;

    //用户等级，0为普通用户；1为会员
    private Integer grade;

    //会员过期日期
    //格式：2021-07-15 05:16:58
    private LocalDateTime membershipExpTime;
}
