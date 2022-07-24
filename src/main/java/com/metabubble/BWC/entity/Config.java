package com.metabubble.BWC.entity;

import lombok.Data;

import java.io.Serializable;

/**
 * 配置
 */
@Data
public class Config implements Serializable {
    //序列化
    private static final long serialVersionUID = 1L;

    //配置id
    private Long id;

    //配置名称
    private String name;

    //配置内容
    private String content;

    //配置类型，1为用户端首页轮播图(默认)，2为首页品类，3为活动，4为背景图，5为公告信息
    private Integer type;

    //是否启用,0为禁用，1为启用(默认)
    private Integer status;

}
