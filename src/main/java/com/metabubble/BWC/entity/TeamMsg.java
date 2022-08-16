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

    //状态：0是团队返现类(默认)，1是添加上下级类,2是用户提现类
    private int type;

    //电话号
    private String downPhone;

    //团队信息
    private String msg;

    //创建时间
    @TableField(fill = FieldFill.INSERT) //插入时填充字段
    private LocalDateTime createTime;
}
