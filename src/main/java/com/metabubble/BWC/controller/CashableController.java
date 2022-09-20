package com.metabubble.BWC.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.metabubble.BWC.common.BaseContext;
import com.metabubble.BWC.common.R;
import com.metabubble.BWC.dto.CDto;
import com.metabubble.BWC.dto.CashableDto;
import com.metabubble.BWC.entity.*;
import com.metabubble.BWC.service.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collector;
import java.util.stream.Collectors;


@RestController
@RequestMapping("/financeList")
@Slf4j
public class CashableController {
    @Autowired CashableService cashableService;
    @Autowired UserService userService;
    @Autowired HttpServletRequest request;
    @Autowired AdminService adminService;
    @Autowired TeamService teamService;
    @Autowired ConfigService configService;
    @Autowired TeamMsgService teamMsgService;
    @Autowired LogsService logsService;
    @Autowired UserMsgService userMsgService;

    /**
     * 提现统计查询
     * author Kenlihankun
     * beginTime 选择的查询时间
     * type 选择的查询类型 1为按天查询查询 2为按月查询 3为按年查询
     * @return
     */
    @GetMapping("/getCashableCount")
    public R<CDto> cashable_amount(@RequestParam("Type") Integer type, @RequestParam("BeginTime") String beginTime) {

        QueryWrapper<Cashable> queryWrapper = new QueryWrapper<>();
        QueryWrapper<Cashable> qw = new QueryWrapper<>();
        CDto cDto = new CDto();
        if (beginTime.length()>9){
            if (type.equals(1) ) {
                String beginTime02 = beginTime.substring(0, 10);
                beginTime = beginTime02;

            }
            else if (type.equals(2) ) {
                String beginTime03 = beginTime.substring(0, 7);
                beginTime = beginTime03;

            }
            else if (type.equals(3)) {
                String beginTime04 = beginTime.substring(0, 4);
                beginTime = beginTime04;

            }else {
                beginTime = "0000-00-00 00:00:00";
            }
            //累计提现金额
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

            cDto.setAllAmount(sumCount);
            cDto.setCashableTimes(count_0);
            cDto.setWaitingTimes(count_1);
        }else {
            cDto.setAllAmount(new BigDecimal(0));
            cDto.setCashableTimes(new BigDecimal(0));
            cDto.setWaitingTimes(new BigDecimal(0));
        }
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
    @GetMapping("/getCashablePageInfo")
    public R<IPage> Page(Integer Page, Integer PageSize, Integer chooseType,String zfbId,String tel
    ,String beginTime,String endTime) {
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
        if (tel != null) {
            wrapper.and(c -> {c.like("user.tel",tel);});
        }
        if (beginTime != null) {
            wrapper.and(c -> {c.ge("cashable.create_time",beginTime);});
        }
        if (endTime != null) {
            wrapper.and(c -> {c.le("cashable.create_time",endTime);});
        }


        //取出所有用户的所有记录
        Page<CashableDto> cashableDtoPage = new Page<>(Page,PageSize);
        IPage<CashableDto> userPage = cashableService.select(cashableDtoPage,wrapper);


        Set<Long> set = new HashSet<>();
        List<CashableDto> records = userPage.getRecords();
        for (CashableDto record : records) {
            Long userId = record.getUserId();
            set.add(userId);
        }
        Map<Long,BigDecimal> list0 = new HashMap<>();
        for (Long aLong : set) {
            User user = userService.getById(aLong);

            QueryWrapper<Team> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("user_id",aLong);
            Team team = teamService.getOne(queryWrapper);

            BigDecimal total = team.getTotalWithdrawnAmount().add(user.getCashableAmount());
            list0.put(aLong,total);
        }

        for (Long a: list0.keySet()
             )
        {System.out.println(a); }

        List<CashableDto> list = records.stream().map((item) -> {
            CashableDto cashableDto = new CashableDto();
            BeanUtils.copyProperties(item, cashableDto);
            for (Long key:list0.keySet()
                 ) {
                if (cashableDto.getUserId().equals(key)){
                    cashableDto.setCurrentAmount(list0.get(key));
                }else{
                    continue;
                }
            }
            return cashableDto;
        }).collect(Collectors.toList());

        userPage.setRecords(list);


        return  R.success(userPage);


    }


    /**
     * 用户端提现信息增加与修改
     * author Kenlihankun
     * @Param amount 获取用户填写的可提现金额
     * @Param  payType 获取用户选择的提现方式 1:支付宝(默认)
     * @return
     */


    @Transactional
    //用户端提现
    @PostMapping("/insertCashableInfo")
    public R<String > cashable(@RequestParam("amount")BigDecimal amount,
                               @RequestParam("payType") Integer payType){
        UpdateWrapper<User> updateWrapper = new UpdateWrapper<>();
        UpdateWrapper<Team> teamWrapper = new UpdateWrapper<>();
        QueryWrapper<Cashable> queryWrapper = new QueryWrapper<>();
        QueryWrapper<Team> teamQueryWrapper = new QueryWrapper<>();

        //BaseContext 获取session Id
        Long userId = BaseContext.getCurrentId();

        BigDecimal minA = new BigDecimal(0);
        BigDecimal maxA = new BigDecimal(1000);
        BigDecimal maxTimes = new BigDecimal(20);

        //最大提现次数
        Config config = configService.getById(16);
        String content = config.getContent();
        //最低提现金额
        Config config1 = configService.getById(11);
        String content1 = config1.getContent();
        //最大提现金额
        Config config2 = configService.getById(12);
        String content2 = config2.getContent();

        //判断是否为正整数或者正小数
        Pattern pattern = Pattern.compile("[0-9]*\\.?[0-9]+");
        boolean isTrue1 = pattern.matcher(content1).matches();
        boolean isTrue2 = pattern.matcher(content2).matches();
        boolean isTrue = pattern.matcher(content).matches();
        if (isTrue1){
            minA = new BigDecimal(content1);
        }
        if (isTrue2){
            maxA = new BigDecimal(content2);
        }
        if (isTrue){
            maxTimes = new BigDecimal(content);
        }

        //提现金额大于0
        BigDecimal zero = new BigDecimal(0);

        //获取user主钱包金额
        User user = userService.getById(userId);
        BigDecimal cashableAmount = user.getCashableAmount();

        //获取用户当天的提现次数
        LocalDateTime dateTime = LocalDateTime.now();
        String timeNow = DateTimeFormatter.ofPattern("yyyy-MM-dd").format(dateTime);
        queryWrapper.eq("user_id",userId).and(c -> c.like("create_time",timeNow));
        Integer count1 = cashableService.count(queryWrapper);
        BigDecimal count_1 = new BigDecimal( Integer.parseInt ( count1.toString() ) );

        //获取team钱包金额
        teamQueryWrapper.eq("user_id",userId);
        Team team = teamService.getOne(teamQueryWrapper);
        BigDecimal totalWithdrawAmount = team.getTotalWithdrawnAmount();

        //计算总金额
        BigDecimal total = cashableAmount.add(totalWithdrawAmount);

        //判断传入参数是否正确
        if (payType.equals(1)){
            //判断是否超过当天最大提现次数
            if (count_1.compareTo(maxTimes)<1){
                if (amount.compareTo(cashableAmount)<1 && minA.compareTo(amount)<1 && amount.compareTo(zero)==1
                        &&amount.compareTo(maxA)<1 && maxA.compareTo(minA)==1){
                    //更新用户表提现金额
                    BigDecimal cashableAmount1 = cashableAmount.subtract(amount);
                    updateWrapper.eq("id",userId);
                    user.setCashableAmount(cashableAmount1);
                    userService.update(user, updateWrapper);

                    //将数据插入提现表
                    Cashable cashable = new Cashable();
                    cashable.setMainWallet(amount);
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

                    //插入数据到userMsg
                    String userMsg = "发起提现请求,扣除"+amount+"元";
                    userMsgService.addWithdrawals(userId,userMsg);

                }
                //主钱包不够提现金额
                else if (amount.compareTo(cashableAmount)==1 && minA.compareTo(amount)<1 && amount.compareTo(maxA)<1
                        && amount.compareTo(total)<1 && maxA.compareTo(minA)==1 && amount.compareTo(zero)==1){
                    BigDecimal cashableAmount1 = amount.subtract(cashableAmount);

                    //更新用户表提现金额
                    updateWrapper.eq("id",userId);
                    user.setCashableAmount(zero);
                    userService.update(user, updateWrapper);

                    //更新团队表
                    BigDecimal totalWithdrawAmount1 = totalWithdrawAmount.subtract(cashableAmount1);
                    teamWrapper.eq("user_id",userId);
                    team.setTotalWithdrawnAmount(totalWithdrawAmount1);
                    teamService.update(team,teamWrapper);

                    //插入数据到team_msg
                    String teamMsg = user.getName()+"申请了提现，并且从团队钱包扣除了"+cashableAmount1+"元";
                    teamMsgService.addWithdrawals(userId,teamMsg);

                    //将数据插入提现表
                    Cashable cashable = new Cashable();
                    cashable.setMainWallet(cashableAmount);
                    cashable.setViceWallet(cashableAmount1);
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

                    //插入数据到userMsg
                    String userMsg = "发起提现请求,扣除"+amount+"元";
                    userMsgService.addWithdrawals(userId,userMsg);
                }else {
                    return  R.error("提现金额不满足条件");
                }

            }else {
                return  R.error("超过当天最大提现次数");
            }
        }else {
            return R.error("参数不符合条件");
        }

        return R.success("success");
    }

    /**
     * 管理端的提现退款
     * author Kenlihankun
     * @Param withdrawReason 获取退款原因
     * cashableDto 接收提现订单的id：id
     * @return
     */


    @Transactional
    @PostMapping("/updateWithdrawInfo")
    public R<String> withdraw(CashableDto cashableDto,@RequestParam("withdrawReason") String withDrawReason){
        UpdateWrapper<Cashable> wrapper = new UpdateWrapper<>();
        UpdateWrapper<User> userUpdateWrapper = new UpdateWrapper<>();
        UpdateWrapper<Team> teamUpdateWrapper = new UpdateWrapper<>();
        //更新提现表
        wrapper.eq("id",cashableDto.getId());
        Cashable cashable = cashableService.getById(cashableDto.getId());

        //日志 判断管理员是否有权限
        Long adminId = (Long) request.getSession().getAttribute("admin");
        Admin adminServiceById = adminService.getById(adminId);
        if (adminServiceById.getType().equals(0) || adminServiceById.getType().equals(1)) {

            //判断参数是否正确
            if (cashable!=null){
                //判断是否为待转账状态
                if (cashable.getStatus().equals(1)){
                    cashable.setAdminId(adminId);
                    cashable.setWithdrawReason(withDrawReason);
                    cashable.setStatus(3);
                    cashableService.update(cashable, wrapper);

                    User user = userService.getById(cashable.getUserId());

                    //更新用户表
                    //将申请的金额退回主钱包
                    userUpdateWrapper.eq("id", cashable.getUserId());
                    BigDecimal beforeAmount = user.getCashableAmount();
                    BigDecimal afterAmount = beforeAmount.add(cashable.getMainWallet());
                    user.setCashableAmount(afterAmount);
                    userService.update(user, userUpdateWrapper);

                    //更新团队表
                    //将金额退回副钱包
                    BigDecimal zero = new BigDecimal(0);
                    if (cashable.getViceWallet().compareTo(zero) == 1) {
                        teamUpdateWrapper.eq("user_id", cashable.getUserId());
                        Team team = teamService.getOne(teamUpdateWrapper);
                        team.setTotalWithdrawnAmount(team.getTotalWithdrawnAmount().add(cashable.getViceWallet()));
                        teamService.update(team, teamUpdateWrapper);

                        //插入数据到team_msg
                        String teamMsg = user.getName() + "提现申请被取消，并且返还团队钱包" + cashable.getViceWallet() + "元";
                        teamMsgService.addWithdrawals(cashable.getUserId(), teamMsg);
                    }

                    //日志
                    String title = "订单审核";
                    String content = user.getName() + "提现订单审核不通过";
                    Logs log = new Logs();
                    log.setAdminId(adminId);
                    log.setAdminName(adminServiceById.getName());
                    log.setName(title);
                    log.setContent(content);
                    logsService.save(log);

                    //插入数据到userMsg
                    String amount = "提现请求不通过,返回"+cashable.getCashableAmount()+"元";
                    userMsgService.addWithdrawals(cashable.getUserId(),amount);


                }else {
                    return R.error("不是待转账状态") ;
                }

            }else {
                return R.error("参数不正确");
            }
        }else {
            return R.error("无权限");
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
    @PutMapping("/updateAgreeInfo")
    public R<String> cashable_success(CashableDto cashableDto){
        UpdateWrapper<Cashable> wrapper = new UpdateWrapper<>();

        //更新提现表
        wrapper.eq("id",cashableDto.getId());
        Cashable cashable = cashableService.getById(cashableDto.getId());

        //判断管理员是否有权限
        Long adminId = (Long) request.getSession().getAttribute("admin");
        Admin adminServiceById = adminService.getById(adminId);
        if (adminServiceById.getType().equals(0) || adminServiceById.getType().equals(1)){

            //判断参数是否正确
            if (cashable!=null){
                //判断是否为待转账状态
                if (cashable.getStatus().equals(1)){

                    cashable.setAdminId(adminId);
                    cashable.setStatus(2);
                    cashableService.update(cashable,wrapper);

                    User user = userService.getById(cashable.getUserId());

                    //日志
                    String title = "订单审核";
                    String content = user.getName()+"提现订单审核通过";
                    Logs log = new Logs();
                    log.setAdminId(adminId);
                    log.setAdminName(adminServiceById.getName());
                    log.setName(title);
                    log.setContent(content);
                    logsService.save(log);

                }else {
                    return R.error("不是待转账状态");
                }
            }else {
                return R.error("参数不正确") ;
            }
        }else {
            return R.error("无权限") ;
        }


        return R.success("success");
    }

}