package com.metabubble.BWC.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Time;
import java.time.LocalDateTime;

/**
 * 订单
 */
@Data
public class Orders implements Serializable {
    //序列化
    private static final long serialVersionUID = 1L;

    //订单id
    private Long id;

    //用户id
    private Long userId;

    //商家id
    private Long merchantId;

    //商家名称
    private String merchantName;

    //商家图片
    private String merchantPic;

    //展示地址
    private String showAddress;

    //任务id
    private Long taskId;

    //平台类型：0为美团(默认)，1为饿了么
    private Integer platform;

    //任务名称
    private String taskName;

    //任务要求
    private String requirement;

    //任务备注
    private String remark;

    //订单状态，0为已下单(默认)；1为一审待审核；2为一审通过；3为一审未通过；4为二审待审核；5为二审未通过；6为已完成；7为订单取消；8为订单过期
    private Integer status;

    //审核理由
    private String reason;

    //外卖平台的订单编号
    private String orderNumber;

    //订单金额
    private BigDecimal amount;

    //用户等级，0为普通用户(默认)；1为会员
    private Integer grade;

    //最低消费
    private BigDecimal minConsumption;

    //返现金额
    private BigDecimal rebate;

    //团队返现上一级用户金额
    private BigDecimal rebate01;

    //团队返现上二级返现金额
    private BigDecimal rebate02;

    //订单截图1
    private String picOrder1;

    //订单截图2
    private String picOrder2;

    //评论截图
    private String picComment;

    //一审核人id
    private Long reviewerIdA;

    //二审核人id
    private Long reviewerIdB;

    //任务开始时间
    private LocalDateTime startTime;

    //任务结束时间
    private LocalDateTime endTime;

    //营业开始时间
    private Time businessStartTime;

    //营业结束时间
    private Time businessEndTime;

    //过期时间
    private LocalDateTime expiredTime;

    //创建时间
    @TableField(fill = FieldFill.INSERT) //插入时填充字段
    private LocalDateTime createTime;

    //更新时间
    @TableField(fill = FieldFill.INSERT_UPDATE) //插入和更新时填充字段
    private LocalDateTime updateTime;
}
