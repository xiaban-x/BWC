package com.metabubble.BWC.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.metabubble.BWC.common.BaseContext;
import com.metabubble.BWC.common.R;
import com.metabubble.BWC.dto.RechargeDto;
import com.metabubble.BWC.entity.Recharge;
import com.metabubble.BWC.entity.User;
import com.metabubble.BWC.service.RechargeService;
import com.metabubble.BWC.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/financeList")
@Slf4j
public class RechargeController {
    @Autowired
    RechargeService rechargeService;
    @Autowired
    UserService userService;

    /**
     * 充值统计
     * author Kenlihankun
     * beginTime 要查询的时间
     * type 查询条件
     * @return
     * @RequestBody map
     */
    //充值统计
    @GetMapping("/recharge_amount")
    public R<Map> recharge_amount(@RequestBody Map map) {
        String beginTime = (String) map.get("beginTime");
        String type = (String) map.get("type");

        Map<String, BigDecimal> map0 = new HashMap<>();

        QueryWrapper<Recharge> queryAmount01 = new QueryWrapper<>();
        QueryWrapper<Recharge> queryAmount02 = new QueryWrapper<>();
        QueryWrapper<Recharge> queryAll = new QueryWrapper<>();

        //充值
        if (type.equals("按天查询") && beginTime != null) {
            String beginTime02 = beginTime.substring(0, 10);
            beginTime = beginTime02;

        }
        if (type.equals("按月查询") && beginTime != null) {
            String beginTime03 = beginTime.substring(0, 7);
            beginTime = beginTime03;

        }
        if (type.equals("按年查询") && beginTime != null) {
            String beginTime04 = beginTime.substring(0, 4);
            beginTime = beginTime04;

        }
        //统计微信充值条件
        queryAmount01.likeRight("update_time", beginTime).and(c2 -> c2.eq("recharge_type", 1))
                .and(c2 -> c2.eq("status", 2));
        //统计支付宝充值条件
        queryAmount02.likeRight("update_time", beginTime).and(c3 -> c3.eq("recharge_type", 2))
                .and(c3 -> c3.eq("status", 2));

        //统计充值总金额条件
        queryAll.likeRight("update_time", beginTime).and(c1 -> c1.eq("status", 2));


        //统计微信转账金额
        queryAmount01.select("IFNULL(sum(recharge_amount),0) AS wx_all");
        Map<String, Object> map11 = rechargeService.getMap(queryAmount01);
        BigDecimal sumCount1 = (BigDecimal) map11.get("wx_all");

        //统计支付宝转账金额
        queryAmount02.select("IFNULL(sum(recharge_amount),0) AS zfb_all");
        Map<String, Object> map12 = rechargeService.getMap(queryAmount02);
        BigDecimal sumCount2 = (BigDecimal) map12.get("zfb_all");


        //统计充值总额
        queryAll.select("IFNULL(sum(recharge_amount),0) AS all0");
        Map<String, Object> map13 = rechargeService.getMap(queryAll);
        BigDecimal sumAll0 = (BigDecimal) map13.get("all0");

        map0.put("wx", sumCount1);
        map0.put("zfb", sumCount2);
        map0.put("All", sumAll0);

        return R.success(map0);
    }


    /**
     * 用户端：待充值
     * author Kenlihankun
     *
     * @return
     * @Param rechargeAmount 充值金额
     * HttpServletRequest 获取session的用户id
     */

