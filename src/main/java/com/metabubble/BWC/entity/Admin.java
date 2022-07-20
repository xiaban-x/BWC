package com.metabubble.BWC.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 管理员
 */
@Data
public class Admin implements Serializable {
    //序列化
    private static final long serialVersionUID = 1L;

    //管理员id
    private Long id;

    //管理员名称
    private String name;

    //邮箱
    private String email;

    //密码
    private String password;

    //类型，0为业务管理员，1为财务管理员，2为超级管理员
    private Integer type;

    //是否启用,0为禁用，1为启用
    private Integer status;

    //创建时间
    @TableField(fill = FieldFill.INSERT) //插入时填充字段
    private LocalDateTime createTime;

    //更新时间
    @TableField(fill = FieldFill.INSERT_UPDATE) //插入和更新时填充字段
    private LocalDateTime updateTime;

}
