package com.metabubble.BWC.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.metabubble.BWC.common.CustomException;
import com.metabubble.BWC.entity.Admin;
import com.metabubble.BWC.entity.Orders;
import com.metabubble.BWC.entity.Task;
import com.metabubble.BWC.entity.User;
import com.metabubble.BWC.mapper.AdminMapper;
import com.metabubble.BWC.mapper.UserMapper;
import com.metabubble.BWC.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
        implements UserService {


    @Autowired
    private TaskService taskService;
    @Autowired
    private TeamService teamService;
    @Autowired
    private OrdersService ordersService;
    @Autowired
    private UserMsgService userMsgService;


    /**
     * 用户完成订单返现
     * @param orders 订单
     * @author leitianyu999
     */
    @Override
    @Transactional
    public void cashback(Orders orders) {
        //获取用户id
        Long userId = orders.getUserId();
        //获取返现金额
        BigDecimal rebate = orders.getRebate();
        User user = this.getById(userId);
        //返现金额加入
        user.setCashableAmount(user.getCashableAmount().add(rebate));
        //节约金额加入
        user.setSavedAmount(user.getSavedAmount().add(rebate));
        //添加返现信息
        userMsgService.addUserCashback(orders);
        this.updateById(user);
    }

    /**
     * 查询用户会员是否过期并更改
     * @param id 用户id
     * @return
     * @author leitianyu999
     */
    @Override
    @Transactional
    public Boolean checkGrade(Long id) {
        User byId = this.getById(id);
        if (byId.getMembershipExpTime()!=null) {
            //判断是否过期
            boolean after = byId.getMembershipExpTime().isAfter(LocalDateTime.now());
            //更改会员等级为0
            if (!after){

                if (byId.getGrade()==1) {
                    //更改会员等级为0
                    byId.setGrade(0);
                    this.updateById(byId);
                }
            }

            if (after){
                if (byId.getGrade()==0) {
                    //更改会员等级为1
                    byId.setGrade(1);
                    this.updateById(byId);
                }
            }
            return after;
        }else {
            if (byId.getGrade()==1){
                //更改会员等级为0
                byId.setGrade(0);
                this.updateById(byId);
            }
            return false;
        }

    }

    /**
     * 邀请码生成
     * @author leitianyu999
     * @return
     */
    @Override
    public String createUUID() {
        String s = UUID.randomUUID().toString();
        return s;
    }


    /**
     * 检查手机号是否注册
     * @param mobile
     * @author leitianyu999
     * @return
     */
    @Override
    public Boolean findUser(String mobile) {
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getTel,mobile);
        List<User> list = this.list(queryWrapper);
        if (list!=null&&list.size()==1){
            return true;
        }else if (list.size()>1){
            throw new CustomException("手机号注册多名用户");
        }else {
            return false;
        }
    }

    /**
     * 检查账号是否封禁
     * @param mobile
     * @author leitianyu999
     * @return
     */
    @Override
    public Map<String,String> findStatus(String mobile) {
        Map<String,String> map = new HashMap<>();

        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getTel,mobile);
        List<User> list = this.list(queryWrapper);
        if (list!=null&&list.size()==1){
            if (list.get(0).getStatus()==1){
                map.put("ban",list.get(0).getReason());
                return map;
            }
            map.put("normal","right");
            return map;
        }else if (list.size()>1){
            throw new CustomException("手机号注册多名用户");
        }else {
            return null;
        }


    }


    /**
     * 检查账号是否封禁
     * @param id
     * @author leitianyu999
     * @return
     */
    @Override
    public Map<String, String> findStatus(Long id) {
        Map<String,String> map = new HashMap<>();

        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getId,id);
        List<User> list = this.list(queryWrapper);
        if (list!=null&&list.size()==1){
            if (list.get(0).getStatus()==1){
                map.put("ban",list.get(0).getReason());
                return map;
            }
            map.put("normal","right");
            return map;
        }else if (list.size()>1){
            throw new CustomException("手机号注册多名用户");
        }else {
            return null;
        }
    }


//    @Override
//    public void cashbackForUserFromSecond(Long id) {
//        User user = this.getById(id);
//        BigDecimal bigDecimal = BigDecimal.valueOf(0.2);
//        user.setCashableAmount(user.getCashableAmount().add(bigDecimal));
//    }
//
//    @Override
//    public void cashbackForUserFromFirst(Long id) {
//        User user = this.getById(id);
//        BigDecimal bigDecimal = BigDecimal.valueOf(0.5);
//        user.setCashableAmount(user.getCashableAmount().add(bigDecimal));
//    }
}
