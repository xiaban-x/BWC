package com.metabubble.BWC.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.metabubble.BWC.common.BaseContext;
import com.metabubble.BWC.common.R;
import com.metabubble.BWC.dto.CashableDto;
import com.metabubble.BWC.entity.Cashable;
import com.metabubble.BWC.entity.User;
import com.metabubble.BWC.service.CashableService;
import com.metabubble.BWC.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/financeList")
@Slf4j
public class CashableController {
    @Autowired CashableService cashableService;
    @Autowired UserService userService;

    /**
     * 提现统计查询
     * author Kenlihankun
     * @RequestBody map
     * beginTime 选择的查询时间
     * type 选择的查询类型
     * @return
     */
    @GetMapping("/cashableAmount")
    public R<Map> cashable_amount(@RequestBody Map map) {
        String beginTime = (String) map.get("beginTime");
        String type = (String) map.get("type");

        Map<String, BigDecimal> map0 = new HashMap<>();
        QueryWrapper<Cashable> queryWrapper = new QueryWrapper<>();
        QueryWrapper<Cashable> qw = new QueryWrapper<>();

        if (type.equals("按天查询") && beginTime!=null) {
            String beginTime02 = beginTime.substring(0, 10);
            beginTime = beginTime02;

        }
        if (type.equals("按月查询") && beginTime!=null) {
            String beginTime03 = beginTime.substring(0, 7);
            beginTime = beginTime03;

        }
        if (type.equals("按年查询")  && beginTime!=null) {
            String beginTime04 = beginTime.substring(0, 4);
            beginTime = beginTime04;

        }
        queryWrapper.likeRight("update_time", beginTime).and(c -> c.eq("status", 2));

        //统计待转账次数条件
        qw.likeRight("create_time", beginTime).and(c1 -> c1.eq("status", 1));




        //统计提现次数
        queryWrapper.select("COUNT(*) as cashable_times");
        Map<String, Object> map02 = cashableService.getMap(queryWrapper);
        int times = Integer.valueOf(String.valueOf(map02.get("cashable_times")));
        BigDecimal count_0 = BigDecimal.valueOf(times);

        //提现累计金额
        queryWrapper.select("IFNULL(sum(cashable_amount),0) as total");
        Map<String, Object> map01 = cashableService.getMap(queryWrapper);
        BigDecimal sumCount = (BigDecimal) map01.get("total");

        //统计待转账次数
        Integer count1 = cashableService.count(qw);
        BigDecimal count_1 = new BigDecimal( Integer.parseInt ( count1.toString() ) );


        map0.put("yearAmount",sumCount);
        map0.put("cashable_times",count_0);
        map0.put("waiting_times",count_1);
        return R.success(map0);
    }



    /**
     * 提现管理：分页联表查询
     * author Kenlihankun
     * @Param Page 页码
     * @Param PageSize 条数
     * @Param chooseType 选择类型 0:全部 1：待转账 2：已转账 3：已退款
     * @Param zfbId 支付宝id
     * @Param zfbName 支付宝名称
     * @return
     */
//提现管理
    @GetMapping("/Page")
    public R<IPage> Page(Integer Page, Integer PageSize, Integer chooseType,String zfbId,String zfbName) {
        //分页构造器
        QueryWrapper<Object> wrapper = new QueryWrapper<>();
        //if(chooseType.equals(0)){
        //wrapper.and(c -> {c.eq("cashable.status",1);});
        //}
        if (chooseType.equals(1) || chooseType.equals(2) || chooseType.equals(3)) {
            wrapper.and(c -> {c.eq("cashable.status",chooseType);});

        }
        if (zfbId != null) {
            wrapper.and(c -> {c.like("user.ali_pay_id",zfbId);});
        }
        if (zfbName != null) {
            wrapper.and(c -> {c.like("user.ali_pay_name",zfbName);});
        }
        Page<CashableDto> cashableDtoPage = new Page<>(Page,PageSize);
        IPage<CashableDto> userPage = cashableService.select(cashableDtoPage,wrapper);
        return  R.success(userPage);


    }

    /**
     * 用户端提现信息增加与修改
     * author Kenlihankun
     * HttpRequest 获取session的用户id
     * @Param amount 获取用户填写的可提现金额
     * @Param  payType 获取用户选择的提现方式 1:支付宝(默认) 2:微信
     * @return
     */


    //用户端提现
    @PostMapping("/Cashable")
    public R<String > cashable(@RequestParam("amount")BigDecimal amount,
                               @RequestParam("payType") Integer payType){
        UpdateWrapper<User> updateWrapper = new UpdateWrapper<>();

        //BaseContext 获取session Id
        Long userId = BaseContext.getCurrentId();

        //根据session获取用户id
        //Long userId = (Long) request.getSession().getAttribute("id");
        //测试：Long userId = 1L;

        //申请提现金额和可提现金额对比
        User user = userService.getById(userId);
        BigDecimal cashableAmount = user.getCashableAmount();
        if (amount.compareTo(cashableAmount)<1){
            //更新用户表提现金额
            BigDecimal cashableAmount1 = cashableAmount.subtract(amount);
            updateWrapper.eq("id",userId);
            user.setCashableAmount(cashableAmount1);
            userService.update(user, updateWrapper);

            //将数据插入提现表
            Cashable cashable = new Cashable();
            cashable.setUserId(userId);
            cashable.setCashableAmount(amount);
            cashable.setPayType(payType);
            UUID uuid = UUID.randomUUID();
            Integer uuidNo = uuid.toString().hashCode();

            // String.hashCode()可能会是负数，如果为负数需要转换为正数
            uuidNo = uuidNo < 0 ? -uuidNo : uuidNo;
            Long tradeNo = Long.valueOf(String.valueOf(uuidNo));
            cashable.setTradeNo(tradeNo);
            cashableService.save(cashable);

            //给订单号加上日期
            UpdateWrapper<Cashable> wrapper = new UpdateWrapper<>();
            wrapper.eq("id",cashable.getId());
            SimpleDateFormat dmDate = new SimpleDateFormat("yyyyMMdd");
            Date time1 =new Date();
            String time = dmDate.format(time1);
            Long time0 = Long.valueOf(time);
            Long TradeNo = time0*10000000000L+tradeNo;
            cashable.setTradeNo(TradeNo);
            cashableService.update(cashable,wrapper);

        }else {
            log.info("提现金额不足");
        }

        return R.success("success");
    }

    /**
     * 管理端的提现退款
     * author Kenlihankun
     * @Param withdrawReason 获取退款原因
     * cashableDto 接收必要参数
     * @return
     */


    @RequestMapping("/withdraw")
    public R<String> withdraw(CashableDto cashableDto,@RequestParam("withdrawReason") String withDrawReason){
        UpdateWrapper<Cashable> wrapper = new UpdateWrapper<>();
        UpdateWrapper<User> userUpdateWrapper = new UpdateWrapper<>();

        //更新提现表
        wrapper.eq("id",cashableDto.getId());
        Cashable cashable = cashableService.getById(cashableDto.getId());
        cashable.setWithdrawReason(withDrawReason);
        cashable.setStatus(3);
        cashableService.update(cashable,wrapper);

        //更新用户表
        userUpdateWrapper.eq("id",cashable.getUserId());
        User user = userService.getById(cashable.getUserId());
        BigDecimal beforeAmount = user.getCashableAmount();
        BigDecimal afterAmount = beforeAmount.add(cashableDto.getCashableAmount());
        user.setCashableAmount(afterAmount);
        userService.update(user,userUpdateWrapper);

        return R.success("success");
    }

}