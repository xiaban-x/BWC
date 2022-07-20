package com.metabubble.BWC.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 客服服务
 */
@Data
public class Cusservice implements Serializable {
    //序列化
    private static final long serialVersionUID = 1L;

    //客服id
    private Long id;

    //二维码
    private String qrCode;

    //账号
    private String number;

    //客服类型,0为微信，1为QQ
    private Integer type;

    //创建时间
    @TableField(fill = FieldFill.INSERT) //插入时填充字段
    private LocalDateTime createTime;

    //更新时间
    @TableField(fill = FieldFill.INSERT_UPDATE) //插入和更新时填充字段
    private LocalDateTime updateTime;

}