    //待充值
    //保存userId,outTradeNo,rechargeAmount,rechargeType(默认),status(默认),createTime,UpdateTime
    @PutMapping("/recharge_msg")
    public R<Map> recharge_click(@RequestParam("rechargeAmount") BigDecimal rechargeAmount) {
        //BaseContext 获取session Id
        Long userId = BaseContext.getCurrentId();

        //Long userId = (Long) request.getSession().getAttribute("id");
        //Long userId = 1L;//测试

        Recharge recharge = new Recharge();
        UpdateWrapper<Recharge> wrapper = new UpdateWrapper<>();

        //uuid转hashcode
        UUID uuid = UUID.randomUUID();
        Integer uuidNo = uuid.toString().hashCode();
        // String.hashCode()可能会是负数，如果为负数需要转换为正数
        uuidNo = uuidNo < 0 ? -uuidNo : uuidNo;
        Long outTradeNo = Long.valueOf(String.valueOf(uuidNo));

        //插入冲值表
        recharge.setOutTradeNo(outTradeNo);
        recharge.setRechargeAmount(rechargeAmount);
        recharge.setUserId(userId);
        rechargeService.save(recharge);

        //给订单编号加上日期
        SimpleDateFormat dmDate = new SimpleDateFormat("yyyyMMdd");
        Date time1 = new Date();
        String time = dmDate.format(time1);
        Long time0 = Long.valueOf(time);
        Long outTradeNo0 = time0 * 10000000000L + outTradeNo;
        recharge.setOutTradeNo(outTradeNo0);
        wrapper.eq("out_trade_no", outTradeNo);
        rechargeService.update(recharge, wrapper);

        //返回订单号
        Map<String, Long> map = new HashMap();
        map.put("out_trade_no", outTradeNo0);
        return R.success(map);
    }

    /**
     * 用户端：充值成功以及续费成功
     * 更新充值表和用户表信息，插入选择充值会员时间
     * author Kenlihankun
     * @return
     * @Param rechargeType 选择的充值类型 0：零钱 1：微信 2：支付宝
     * @Param tradeNo 订单号
     * @Param membershipTime 选择充值的会员时间 1：月卡 2：季卡 3：年卡
     */
    //充值成功
    //
    @PutMapping("/recharge_success")
    public R<Map> recharge_confirm(@RequestParam("tradeNo") Long tradeNo,
                                   @RequestParam("membershipTime") Integer membershipTime ,
                                   @RequestParam("rechargeType") Integer rechargeType) {
        //返回会员到期时间
        Map<String, LocalDateTime> map = new HashMap<>();

        //充值成功，根据订单号更新冲值表数据
        UpdateWrapper<User> updateWrapper = new UpdateWrapper<>();
        UpdateWrapper<Recharge> wrapper = new UpdateWrapper<>();
        QueryWrapper<Recharge> queryWrapper = new QueryWrapper();
        queryWrapper.eq("out_trade_no", tradeNo);
        Recharge recharge = rechargeService.getOne(queryWrapper);
        wrapper.eq("out_trade_no", tradeNo);
        recharge.setStatus(2);
        recharge.setRechargeType(rechargeType);
        rechargeService.update(recharge, wrapper);

        //根据冲值表数据更新用户表数据
        BigDecimal rechargeAmount = recharge.getRechargeAmount();
        Long id = recharge.getUserId();
        LocalDateTime localDateTime = recharge.getUpdateTime();

        //充值成功，修改用户表数据
        User user = userService.getById(id);
        BigDecimal cashableAmount0 = user.getCashableAmount();


        if (recharge.getRechargeType().equals(0)) {//判断是否为零钱充值
            updateWrapper.eq("id", id);
            if (rechargeAmount.compareTo(cashableAmount0) < 1) {
                if (membershipTime.equals(1)) {
                    //充值会员
                    if (user.getGrade().equals(0)){
                        user.setMembershipExpTime(localDateTime.plusMonths(1L));
                    }
                    if (user.getGrade().equals(1)){
                        //续费会员
                        user.setMembershipExpTime(user.getMembershipExpTime().plusMonths(1L));

                    }
                }
                if (membershipTime.equals(2)) {
                    //充值会员
                    if (user.getGrade().equals(0)){
                        user.setMembershipExpTime(localDateTime.plusMonths(3L));
                    }
                    if (user.getGrade().equals(1)){
                        //续费会员
                        user.setMembershipExpTime(user.getMembershipExpTime().plusMonths(3L));

                    }
                }
                if (membershipTime.equals(3)) {
                    //充值会员
                    if (user.getGrade().equals(0)){
                        user.setMembershipExpTime(localDateTime.plusYears(1L));
                    }
                    if (user.getGrade().equals(1)){
                        //续费会员
                        user.setMembershipExpTime(user.getMembershipExpTime().plusYears(1L));

                    }
                }
                BigDecimal cashableAmount = user.getCashableAmount().subtract(rechargeAmount);
                user.setCashableAmount(cashableAmount);
                // 修改用户等级为会员
                user.setGrade(1);
                userService.update(user, updateWrapper);

            }
        }

        map.put("会员到期时间", user.getMembershipExpTime());

        return R.success(map);
    }

