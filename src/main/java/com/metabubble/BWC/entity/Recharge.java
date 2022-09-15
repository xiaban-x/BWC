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

    //充值方式 0零钱（默认） 1后台  2支付宝
    private Integer rechargeType;

    //订单号
    private Long outTradeNo;

    //选择充值的会员时间 0：其他（默认） 1：月卡 2：季卡 3：年卡
    private Long membershipTime;

    //充值金额
    private BigDecimal rechargeAmount;

    //充值天数
    private Integer days;

    //状态 1充值成功，2充值失败
    private Integer status;

    //创建时间
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

}
