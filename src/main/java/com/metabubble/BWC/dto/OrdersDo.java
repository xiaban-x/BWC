package com.metabubble.BWC.dto;

import com.metabubble.BWC.entity.Orders;
import lombok.Data;

@Data
public class OrdersDo extends Orders {
    //电话号
    public String tel;

}
