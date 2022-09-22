package com.metabubble.BWC.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.metabubble.BWC.entity.Orders;
import com.metabubble.BWC.entity.User;

import java.util.Map;


public interface UserService extends IService<User> {

    //用户订单完成返现
    public void cashback(Orders orders);
    //检查用户是否为会员
    public Boolean checkGrade(Long id);
    //检查手机号是否注册
    public Boolean findUser(String mobile);
    //检查账号是否封禁
    public Map<String,String> findStatus(String mobile);
    //检查账号是否封禁
    public Map<String,String> findStatus(Long id);






//    //团队二级成员订单返现实现
//    public void cashbackForUserFromSecond(Long id);
//    //团队一级成员订单返现实现
//    public void cashbackForUserFromFirst(Long id);
}
