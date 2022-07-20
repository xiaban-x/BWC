package com.metabubble.BWC.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 团队
 */
@Data
public class Team implements Serializable {
    //序列化
    private static final long serialVersionUID = 1L;

    //团队id
    private Long id;

    //一级团长id
    private Long user01Id;

    //二级团长id
    private Long user02Id;

    //成员id
    private Long userId;

    //全部消费金额
    private BigDecimal totalAmount;

    //全部可提现金额
    private BigDecimal totalWithdrawnAmount;

    //创建时间
    @TableField(fill = FieldFill.INSERT) //插入时填充字段
    private LocalDateTime createTime;

    //更新时间
    @TableField(fill = FieldFill.INSERT_UPDATE) //插入和更新时填充字段
    private LocalDateTime updateTime;
}
