package com.metabubble.BWC.entity;


import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 团队信息
 */
@Data
public class TeamMsg {
    //序列化
    private static final long serialVersionUID = 1L;

    //主键id
    private Long id;

    //用户/团队id
    private Long userId;

    //团队信息
    private String information;

    //创建时间
    @TableField(fill = FieldFill.INSERT) //插入时填充字段
    private LocalDateTime createTime;
}
