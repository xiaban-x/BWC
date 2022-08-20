package com.metabubble.BWC.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Cooperation {
    //序列化
    private static final long serialVersionUID = 1L;

    // 渠道合作id
    private Long id;

    // 意向代理城市
    private String city;

    // 预计投入资金
    private String funds;

    // 团队规模
    private String scale;

    // 姓名
    private String name;

    // 微信号
    private String wx;

    // 手机号
    private String tel;

    //创建时间
    @TableField(fill = FieldFill.INSERT) //插入时填充字段
    private LocalDateTime createTime;

    //更新时间
    @TableField(fill = FieldFill.INSERT_UPDATE) //插入和更新时填充字段
    private LocalDateTime updateTime;

}