    /**
     * 用户端：充值失败
     * 更新冲值表
     * author Kenlihankun
     *
     * @return
     * @Param tradeNo 充值订单号
     */
    //充值失败
    @PutMapping("/recharge_fail")
    public R<String> recharge_cancel(@RequestParam("tradeNo") Long tradeNo) {
        UpdateWrapper<Recharge> wrapper = new UpdateWrapper<>();
        QueryWrapper<Recharge> queryWrapper = new QueryWrapper();
        queryWrapper.eq("out_trade_no", tradeNo);
        Recharge recharge = rechargeService.getOne(queryWrapper);
        wrapper.eq("out_trade_no", tradeNo);
        recharge.setStatus(3);
        rechargeService.update(recharge, wrapper);
        return R.success("success");
    }

    /**
     * 充值管理分页联表查询
     * author Kenlihankun
     * @Param Page 页码
     * @Param PageSize 条数
     * @Param chooseType 选择类型 0:全部 1：待充值 2：充值成功 3：充值失败
     * @Param beginTime 选择开始时间
     * @Param endTime 选择截至时间
     * @return
     */
    @GetMapping("/page")
    public R<Page> Page(int Page, int PageSize, Integer chooseType, String beginTime, String endTime) {
        Page<Recharge> pageInfo = new Page(Page, PageSize);
        Page<RechargeDto> rechargeDtoPage = new Page<>();
        QueryWrapper<Recharge> wrapper = new QueryWrapper<>();
        beginTime = beginTime + " 00:00:00";
        endTime = endTime + " 00:00:00";
        String finalEndTime = endTime;

        if (chooseType.equals(0)) {
            wrapper.ge("update_time", beginTime).and(c -> c.le("update_time", finalEndTime));
        }
        if(chooseType.equals(1)){
            wrapper.ge("update_time", beginTime).and(c -> c.le("update_time", finalEndTime))
                    .and(c -> c.eq("status",1));

        }
        if (chooseType.equals(2)){
            wrapper.ge("update_time", beginTime).and(c -> c.le("update_time", finalEndTime))
                    .and(c -> c.eq("status", 2));

        }
        if (chooseType.equals(3)){
            wrapper.ge("update_time", beginTime).and(c -> c.le("update_time", finalEndTime))
                    .and(c -> c.eq("status",3));
        }
        //执行分页查询
        rechargeService.page(pageInfo, wrapper);
        //联表查询
        BeanUtils.copyProperties(pageInfo, rechargeDtoPage, "records");

        List<Recharge> records = pageInfo.getRecords();

        List<RechargeDto> list = records.stream().map((item) -> {
            RechargeDto rechargeDto = new RechargeDto();

            BeanUtils.copyProperties(item, rechargeDto);

            Long userId = item.getUserId();//分类id
            //根据id查询分类对象
            User user = userService.getById(userId);

            if (user != null) {
                String name = user.getName();
                rechargeDto.setName(name);
            }
            return rechargeDto;
        }).collect(Collectors.toList());

        rechargeDtoPage.setRecords(list);
        return R.success(rechargeDtoPage);

    }
}
