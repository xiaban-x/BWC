package com.metabubble.BWC.dto;

import com.metabubble.BWC.entity.Orders;
import com.metabubble.BWC.entity.Task;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class OrdersDo extends Orders {
    //电话号
    public String tel;

    //用户名
    private String name;

    //任务对象
    public Task task;

    //展示地址
    private String showAddress;
}
