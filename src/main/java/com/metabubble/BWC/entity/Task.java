package com.metabubble.BWC.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
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

    //任务id
    @TableId(type= IdType.AUTO)
    private Long id;

    //商家id
    private Long merchantId;

    //非会员最低消费
    private BigDecimal minConsumptionA;

    //非会员满减额
    private BigDecimal rebateA;

    //会员最低消费
    private BigDecimal minConsumptionB;

    //会员满减额
    private BigDecimal rebateB;

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
