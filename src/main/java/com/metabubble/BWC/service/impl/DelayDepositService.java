package com.metabubble.BWC.service.impl;

import com.metabubble.BWC.common.CustomException;
import com.metabubble.BWC.common.R;
import com.metabubble.BWC.dto.OrderDelayedVo;
import com.metabubble.BWC.entity.Orders;
import com.metabubble.BWC.entity.User;
import com.metabubble.BWC.service.OrdersService;
import com.metabubble.BWC.service.UserService;
import com.metabubble.BWC.utils.SMSUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.concurrent.DelayQueue;

/**
 * 延迟订单
 */
@Service
@Slf4j
public class DelayDepositService {

    private final DelayQueue<OrderDelayedVo> delayQueue = new DelayQueue<>();

    private final OrdersService ordersService;
    private final UserService userService;

    public DelayDepositService(OrdersService ordersService, UserService userService) {
        this.ordersService = ordersService;
        this.userService = userService;
    }


    /**
     * 添加订单到DelayQueue
     */
    public void save(Orders order , String expTime, String type) {
        OrderDelayedVo delayedVo = new OrderDelayedVo();
        delayedVo.setExpTime(expTime);
        delayedVo.setOrder(order);
        delayQueue.put(delayedVo);
        log.info("订单【超时时间:{}毫秒】被推入延时队列,订单详情：{}", expTime, order);
    }


    /**
     * 异步线程处理DelayQueue
     */
    class DepositTask implements Runnable {
        @Override
        public void run() {
            try {
                //noinspection InfiniteLoopStatement
                while (true) {
                    OrderDelayedVo delayedVo = delayQueue.take();
                    Orders orders = delayedVo.getOrder();
                    Orders ordersById = ordersService.getById(orders.getId());
                    //判断数据库中订单是否未支付
                    if(ordersById.getStatus().equals(orders.getStatus())&&ordersById.getExpiredTime().equals(orders.getExpiredTime())){
                        warmMsg(ordersById,delayedVo.getType());
                        log.info("订单短信提醒:order={}", ordersById);
                    }else {
                        log.info("订单已处理:order={}", ordersById);
                    }
                    log.info(orders.toString());
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 启动异步线程
     */
    @PostConstruct
    public void init() {
        new Thread(new DepositTask()).start();
    }



    private void warmMsg(Orders orders, String type){
        User user = userService.getById(orders.getUserId());
        String modelCode;
        switch (type) {
            case "一二审":// 发送注册的短信验证码
                modelCode = "SMS_267115517";
                break;

            case "初审":// 发送重置登录密码的短信验证码
                modelCode = "SMS_267115517";
                break;

            default:
                throw new CustomException("发送订单提醒信息有误");
        }

        Boolean msg = SMSUtils.sendMessage("饭团霸王餐",modelCode,user.getTel(),"");
        if (!msg) {
            throw new CustomException("发送订单提醒短信失败");
        }
    }


}
