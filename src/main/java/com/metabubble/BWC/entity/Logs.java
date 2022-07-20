package com.metabubble.BWC.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 日志
 */
@Data
public class Logs {

    //日志id
    private Long id;

    //管理员id
    private Long adminId;

    //标题
    private String name;

    //内容
    private String content;

    //创建时间
    @TableField(fill = FieldFill.INSERT) //插入时填充字段
    private LocalDateTime createTime;






}
