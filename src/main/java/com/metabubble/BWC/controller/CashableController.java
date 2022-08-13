package com.metabubble.BWC.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.metabubble.BWC.common.BaseContext;
import com.metabubble.BWC.common.R;
import com.metabubble.BWC.dto.CDto;
import com.metabubble.BWC.dto.CashableDto;
import com.metabubble.BWC.entity.Cashable;
import com.metabubble.BWC.entity.Config;
import com.metabubble.BWC.entity.Team;
import com.metabubble.BWC.entity.User;
import com.metabubble.BWC.service.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Pattern;


@RestController
@RequestMapping("/financeList")
@Slf4j
public class CashableController {
    @Autowired CashableService cashableService;
    @Autowired UserService userService;
    @Autowired LogsService logsService;
    @Autowired TeamService teamService;
    @Autowired ConfigService configService;

    /**
     * 提现统计查询
     * author Kenlihankun
     * @RequestBody map
     * beginTime 选择的查询时间
     * type 选择的查询类型
     * @return
     */
    @GetMapping("/cashableAmount")
    public R<CDto> cashable_amount(@RequestParam("Type") Integer type, @RequestParam("BeginTime") String beginTime) {

        QueryWrapper<Cashable> queryWrapper = new QueryWrapper<>();
        QueryWrapper<Cashable> qw = new QueryWrapper<>();

        if (type.equals(1) && beginTime!=null) {
            String beginTime02 = beginTime.substring(0, 10);
            beginTime = beginTime02;

        }
        if (type.equals(2) && beginTime!=null) {
            String beginTime03 = beginTime.substring(0, 7);
            beginTime = beginTime03;

        }
        if (type.equals(3)  && beginTime!=null) {
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

        CDto cDto = new CDto();
        cDto.setAllAmount(sumCount);
        cDto.setCashableTimes(count_0);
        cDto.setWaitingTimes(count_1);

        return R.success(cDto);
    }



    /**
     * 提现管理：分页联表查询
     * author Kenlihankun
     * @Param Page 页码
     * @Param PageSize 条数
     * @Param chooseType 选择类型 0:全部 1：待转账 2：已转账 3：已退款
     * @Param zfbId 支付宝id
     * @Param zfbName 支付宝name
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
     * @Param amount 获取用户填写的可提现金额
     * @Param  payType 获取用户选择的提现方式 1:支付宝(默认) 2:微信
     * @Param chooseType 0为用户个人零钱提现 1为团队零钱提现
     * @return
     */


    @Transactional
    //用户端提现
    @PostMapping("/Cashable")
    public R<String > cashable(@RequestParam("amount")BigDecimal amount,
                               @RequestParam("payType") Integer payType,
                               @RequestParam("chooseType") Integer chooseType){
        UpdateWrapper<User> updateWrapper = new UpdateWrapper<>();
        UpdateWrapper<Team> teamWrapper = new UpdateWrapper<>();

        //BaseContext 获取session Id
        Long userId = BaseContext.getCurrentId();
        //Long userId = 1L;//测试

  /**     最低和最高提现额度
        Integer i;
        BigDecimal b;
        Config config = configService.getById();
        String content = config.getContent();
   //判断是否为整数
        Pattern pattern2 = Pattern.compile("[0-9]*");

   //判断是否为double
        Pattern pattern1 = Pattern.compile("[0-9]+[.]{0,1}[0-9]*[dD]{0,1}");
        if (pattern2.matcher(content).matches() == false && pattern1.matcher(content).matches() == false){
            content = "0";
            BigDecimal CT = new BigDecimal(content);
            b = CT;
        }
        if (pattern2.matcher(content).matches() == true && pattern1.matcher(content).matches() == false){
            i = Integer.valueOf(content);
            if (i<0){
                i = 0;
            }
            BigDecimal CT = new BigDecimal(i);
            b = CT;
        }
        if (pattern2.matcher(content).matches() == false && pattern1.matcher(content).matches() == true){
            Double c = new Double(content);
            content = String.format("%.2f", c);
            if(content<0){
                content=0;
            }
            BigDecimal Ct = new BigDecimal(content);
            b = CT;
        }
        */

        //提现金额大于0
        BigDecimal zero = new BigDecimal(0);

        if (chooseType.equals(0)){
            //申请提现金额和可提现金额对比
            User user = userService.getById(userId);
            BigDecimal cashableAmount = user.getCashableAmount();
            //大于等于最低提现金额和小于等于最高提现金额，输入的提现金额大于0
            if (amount.compareTo(cashableAmount)<1 && amount.compareTo(zero)==1){
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
        }

        if (chooseType.equals(1)){
            //申请提现金额和可提现金额对比
            Team team = teamService.getById(userId);
            BigDecimal cashableAmount = team.getTotalWithdrawnAmount();
            //大于等于最低提现金额和小于等于最高提现金额，输入的提现金额大于0
            if (amount.compareTo(cashableAmount)<1 && amount.compareTo(zero)==1){
                //更新团队表提现金额
                BigDecimal cashableAmount1 = cashableAmount.subtract(amount);
                teamWrapper.eq("id",userId);
                team.setTotalWithdrawnAmount(cashableAmount1);
                teamService.update(team, teamWrapper);

                //将数据插入提现表
                Cashable cashable = new Cashable();
                cashable.setUserId(userId);
                cashable.setCashableAmount(amount);
                cashable.setPayType(payType);
                cashable.setChooseType(chooseType);
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
        }
        return R.success("success");
    }

    /**
     * 管理端的提现退款
     * author Kenlihankun
     * @Param withdrawReason 获取退款原因
     * cashableDto 接收提现订单的id：id，提现金额：amount
     * @return
     */


    @Transactional
    @RequestMapping("/withdraw")
    public R<String> withdraw(CashableDto cashableDto,@RequestParam("withdrawReason") String withDrawReason){
        UpdateWrapper<Cashable> wrapper = new UpdateWrapper<>();
        UpdateWrapper<User> userUpdateWrapper = new UpdateWrapper<>();
        UpdateWrapper<Team> teamUpdateWrapper = new UpdateWrapper<>();

        //更新提现表
        wrapper.eq("id",cashableDto.getId());
        Cashable cashable = cashableService.getById(cashableDto.getId());

        //判断是否为待转账状态
        if (cashable.getStatus().equals(1)){

            cashable.setWithdrawReason(withDrawReason);
            cashable.setStatus(3);
            cashableService.update(cashable,wrapper);

            User user = userService.getById(cashable.getUserId());
            Team team = teamService.getById(cashable.getUserId());

            if (cashable.getChooseType().equals(0)){
                //更新用户表
                userUpdateWrapper.eq("id",cashable.getUserId());
                BigDecimal beforeAmount = user.getCashableAmount();
                BigDecimal afterAmount = beforeAmount.add(cashableDto.getCashableAmount());
                user.setCashableAmount(afterAmount);
                userService.update(user,userUpdateWrapper);
            }
            if (cashable.getChooseType().equals(1)){
                //更新团队表
                teamUpdateWrapper.eq("id",cashable.getUserId());
                BigDecimal beforeAmount = team.getTotalWithdrawnAmount();
                BigDecimal afterAmount = beforeAmount.add(cashableDto.getCashableAmount());
                team.setTotalWithdrawnAmount(afterAmount);
                teamService.update(team,teamUpdateWrapper);
            }


            //日志
            String title = "订单审核";
            String content = user.getName()+"订单审核不通过";
            logsService.saveLog(title,content);

        }


        return R.success("success");
    }
    /**
     * 管理端的提现转账
     * author Kenlihankun
     * cashableDto 接收提现订单的id
     * @return
     */


    @Transactional
    @PutMapping("/cashable_success")
    public R<String> cashable_success(CashableDto cashableDto){
        UpdateWrapper<Cashable> wrapper = new UpdateWrapper<>();

        //更新提现表
        wrapper.eq("id",cashableDto.getId());
        Cashable cashable = cashableService.getById(cashableDto.getId());

        //判断是否为待转账状态
        if (cashable.getStatus().equals(1)){
            cashable.setStatus(2);
            cashableService.update(cashable,wrapper);

            User user = userService.getById(cashable.getUserId());

            //日志
            String title = "订单审核";
            String content = user.getName()+"订单审核通过";
            logsService.saveLog(title,content);

        }



        return R.success("success");
    }

}