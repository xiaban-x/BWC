package com.metabubble.BWC.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 用户
 */
@Data
public class User implements Serializable {
    //序列化
    private static final long serialVersionUID = 1L;

    //主键id
    private Long id;

    //用户id
    private Long userId;

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

    //经度
    private String lng;

    //纬度
    private String lat;

    //头像文件名
    private String avatar;

    //用户等级，0为普通用户；1为会员
    private Integer grade;

    //会员过期日期
    //格式：2021-07-15 05:16:58
    private LocalDateTime membershipExpTime;

    //创建时间
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    //更新时间
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;










}
