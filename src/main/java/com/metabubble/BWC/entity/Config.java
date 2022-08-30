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

    //配置详细内容
    private String contents;

    //配置类型，0为系统配置(默认)，1为用户端首页轮播图，2为公告信息，3为隐私政策，4为用户协议，5为关于我们，6为微信客服，7为QQ客服，8为客服邮箱，9为客服电话
    private Integer type;

    //是否启用,0为禁用，1为启用(默认)
    private Integer status;

}
