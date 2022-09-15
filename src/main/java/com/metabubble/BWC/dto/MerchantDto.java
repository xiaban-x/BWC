package com.metabubble.BWC.dto;

import lombok.Data;

import java.time.LocalDateTime;


/**
 * Merchant的Dto
 */
@Data
public class MerchantDto {

    //商家id
    private Long id;

    //商家名称
    private String name;

    //商家电话
    private String tel;

    //商家图片
    private String pic;

    //任务类型：0为早餐(默认)，1为午餐，2为下午茶，3为宵夜
    private Integer type;

    //平台类型：0为美团（默认）;1为饿了么
    private Integer platform;

    //地址
    private String address;

    //展示地址
    private String showAddress;

    //店铺链接
    private String link;

    //店铺备注
    private String note;

    //店铺黑名单
    private String blacklist;

    //商家创建时间
    private LocalDateTime createTime;
}
