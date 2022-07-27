package com.metabubble.BWC.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单
 */
@Data
public class Orders implements Serializable {
    //序列化
    private static final long serialVersionUID = 1L;

    //订单id
    private Long id;

    //用户id
    private Long userId;

    //商家id
    private Long merchantId;

    //任务id
    private Long taskId;

    //订单状态，0为已下单(默认)；1为一审待审核；2为一审通过；3为一审未通过；4为二审待审核；5为二审未通过；6为已完成；7为订单取消
    private Integer status;

    //外卖平台的订单编号
    private String orderNumber;

    //订单金额
    private BigDecimal amount;

    //返现金额
    private BigDecimal rebate;

    //审核理由
    private String reason;

    //订单截图
    private String picOrder;

    //评论截图
    private String picComment;

    //审核人id
    private Long reviewerId;

    //创建时间
    @TableField(fill = FieldFill.INSERT) //插入时填充字段
    private LocalDateTime createTime;

    //更新时间
    @TableField(fill = FieldFill.INSERT_UPDATE) //插入和更新时填充字段
    private LocalDateTime updateTime;
}
