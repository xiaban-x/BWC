package com.metabubble.BWC.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
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

        }else{
            beginTime = "0000-00-00 00:00:00";
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
     * @Param chooseType 选择类型
     * @Param zfbId 支付宝id
     * @Param zfbName 支付宝名称
     * @return
     */
//提现管理
    @GetMapping("/Page")
    public R<Page> Page(int Page, int PageSize, String chooseType,String zfbId,String zfbName) {
    //分页构造器
        Page<User> pageInfo = new Page(Page,PageSize);
        Page<Cashable> page = new Page<>(Page,PageSize);
        Page<CashableDto> cashableDtoPage = new Page<>();

        QueryWrapper<User> qw0 = new QueryWrapper<>();
        QueryWrapper<Cashable> qw1 = new QueryWrapper<>();

        if (chooseType.equals("全部")){
            if(zfbId == null &&zfbName == null){
                qw1.orderByDesc("create_time");
                //执行分页查询
                cashableService.page(page,qw1);
                //联表查询
                BeanUtils.copyProperties(page,cashableDtoPage,"records");

                List<Cashable> records = page.getRecords();

                List<CashableDto> list = records.stream().map((item) -> {
                    CashableDto cashableDto = new CashableDto();

                    BeanUtils.copyProperties(item,cashableDto);

                    Long userId = item.getUserId();//分类id
                    //根据id查询分类对象
                    User user = userService.getById(userId);

                    if(user != null){
                        String aliPayId = user.getAliPayId();
                        cashableDto.setAliPayId(aliPayId);
                        String aliPayName = user.getAliPayId();
                        cashableDto.setAliPayName(aliPayName);
                    }
                    return cashableDto;
                }).collect(Collectors.toList());

                cashableDtoPage.setRecords(list);
            }

            if(zfbId != null&&zfbName == null){
                qw0.like("ali_pay_id",zfbId);
                qw0.select("id","ali_pay_Id","ali_pay_name");
                //执行分页查询
                userService.page(pageInfo,qw0);
                //联表查询
                BeanUtils.copyProperties(pageInfo,cashableDtoPage,"records");

                List<User> records = pageInfo.getRecords();

                List<CashableDto> list = records.stream().map((item) -> {
                    CashableDto cashableDto = new CashableDto();

                    BeanUtils.copyProperties(item,cashableDto);

                    Long userId = item.getId();//分类id
                    //根据id查询分类对象
                    qw1.eq("user_id",userId);
                    Cashable cashable = cashableService.getOne(qw1);

                    if(cashable != null){
                        Long id = cashable.getId();
                        cashableDto.setId(id);
                        //Long userId0 = cashable.getUserId();
                        //cashableDto.setUserId(userId0);
                        Long tradeNo = cashable.getTradeNo();
                        cashableDto.setTradeNo(tradeNo);
                        BigDecimal CA = cashable.getCashableAmount();
                        cashableDto.setCashableAmount(CA);
                        Integer payType = cashable.getPayType();
                        cashableDto.setPayType(payType);
                        Integer status = cashable.getStatus();
                        cashableDto.setStatus(status);
                        String withdrawReason = cashable.getWithdrawReason();
                        cashableDto.setWithdrawReason(withdrawReason);
                        LocalDateTime createTime = cashable.getCreateTime();
                        cashableDto.setCreateTime(createTime);
                        LocalDateTime updateTime = cashable.getUpdateTime();
                        cashableDto.setUpdateTime(updateTime);
                    }
                    return cashableDto;
                }).collect(Collectors.toList());

                cashableDtoPage.setRecords(list);

            }
            if(zfbId == null &&zfbName != null){
                qw0.like("ali_pay_name",zfbName);
                qw0.select("id","ali_pay_id","ali_pay_name");
                //执行分页查询
                userService.page(pageInfo,qw0);
                //联表查询
                BeanUtils.copyProperties(pageInfo,cashableDtoPage,"records");

                List<User> records = pageInfo.getRecords();

                List<CashableDto> list = records.stream().map((item) -> {
                    CashableDto cashableDto = new CashableDto();

                    BeanUtils.copyProperties(item,cashableDto);

                    Long userId = item.getId();//分类id
                    //根据id查询分类对象
                    qw1.eq("user_id",userId);
                    Cashable cashable = cashableService.getOne(qw1);

                    if(cashable != null){
                        Long id = cashable.getId();
                        cashableDto.setId(id);
                        Long tradeNo = cashable.getTradeNo();
                        cashableDto.setTradeNo(tradeNo);
                        BigDecimal CA = cashable.getCashableAmount();
                        cashableDto.setCashableAmount(CA);
                        Integer payType = cashable.getPayType();
                        cashableDto.setPayType(payType);
                        Integer status = cashable.getStatus();
                        cashableDto.setStatus(status);
                        String withdrawReason = cashable.getWithdrawReason();
                        cashableDto.setWithdrawReason(withdrawReason);
                        LocalDateTime createTime = cashable.getCreateTime();
                        cashableDto.setCreateTime(createTime);
                        LocalDateTime updateTime = cashable.getUpdateTime();
                        cashableDto.setUpdateTime(updateTime);
                    }
                    return cashableDto;
                }).collect(Collectors.toList());

                cashableDtoPage.setRecords(list);
            }

        }
        if (chooseType.equals("待转账")){
            if(zfbId == null &&zfbName == null){
                qw1.eq("status",1);
                qw1.orderByDesc("create_time");
                //执行分页查询
                cashableService.page(page,qw1);
                //联表查询
                BeanUtils.copyProperties(page,cashableDtoPage,"records");

                List<Cashable> records = page.getRecords();

                List<CashableDto> list = records.stream().map((item) -> {
                    CashableDto cashableDto = new CashableDto();

                    BeanUtils.copyProperties(item,cashableDto);

                    Long userId = item.getUserId();//分类id
                    //根据id查询分类对象
                    User user = userService.getById(userId);

                    if(user != null){
                        String aliPayId = user.getAliPayId();
                        cashableDto.setAliPayId(aliPayId);
                        String aliPayName = user.getAliPayId();
                        cashableDto.setAliPayName(aliPayName);
                    }
                    return cashableDto;
                }).collect(Collectors.toList());

                cashableDtoPage.setRecords(list);
            }
            if(chooseType !=null&&zfbName == null){
                qw0.like("ali_pay_id",zfbId);
                qw0.select("id","ali_pay_id","ali_name_name");
                //执行分页查询
                userService.page(pageInfo,qw0);
                //联表查询
                BeanUtils.copyProperties(pageInfo,cashableDtoPage,"records");

                List<User> records = pageInfo.getRecords();

                List<CashableDto> list = records.stream().map((item) -> {
                    CashableDto cashableDto = new CashableDto();

                    BeanUtils.copyProperties(item,cashableDto);

                    Long userId = item.getId();//分类id
                    //根据id查询分类对象
                    qw1.eq("userId",userId).and(c -> c.eq("status",1));
                    Cashable cashable = cashableService.getOne(qw1);

                    if(cashable != null){
                        Long id = cashable.getId();
                        cashableDto.setId(id);
                        Long tradeNo = cashable.getTradeNo();
                        cashableDto.setTradeNo(tradeNo);
                        BigDecimal CA = cashable.getCashableAmount();
                        cashableDto.setCashableAmount(CA);
                        Integer payType = cashable.getPayType();
                        cashableDto.setPayType(payType);
                        Integer status = cashable.getStatus();
                        cashableDto.setStatus(status);
                        String withdrawReason = cashable.getWithdrawReason();
                        cashableDto.setWithdrawReason(withdrawReason);
                        LocalDateTime createTime = cashable.getCreateTime();
                        cashableDto.setCreateTime(createTime);
                        LocalDateTime updateTime = cashable.getUpdateTime();
                        cashableDto.setUpdateTime(updateTime);
                    }
                    return cashableDto;
                }).collect(Collectors.toList());

                cashableDtoPage.setRecords(list);

            }
            if(zfbId == null &&zfbName != null){
                qw0.like("ali_pay_name",zfbName);
                qw0.select("id","ali_pay_id","ali_pay_name");
                //执行分页查询
                userService.page(pageInfo,qw0);
                //联表查询
                BeanUtils.copyProperties(pageInfo,cashableDtoPage,"records");

                List<User> records = pageInfo.getRecords();

                List<CashableDto> list = records.stream().map((item) -> {
                    CashableDto cashableDto = new CashableDto();

                    BeanUtils.copyProperties(item,cashableDto);

                    Long userId = item.getId();//分类id
                    //根据id查询分类对象
                    qw1.eq("user_id",userId).and(c -> c.eq("status",1));
                    Cashable cashable = cashableService.getOne(qw1);

                    if(cashable != null){
                        Long id = cashable.getId();
                        cashableDto.setId(id);
                        Long tradeNo = cashable.getTradeNo();
                        cashableDto.setTradeNo(tradeNo);
                        BigDecimal CA = cashable.getCashableAmount();
                        cashableDto.setCashableAmount(CA);
                        Integer payType = cashable.getPayType();
                        cashableDto.setPayType(payType);
                        Integer status = cashable.getStatus();
                        cashableDto.setStatus(status);
                        String withdrawReason = cashable.getWithdrawReason();
                        cashableDto.setWithdrawReason(withdrawReason);
                        LocalDateTime createTime = cashable.getCreateTime();
                        cashableDto.setCreateTime(createTime);
                        LocalDateTime updateTime = cashable.getUpdateTime();
                        cashableDto.setUpdateTime(updateTime);
                    }
                    return cashableDto;
                }).collect(Collectors.toList());

                cashableDtoPage.setRecords(list);
            }

        }
        if (chooseType.equals("已转账")){
            if(zfbId == null &&zfbName == null){
                qw1.eq("status",2);
                qw1.orderByDesc("create_time");
                //执行分页查询
                cashableService.page(page,qw1);
                //联表查询
                BeanUtils.copyProperties(page,cashableDtoPage,"records");

                List<Cashable> records = page.getRecords();

                List<CashableDto> list = records.stream().map((item) -> {
                    CashableDto cashableDto = new CashableDto();

                    BeanUtils.copyProperties(item,cashableDto);

                    Long userId = item.getUserId();//分类id
                    //根据id查询分类对象
                    User user = userService.getById(userId);

                    if(user != null){
                        String aliPayId = user.getAliPayId();
                        cashableDto.setAliPayId(aliPayId);
                        String aliPayName = user.getAliPayId();
                        cashableDto.setAliPayName(aliPayName);
                    }
                    return cashableDto;
                }).collect(Collectors.toList());

                cashableDtoPage.setRecords(list);
            }
            if(zfbId != null&&zfbName == null){
                qw0.like("ali_payId",zfbId);
                qw0.select("id","ali_pay_id","ali_pay_name");
                //执行分页查询
                userService.page(pageInfo,qw0);
                //联表查询
                BeanUtils.copyProperties(pageInfo,cashableDtoPage,"records");

                List<User> records = pageInfo.getRecords();

                List<CashableDto> list = records.stream().map((item) -> {
                    CashableDto cashableDto = new CashableDto();

                    BeanUtils.copyProperties(item,cashableDto);

                    Long userId = item.getId();//分类id
                    //根据id查询分类对象
                    qw1.eq("user_id",userId).and(c -> c.eq("status",2));
                    Cashable cashable = cashableService.getOne(qw1);

                    if(cashable != null){
                        Long id = cashable.getId();
                        cashableDto.setId(id);
                        Long tradeNo = cashable.getTradeNo();
                        cashableDto.setTradeNo(tradeNo);
                        BigDecimal CA = cashable.getCashableAmount();
                        cashableDto.setCashableAmount(CA);
                        Integer payType = cashable.getPayType();
                        cashableDto.setPayType(payType);
                        Integer status = cashable.getStatus();
                        cashableDto.setStatus(status);
                        String withdrawReason = cashable.getWithdrawReason();
                        cashableDto.setWithdrawReason(withdrawReason);
                        LocalDateTime createTime = cashable.getCreateTime();
                        cashableDto.setCreateTime(createTime);
                        LocalDateTime updateTime = cashable.getUpdateTime();
                        cashableDto.setUpdateTime(updateTime);
                    }
                    return cashableDto;
                }).collect(Collectors.toList());

                cashableDtoPage.setRecords(list);

            }
            if(zfbId == null &&zfbName !=null){
                qw0.like("ali_pay_name",zfbName);
                qw0.select("id","ali_pay_id","ali_pay_name");
                //执行分页查询
                userService.page(pageInfo,qw0);
                //联表查询
                BeanUtils.copyProperties(pageInfo,cashableDtoPage,"records");

                List<User> records = pageInfo.getRecords();

                List<CashableDto> list = records.stream().map((item) -> {
                    CashableDto cashableDto = new CashableDto();

                    BeanUtils.copyProperties(item,cashableDto);

                    Long userId = item.getId();//分类id
                    //根据id查询分类对象
                    qw1.eq("user_id",userId).and(c -> c.eq("status",2));
                    Cashable cashable = cashableService.getOne(qw1);

                    if(cashable != null){
                        Long id = cashable.getId();
                        cashableDto.setId(id);
                        Long tradeNo = cashable.getTradeNo();
                        cashableDto.setTradeNo(tradeNo);
                        BigDecimal CA = cashable.getCashableAmount();
                        cashableDto.setCashableAmount(CA);
                        Integer payType = cashable.getPayType();
                        cashableDto.setPayType(payType);
                        Integer status = cashable.getStatus();
                        cashableDto.setStatus(status);
                        String withdrawReason = cashable.getWithdrawReason();
                        cashableDto.setWithdrawReason(withdrawReason);
                        LocalDateTime createTime = cashable.getCreateTime();
                        cashableDto.setCreateTime(createTime);
                        LocalDateTime updateTime = cashable.getUpdateTime();
                        cashableDto.setUpdateTime(updateTime);
                    }
                    return cashableDto;
                }).collect(Collectors.toList());

                cashableDtoPage.setRecords(list);

            }

        }
        if (chooseType.equals("已退款")){
            if(zfbId == null &&zfbName == null){
                qw1.eq("status",3);
                qw1.orderByDesc("create_time");
                //执行分页查询
                cashableService.page(page,qw1);
                //联表查询
                BeanUtils.copyProperties(page,cashableDtoPage,"records");

                List<Cashable> records = page.getRecords();

                List<CashableDto> list = records.stream().map((item) -> {
                    CashableDto cashableDto = new CashableDto();

                    BeanUtils.copyProperties(item,cashableDto);

                    Long userId = item.getUserId();//分类id
                    //根据id查询分类对象
                    User user = userService.getById(userId);

                    if(user != null){
                        String aliPayId = user.getAliPayId();
                        cashableDto.setAliPayId(aliPayId);
                        String aliPayName = user.getAliPayId();
                        cashableDto.setAliPayName(aliPayName);
                    }
                    return cashableDto;
                }).collect(Collectors.toList());

                cashableDtoPage.setRecords(list);
            }
            if(zfbId !=null&&zfbName == null){
                qw0.like("ali_pay_id",zfbId);
                qw0.select("id","ali_pay_id","ali_pay_name");
                //执行分页查询
                userService.page(pageInfo,qw0);
                //联表查询
                BeanUtils.copyProperties(pageInfo,cashableDtoPage,"records");

                List<User> records = pageInfo.getRecords();

                List<CashableDto> list = records.stream().map((item) -> {
                    CashableDto cashableDto = new CashableDto();

                    BeanUtils.copyProperties(item,cashableDto);

                    Long userId = item.getId();//分类id
                    //根据id查询分类对象
                    qw1.eq("user_id",userId).and(c -> c.eq("status",3));
                    Cashable cashable = cashableService.getOne(qw1);

                    if(cashable != null){
                        Long id = cashable.getId();
                        cashableDto.setId(id);
                        Long tradeNo = cashable.getTradeNo();
                        cashableDto.setTradeNo(tradeNo);
                        BigDecimal CA = cashable.getCashableAmount();
                        cashableDto.setCashableAmount(CA);
                        Integer payType = cashable.getPayType();
                        cashableDto.setPayType(payType);
                        Integer status = cashable.getStatus();
                        cashableDto.setStatus(status);
                        String withdrawReason = cashable.getWithdrawReason();
                        cashableDto.setWithdrawReason(withdrawReason);
                        LocalDateTime createTime = cashable.getCreateTime();
                        cashableDto.setCreateTime(createTime);
                        LocalDateTime updateTime = cashable.getUpdateTime();
                        cashableDto.setUpdateTime(updateTime);
                    }
                    return cashableDto;
                }).collect(Collectors.toList());

                cashableDtoPage.setRecords(list);

            }
            if(zfbId == null &&zfbName != null){
                qw0.like("ali_pay_name",zfbName);
                qw0.select("id","ali_pay_id","ali_pay_name");
                //执行分页查询
                userService.page(pageInfo,qw0);
                //联表查询
                BeanUtils.copyProperties(pageInfo,cashableDtoPage,"records");

                List<User> records = pageInfo.getRecords();

                List<CashableDto> list = records.stream().map((item) -> {
                    CashableDto cashableDto = new CashableDto();

                    BeanUtils.copyProperties(item,cashableDto);

                    Long userId = item.getId();//分类id
                    //根据id查询分类对象
                    qw1.eq("user_id",userId).and(c -> c.eq("status",3));
                    Cashable cashable = cashableService.getOne(qw1);

                    if(cashable != null){
                        Long id = cashable.getId();
                        cashableDto.setId(id);
                        Long tradeNo = cashable.getTradeNo();
                        cashableDto.setTradeNo(tradeNo);
                        BigDecimal CA = cashable.getCashableAmount();
                        cashableDto.setCashableAmount(CA);
                        Integer payType = cashable.getPayType();
                        cashableDto.setPayType(payType);
                        Integer status = cashable.getStatus();
                        cashableDto.setStatus(status);
                        String withdrawReason = cashable.getWithdrawReason();
                        cashableDto.setWithdrawReason(withdrawReason);
                        LocalDateTime createTime = cashable.getCreateTime();
                        cashableDto.setCreateTime(createTime);
                        LocalDateTime updateTime = cashable.getUpdateTime();
                        cashableDto.setUpdateTime(updateTime);
                    }
                    return cashableDto;
                }).collect(Collectors.toList());

                cashableDtoPage.setRecords(list);
            }

        }
        return R.success(cashableDtoPage);

    }

    /**
     * 用户端提现信息增加与修改
     * author Kenlihankun
     * HttpRequest 获取session的用户id
     * @Param 获取用户填写的可提现金额
     * @Param 获取用户选择的提现方式
     * @return
     */
    //用户端提现
    @PostMapping("/Cashable")
    public R<String > cashable(HttpServletRequest request,@RequestParam("amount")BigDecimal amount,
                               @RequestParam("payType") Integer payType){
        UpdateWrapper<User> updateWrapper = new UpdateWrapper<>();

        //根据session获取用户id
        Long userId = (Long) request.getSession().getAttribute("id");
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

}
