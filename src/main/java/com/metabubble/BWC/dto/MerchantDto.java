package com.metabubble.BWC.dto;

import lombok.Data;


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

    //地址
    private String address;
}
