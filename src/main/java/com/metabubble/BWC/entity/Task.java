package com.metabubble.BWC.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 商家任务
 */
@Data
public class Task{

    //序列化
    private static final long serialVersionUID = 1L;

    //商家id
    private Long id;

    //非会员最低消费
    private BigDecimal minConsumption_0;

    //非会员满减额
    private BigDecimal rebate_0;

    //会员最低消费
    private BigDecimal minConsumption_1;

    //会员满减额
    private BigDecimal rebate_1;

    //任务要求
    private String requirement;

    //任务备注
    private String remark;

    //是否启用，0为禁用(默认)，1为启用
    private Integer status;

    //创建时间
    @TableField(fill = FieldFill.INSERT) //插入时填充字段
    private LocalDateTime createTime;

    //更新时间
    @TableField(fill = FieldFill.INSERT_UPDATE) //插入和更新时填充字段
    private LocalDateTime updateTime;



}
