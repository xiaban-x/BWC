package com.metabubble.BWC.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 商家类型
 */
@Data
public class MerchantType implements Serializable {
    //序列化
    private static final long serialVersionUID = 1L;

    //商家类型id
    private Long id;

    //商家id
    private Long merchantId;

    //类型名称
    private String name;

    //创建时间
    @TableField(fill = FieldFill.INSERT) //插入时填充字段
    private LocalDateTime createTime;

    //更新时间
    @TableField(fill = FieldFill.INSERT_UPDATE) //插入和更新时填充字段
    private LocalDateTime updateTime;
}
