package com.metabubble.BWC.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 充值管理
 */
@Data
public class Recharge {

    //序列化
    private static final long serialVersionUID = 1L;

    //充值编号
    private Integer id;

    //用户id
    private Long userId;

    //订单号
    private Long outTradeNo;

    //充值金额
    private BigDecimal rechargeAmount;

    //状态 1待充值(默认)，2充值成功，3充值失败
    private Integer status;

    //创建时间
    private LocalDateTime createTime;

    //支付时间
    private LocalDateTime updateTime;
}
