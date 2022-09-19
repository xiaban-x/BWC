package com.metabubble.BWC.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 商家任务
 */
@Data
public class Task implements Serializable {

    //序列化
    private static final long serialVersionUID = 1L;

    //任务id
    @TableId(type= IdType.AUTO)
    private Long id;

    //商家id
    private Long merchantId;

    //任务名称
    private String name;

    //任务类型：0为早餐(默认)，1为午餐，2为下午茶，3为宵夜
    private Integer type;

    //平台类型：0为美团(默认)，1为饿了么
    private Integer platform;

    //任务是否需要评价：0为需要(默认)，1为不需要
    private Integer comment;

    //任务数量
    private Integer amount;

    //已完成任务数量
    private Integer completed;

    //任务剩余量
    private Integer taskLeft;

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

    //接取任务时间间隔
    private Integer timeInterval;

    //是否启用，0为禁用(默认)，1为启用
    private Integer status;

    //任务开始时间
    private LocalDateTime startTime;

    //任务结束时间
    private LocalDateTime endTime;

    //营业时间
    private LocalDateTime businessTime;

    //创建时间
    @TableField(fill = FieldFill.INSERT) //插入时填充字段
    private LocalDateTime createTime;

    //更新时间
    @TableField(fill = FieldFill.INSERT_UPDATE) //插入和更新时填充字段
    private LocalDateTime updateTime;
}
