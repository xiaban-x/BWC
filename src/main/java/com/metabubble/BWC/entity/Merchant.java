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
 * 商家
 */
@Data
public class Merchant implements Serializable {
    //序列化
    private static final long serialVersionUID = 1L;

    //商家id
    private Long id;

    //商家名称
    private String name;

    //商家电话
    private String tel;

    //商家图片
    private String pic;

    //任务类型：0为早餐(默认)，1为午餐，2为下午茶，3为宵夜
    private Integer type;

    //平台类型：0为美团（默认）;1为饿了么
    private Integer platform;

    //地址
    private String address;

    //展示地址
    private String showAddress;

    //店铺链接
    private String link;

    //店铺备注
    private String note;

    //店铺黑名单
    private String blacklist;

    //经度
    private BigDecimal lng;

    //纬度
    private BigDecimal lat;

    //创建时间
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    //更新时间
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;



}
