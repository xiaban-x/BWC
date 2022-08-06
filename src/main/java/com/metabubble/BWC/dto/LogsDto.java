package com.metabubble.BWC.dto;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * logs Dto
 * 暂时废弃
 */
@Data
public class LogsDto {

    //日志id
    private Long id;

    //管理员名称
    private String adminName;

    //标题
    private String name;

    //内容
    private String content;

    //创建时间
    private LocalDateTime createTime;
}
