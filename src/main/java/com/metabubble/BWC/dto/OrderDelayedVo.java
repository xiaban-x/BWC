package com.metabubble.BWC.dto;


import com.metabubble.BWC.entity.Orders;

import lombok.Data;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;


@Data
public class OrderDelayedVo implements Delayed , Serializable {





    private static final long serialVersionUID = 1;
    private static final long warnTime = 300000L;

    private String type;
    private Orders order;// 订单号
    private String expTime;// 订单过期时间

    /**
     * compareTo    用于延时队列内部比较排序：当前订单的过期时间 与 队列中对象的过期时间 比较
     */
    @Override
    public int compareTo(Delayed o) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        long nowThreadtime = 0;
        long queueThreadtime = 0;
        try {
            nowThreadtime = formatter.parse(this.expTime).getTime();
            queueThreadtime = formatter.parse(((OrderDelayedVo)o).expTime).getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return Long.compare(nowThreadtime, queueThreadtime);
    }

    /**
     * 时间单位：秒
     * getDelay   延迟关闭时间 = 过期时间 - 当前时间
     */
    @Override
    public long getDelay(TimeUnit unit) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        long time = 0;
        try {
            time = formatter.parse(this.expTime).getTime();

        } catch (ParseException e) {
            e.printStackTrace();
        }
        return time - System.currentTimeMillis() - warnTime;
    }
}
