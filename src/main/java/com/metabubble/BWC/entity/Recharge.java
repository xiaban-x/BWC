package com.metabubble.BWC.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 充值管理
 */
@Data
public class Recharge implements Serializable {

    //序列化
    private static final long serialVersionUID = 1L;

    //充值编号
    private Integer id;

    //用户id
    private Long userId;

    //充值方式 0零钱（默认） 1微信 2支付宝
    private Integer rechargeType;

    //订单号
    private Long outTradeNo;

    //充值金额
    private BigDecimal rechargeAmount;

    //状态 1待充值(默认)，2充值成功，3充值失败
    private Integer status;

    //创建时间
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    //支付时间
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
