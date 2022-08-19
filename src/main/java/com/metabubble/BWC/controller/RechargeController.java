package com.metabubble.BWC.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.metabubble.BWC.common.BaseContext;
import com.metabubble.BWC.common.R;
import com.metabubble.BWC.dto.RDto;
import com.metabubble.BWC.dto.RechargeDto;
import com.metabubble.BWC.dto.TDto;
import com.metabubble.BWC.entity.*;
import com.metabubble.BWC.service.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/financeList")
@Slf4j
public class RechargeController {
    @Autowired
    RechargeService rechargeService;
    @Autowired
    UserService userService;
    @Autowired
    TeamService teamService;
    @Autowired
    ConfigService configService;
    @Autowired
    TeamMsgService teamMsgService;

    /**
     * 充值统计
     * author Kenlihankun
     * beginTime 要查询的时间
     * type 查询条件
     * @return
     * @RequestBody map
     */
    //充值统计
    @GetMapping("/getRechargeCount")
    public R<RDto> recharge_amount(@RequestParam("Type") Integer type, @RequestParam("BeginTime") String beginTime) {


        QueryWrapper<Recharge> queryAmount01 = new QueryWrapper<>();
        QueryWrapper<Recharge> queryAmount02 = new QueryWrapper<>();
        QueryWrapper<Recharge> queryAll = new QueryWrapper<>();

        //充值
        if (type.equals(1) && beginTime != null) {
            String beginTime02 = beginTime.substring(0, 10);
            beginTime = beginTime02;

        }
        if (type.equals(2) && beginTime != null) {
            String beginTime03 = beginTime.substring(0, 7);
            beginTime = beginTime03;

        }
        if (type.equals(3) && beginTime != null) {
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

        RDto rDto = new RDto();
        rDto.setWxAmount(sumCount1);
        rDto.setZfbAmount(sumCount2);
        rDto.setAllAmount(sumAll0);

        return R.success(rDto);
    }


    /**
     * 用户端：待充值
     * author Kenlihankun
     * @return
     * @Param membershipTime 选择充值的会员时间 1：月卡 2：季卡 3：年卡
     */

    //待充值
    //保存userId,outTradeNo,rechargeAmount,rechargeType(默认),status(默认),createTime,UpdateTime
    @Transactional
    @PostMapping("/insertRechargeInfo")
    public R<TDto> recharge_click(
                                  @RequestParam("membershipTime") Long membershipTime) {
        //BaseContext 获取session Id
        Long userId = BaseContext.getCurrentId();
        QueryWrapper<Recharge> queryWrapper = new QueryWrapper<>();
        TDto tDto = new TDto();

        if (membershipTime.equals(1L) || membershipTime.equals(2L) || membershipTime.equals(3L) ){
            //获取充值金额
            String RA = "";
            BigDecimal rechargeAmount = new BigDecimal(10000);
            if (membershipTime.equals(1L)){
                Config config1 = configService.getById(13);
                RA =  config1.getContent();
                Pattern pattern = Pattern.compile("[0-9]*\\.?[0-9]+");
                boolean isTrue = pattern.matcher(RA).matches();
                if (isTrue){
                    rechargeAmount = new BigDecimal(RA);
                }
            }
            if (membershipTime.equals(2L)){
                Config config1 = configService.getById(14);
                RA =  config1.getContent();
                Pattern pattern = Pattern.compile("[0-9]*\\.?[0-9]+");
                boolean isTrue = pattern.matcher(RA).matches();
                if (isTrue){
                    rechargeAmount = new BigDecimal(RA);
                }
            }
            if (membershipTime.equals(3L)){
                Config config1 = configService.getById(15);
                RA =  config1.getContent();
                Pattern pattern = Pattern.compile("[0-9]*\\.?[0-9]+");
                boolean isTrue = pattern.matcher(RA).matches();
                if (isTrue){
                    rechargeAmount = new BigDecimal(RA);
                }
            }


            BigDecimal maxTimes = new BigDecimal(30);
            //当日充值最大次数
            Config config = configService.getById(17);
            String content = config.getContent();
            Pattern pattern = Pattern.compile("[0-9]*\\.?[0-9]+");
            boolean isTrue = pattern.matcher(content).matches();
            if (isTrue){
                maxTimes = new BigDecimal(content);
            }

            //查询当日充值次数
            LocalDateTime dateTime = LocalDateTime.now();
            String timeNow = DateTimeFormatter.ofPattern("yyyy-MM-dd").format(dateTime);
            queryWrapper.eq("user_id",userId).and(c -> c.like("create_time",timeNow));
            Integer count1 = rechargeService.count(queryWrapper);
            BigDecimal count_1 = new BigDecimal( Integer.parseInt ( count1.toString() ) );


            Recharge recharge = new Recharge();
            UpdateWrapper<Recharge> wrapper = new UpdateWrapper<>();

            //uuid转hashcode
            UUID uuid = UUID.randomUUID();
            Integer uuidNo = uuid.toString().hashCode();
            // String.hashCode()可能会是负数，如果为负数需要转换为正数
            uuidNo = uuidNo < 0 ? -uuidNo : uuidNo;
            Long outTradeNo = Long.valueOf(String.valueOf(uuidNo));

            if (count_1.compareTo(maxTimes)<1){
                //插入冲值表
                recharge.setOutTradeNo(outTradeNo);
                recharge.setRechargeAmount(rechargeAmount);
                recharge.setUserId(userId);
                recharge.setMembershipTime(membershipTime);
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

                //返回订单号码
                tDto.setOutTradeNo(outTradeNo0);
            }

        }


        //map.put("out_trade_no", outTradeNo0);
        return R.success(tDto);
    }

    /**
     * 用户端：充值成功以及续费成功
     * 更新充值表和用户表信息，插入选择充值会员时间
     * author Kenlihankun
     * @return
     * @Param rechargeType 选择的充值类型 0：零钱 1：微信 2：支付宝
     * @Param outTradeNo 充值订单号
     */
    //充值成功
    //
    @Transactional
    @PutMapping("/updateSuccessInfo")
    public R<String> recharge_confirm(@RequestParam("outTradeNo") Long outTradeNo,
                                      @RequestParam("rechargeType") Integer rechargeType) {

        //充值成功，根据订单号更新冲值表数据
        UpdateWrapper<User> updateWrapper = new UpdateWrapper<>();
        UpdateWrapper<Recharge> wrapper = new UpdateWrapper<>();
        UpdateWrapper<Team> update = new UpdateWrapper<>();
        QueryWrapper<Team> teamQueryWrapper = new QueryWrapper<>();
        String result = "success";


        QueryWrapper<Recharge> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("out_trade_no",outTradeNo);
        Recharge recharge = rechargeService.getOne(queryWrapper);
        //如果参数正确
        if (recharge != null) {
            if (rechargeType.equals(0) || rechargeType.equals(1) ||rechargeType.equals(2)){

                wrapper.eq("out_trade_no", outTradeNo);
                recharge.setRechargeType(rechargeType);
                rechargeService.update(recharge, wrapper);


                //根据冲值表数据更新用户表数据
                BigDecimal rechargeAmount = recharge.getRechargeAmount();
                Long userId = recharge.getUserId();
                LocalDateTime localDateTime = recharge.getUpdateTime();

                //获取用户表数据
                User user = userService.getById(userId);
                BigDecimal cashableAmount0 = user.getCashableAmount();

                //获取团队表数据
                teamQueryWrapper.eq("user_id", userId);
                Team team = teamService.getOne(teamQueryWrapper);
                BigDecimal totalWithdrawnAmount = team.getTotalWithdrawnAmount();

                //总金额
                BigDecimal total = totalWithdrawnAmount.add(cashableAmount0);


                //判断是否为待充值
                if (recharge.getStatus().equals(1)) {
                    if (recharge.getRechargeType().equals(0)) {//判断是否为零钱充值
                        updateWrapper.eq("id", userId);
                        if (rechargeAmount.compareTo(cashableAmount0) < 1) {
                            if (recharge.getMembershipTime().equals(1L)) {
                                //充值会员
                                if (user.getGrade().equals(0)) {
                                    user.setMembershipExpTime(localDateTime.plusMonths(1L));
                                }
                                if (user.getGrade().equals(1)) {
                                    //续费会员
                                    user.setMembershipExpTime(user.getMembershipExpTime().plusMonths(1L));

                                }
                            }
                            if (recharge.getMembershipTime().equals(2L)) {
                                //充值会员
                                if (user.getGrade().equals(0)) {
                                    user.setMembershipExpTime(localDateTime.plusMonths(3L));
                                }
                                if (user.getGrade().equals(1)) {
                                    //续费会员
                                    user.setMembershipExpTime(user.getMembershipExpTime().plusMonths(3L));

                                }
                            }
                            if (recharge.getMembershipTime().equals(3L)) {
                                //充值会员
                                if (user.getGrade().equals(0)) {
                                    user.setMembershipExpTime(localDateTime.plusYears(1L));
                                }
                                if (user.getGrade().equals(1)) {
                                    //续费会员
                                    user.setMembershipExpTime(user.getMembershipExpTime().plusYears(1L));

                                }
                            }
                            BigDecimal cashableAmount = user.getCashableAmount().subtract(rechargeAmount);
                            user.setCashableAmount(cashableAmount);
                            // 修改用户等级为会员
                            user.setGrade(1);
                            userService.update(user, updateWrapper);

                            recharge.setStatus(2);
                            rechargeService.update(recharge, wrapper);
                        } else if (rechargeAmount.compareTo(cashableAmount0) == 1 && rechargeAmount.compareTo(total) < 1) {
                            if (recharge.getMembershipTime().equals(1L)) {
                                //充值会员
                                if (user.getGrade().equals(0)) {
                                    user.setMembershipExpTime(localDateTime.plusMonths(1L));
                                }
                                if (user.getGrade().equals(1)) {
                                    //续费会员
                                    user.setMembershipExpTime(user.getMembershipExpTime().plusMonths(1L));

                                }
                            }
                            if (recharge.getMembershipTime().equals(2L)) {
                                //充值会员
                                if (user.getGrade().equals(0)) {
                                    user.setMembershipExpTime(localDateTime.plusMonths(3L));
                                }
                                if (user.getGrade().equals(1)) {
                                    //续费会员
                                    user.setMembershipExpTime(user.getMembershipExpTime().plusMonths(3L));

                                }
                            }
                            if (recharge.getMembershipTime().equals(3L)) {
                                //充值会员
                                if (user.getGrade().equals(0)) {
                                    user.setMembershipExpTime(localDateTime.plusYears(1L));
                                }
                                if (user.getGrade().equals(1)) {
                                    //续费会员
                                    user.setMembershipExpTime(user.getMembershipExpTime().plusYears(1L));

                                }
                            }
                            BigDecimal zero = new BigDecimal(0);
                            BigDecimal cashableAmount = rechargeAmount.subtract(cashableAmount0);
                            user.setCashableAmount(zero);
                            // 修改用户等级为会员
                            user.setGrade(1);
                            userService.update(user, updateWrapper);

                            //更新团队表
                            update.eq("user_id", userId);
                            BigDecimal totalWithdrawAmount1 = totalWithdrawnAmount.subtract(cashableAmount);
                            team.setTotalWithdrawnAmount(totalWithdrawAmount1);
                            teamService.update(team, update);

                            recharge.setStatus(2);
                            rechargeService.update(recharge, wrapper);

                            //插入数据到team_msg
                            String teamMsg = user.getName() + "申请了充值，并且从团队钱包扣除了" + cashableAmount + "元";
                            teamMsgService.addRecharge(userId, teamMsg);

                        } else {
                            result = "充值金额不满足条件";
                        }
                    }
                } else {
                    result = "不是待充值状态";
                }
            }else {
                result = "参数错误";
            }

        }else {
            result = "参数错误";
        }




        return R.success(result);
    }

    /**
     * 用户端：充值失败（用户取消充值）
     * 更新冲值表
     * author Kenlihankun
     *
     * @return
     * @Param outTradeNo 充值订单号
     */
    //充值失败
    @Transactional
    @PutMapping("/updateFailInfo")
    public R<String> recharge_cancel(@RequestParam("outTradeNo") Long outTradeNo) {
        UpdateWrapper<Recharge> wrapper = new UpdateWrapper<>();
        QueryWrapper<Recharge> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("out_trade_no",outTradeNo);
        Recharge recharge = rechargeService.getOne(queryWrapper);
        String result = "success";
        if (recharge!=null){
            if (recharge.getStatus().equals(1)){
                wrapper.eq("id", outTradeNo);
                recharge.setStatus(3);
                rechargeService.update(recharge, wrapper);
            }else {
                result = "不是待充值状态";
            }

        }else {
            result = "参数错误";
        }


        return R.success(result);
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
    @GetMapping("/getRechargePageInfo")
    public R<Page> Page(int Page, int PageSize, Integer chooseType, String beginTime, String endTime) {
        Page<Recharge> pageInfo = new Page(Page, PageSize);
        Page<RechargeDto> rechargeDtoPage = new Page<>();
        QueryWrapper<Recharge> wrapper = new QueryWrapper<>();
        //beginTime = beginTime + " 00:00:00";
        //endTime = endTime + " 00:00:00";
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
