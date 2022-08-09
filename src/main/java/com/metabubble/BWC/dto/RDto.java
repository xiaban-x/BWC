package com.metabubble.BWC.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class RDto {

    //微信充值金额
    private BigDecimal wxAmount;

    //支付宝充值金额
    private BigDecimal zfbAmount;

    //全部充值金额
    private BigDecimal allAmount;

}
