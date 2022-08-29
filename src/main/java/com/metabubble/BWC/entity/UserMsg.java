package com.metabubble.BWC.entity;


import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户信息
 */
@Data
public class UserMsg {
    //序列化
    private static final long serialVersionUID = 1L;

    //主键id
    private Long id;

    //用户/团队id
    private Long userId;

    //状态：//状态：0是用户提现类,1是充值信息,2.用户任务返现类
    private int type;

    //团队信息
    private String msg;

    //创建时间
    @TableField(fill = FieldFill.INSERT) //插入时填充字段
    private LocalDateTime createTime;
}
