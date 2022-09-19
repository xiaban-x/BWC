package com.metabubble.BWC.dto;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class OrdersDto {

    //订单id
    private Long id;

    //用户id
    private Long userId;

    //商家id
    private Long merchantId;

    //商家名称
    private String merchantName;

    //展示地址
    private String showAddress;

    //商家图片
    private String merchantPic;

    //平台类型：0为美团(默认)，1为饿了么
    private Integer platform;

    //任务id
    private Long taskId;

    //任务名称
    private String taskName;

    //订单状态，0为已下单(默认)；1为一审待审核；2为一审通过；3为一审未通过；4为二审待审核；5为二审未通过；6为已完成；7为订单取消；8为订单过期
    private Integer status;

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

    //审核理由
    private String reason;

    //订单截图1
    private String picOrder1;

    //订单截图2
    private String picOrder2;

    //评论截图
    private String picComment;

    //任务要求
    private String requirement;

    //任务备注
    private String remark;

    //创建时间
    private LocalDateTime createTime;

    //过期时间
    private LocalDateTime expiredTime;

}
