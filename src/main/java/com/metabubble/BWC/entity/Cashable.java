package com.metabubble.BWC.entity;


import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 提现管理
 */
@Data
public class Cashable {
    //序列化
    private static final long serialVersionUID = 1L;

    //提现编号
    private Long id;

    //用户id
    private Long userId;

    //用户id
    private Long adminId;

    //转账订单号
    private Long tradeNo;

    //主钱包
    private BigDecimal mainWallet;

    //副钱包
    private BigDecimal viceWallet;

    //提现金额
    private BigDecimal cashableAmount;

    //提现类型 1,支付宝(默认) 2，微信
    private Integer payType;

    //提现状态 1，待转账(默认) 2，已转账 3,已取消
    private Integer status;

    //退款原因
    private String withdrawReason;

    //申请时间
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    //转账、退款时间
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

}
