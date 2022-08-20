package com.metabubble.BWC.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 商家入驻
 */
@Data
public class Recruitment {
    //序列化
    private static final long serialVersionUID = 1L;

    // 商家入驻id
    private Long id;

    // 店主名称
    private String bossName;

    // 微信号
    private String wx;

    // 手机号
    private String tel;

    // 店铺名称
    private String shopName;

    // 所在城市
    private String city;

    // 详细地址
    private String address;

    //创建时间
    @TableField(fill = FieldFill.INSERT) //插入时填充字段
    private LocalDateTime createTime;

    //更新时间
    @TableField(fill = FieldFill.INSERT_UPDATE) //插入和更新时填充字段
    private LocalDateTime updateTime;
}
