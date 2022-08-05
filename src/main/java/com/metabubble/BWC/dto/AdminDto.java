package com.metabubble.BWC.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * admin的Dto
 * author cclucky
 */
@Data
public class AdminDto {

    //序列化
    private static final long serialVersionUID = 1L;

    //主键id
    private Long id;

    // 管理员名称 name
    private String name;

    // 管理员邮箱 email
    private String email;

    // 管理员类型 type
    private Integer type;

    // 管理员状态 status
    private Integer status;

    // 管理员添加时间 createTime
    private LocalDateTime createTime;


}
