package com.metabubble.BWC.common;

import lombok.Data;

import java.io.Serializable;

@Data
public class Condition implements Serializable {
    private String name;
    private String tel;
    private Integer plaType = null;
}
