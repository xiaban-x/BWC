package com.metabubble.BWC.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.metabubble.BWC.common.BaseContext;
import com.metabubble.BWC.common.R;
import com.metabubble.BWC.dto.RDto;
import com.metabubble.BWC.dto.RechargeDto;
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
     * type 1为按天查询查询 2为按月查询 3为按年查询
     * @return
     * @RequestBody map
     */
    //充值统计
    @GetMapping("/getRechargeCount")
    public R<RDto> recharge_amount(@RequestParam("Type") Integer type, @RequestParam("BeginTime") String beginTime) {


        QueryWrapper<Recharge> queryAmount01 = new QueryWrapper<>();
        QueryWrapper<Recharge> queryAmount02 = new QueryWrapper<>();
        QueryWrapper<Recharge> queryAll = new QueryWrapper<>();
        RDto rDto = new RDto();

        if (beginTime.length()>9){
            //充值
            if (type.equals(1)) {
                String beginTime02 = beginTime.substring(0, 10);
                beginTime = beginTime02;

            }
            else if (type.equals(2) ) {
                String beginTime03 = beginTime.substring(0, 7);
                beginTime = beginTime03;

            }
            else if (type.equals(3) ) {
                String beginTime04 = beginTime.substring(0, 4);
                beginTime = beginTime04;

            }else {
                beginTime = "0000-00-00 00:00:00";
            }
            //统计微信充值条件
            queryAmount01.likeRight("create_time", beginTime).and(c2 -> c2.eq("recharge_type", 1))
                    .and(c2 -> c2.eq("status", 1));
            //统计支付宝充值条件
            queryAmount02.likeRight("create_time", beginTime).and(c3 -> c3.eq("recharge_type", 2))
                    .and(c3 -> c3.eq("status", 1));

            //统计充值总金额条件
            queryAll.likeRight("create_time", beginTime).and(c1 -> c1.eq("status", 1));


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

            rDto.setWxAmount(sumCount1);
            rDto.setZfbAmount(sumCount2);
            rDto.setAllAmount(sumAll0);
        }else {
            rDto.setWxAmount(new BigDecimal(0));
            rDto.setZfbAmount(new BigDecimal(0));
            rDto.setAllAmount(new BigDecimal(0));
        }


        return R.success(rDto);
    }


    /**
     * 用户端：发起充值请求
     * author Kenlihankun
     * @return
     * @Param membershipTime 选择充值的会员时间 1：月卡 2：季卡 3：年卡
     *  @Param rechargeType 选择的充值类型 0：零钱 1：微信 2：支付宝
     */

    //待充值
    //保存userId,outTradeNo,rechargeAmount,rechargeType(默认),status(默认),createTime,UpdateTime
    @Transactional
    @PostMapping("/insertRechargeInfo")
    public R<String> recharge_click(
                                  @RequestParam("membershipTime") Long membershipTime,
                                  @RequestParam("rechargeType") Integer rechargeType) {
        //BaseContext 获取session Id
        Long userId = BaseContext.getCurrentId();
        //Long userId = 2L;

        //充值成功
        UpdateWrapper<User> updateWrapper = new UpdateWrapper<>();
        UpdateWrapper<Team> update = new UpdateWrapper<>();
        UpdateWrapper<Recharge> wrapper = new UpdateWrapper<>();

        //获取user数据
        User user = userService.getById(userId);
        //获取团队表数据
        QueryWrapper<Team> teamQueryWrapper = new QueryWrapper<>();
        teamQueryWrapper.eq("user_id", userId);
        Team team = teamService.getOne(teamQueryWrapper);


        Recharge recharge = new Recharge();

        LocalDateTime localDateTime = LocalDateTime.now();

        if (user != null && team!=null) {
            BigDecimal cashableAmount0 = user.getCashableAmount();
            BigDecimal totalWithdrawnAmount = team.getTotalWithdrawnAmount();
            //总金额
            BigDecimal total = totalWithdrawnAmount.add(cashableAmount0);

            if (rechargeType.equals(0) || rechargeType.equals(1) || rechargeType.equals(2)) {
                if (membershipTime.equals(1L) || membershipTime.equals(2L) || membershipTime.equals(3L)) {
                    //获取充值金额
                    String RA = "";
                    BigDecimal rechargeAmount = new BigDecimal(10000);
                    if (membershipTime.equals(1L)) {
                        Config config1 = configService.getById(13);
                        RA = config1.getContent();
                        Pattern pattern = Pattern.compile("[0-9]*\\.?[0-9]+");
                        boolean isTrue = pattern.matcher(RA).matches();
                        if (isTrue) {
                            rechargeAmount = new BigDecimal(RA);
                        }
                    }
                    if (membershipTime.equals(2L)) {
                        Config config1 = configService.getById(14);
                        RA = config1.getContent();
                        Pattern pattern = Pattern.compile("[0-9]*\\.?[0-9]+");
                        boolean isTrue = pattern.matcher(RA).matches();
                        if (isTrue) {
                            rechargeAmount = new BigDecimal(RA);
                        }
                    }
                    if (membershipTime.equals(3L)) {
                        Config config1 = configService.getById(15);
                        RA = config1.getContent();
                        Pattern pattern = Pattern.compile("[0-9]*\\.?[0-9]+");
                        boolean isTrue = pattern.matcher(RA).matches();
                        if (isTrue) {
                            rechargeAmount = new BigDecimal(RA);
                        }
                    }
                    if (rechargeType.equals(0)) {
                        if (rechargeAmount.compareTo(total)<1) {

                            updateWrapper.eq("id", userId);
                            if (rechargeAmount.compareTo(cashableAmount0) < 1) {
                                if (membershipTime.equals(1L)) {
                                    //充值会员
                                    if (user.getGrade().equals(0)) {
                                        user.setMembershipExpTime(localDateTime.plusMonths(1L));
                                    }
                                    if (user.getGrade().equals(1)) {
                                        //续费会员
                                        user.setMembershipExpTime(user.getMembershipExpTime().plusMonths(1L));

                                    }
                                }
                                if (membershipTime.equals(2L)) {
                                    //充值会员
                                    if (user.getGrade().equals(0)) {
                                        user.setMembershipExpTime(localDateTime.plusMonths(3L));
                                    }
                                    if (user.getGrade().equals(1)) {
                                        //续费会员
                                        user.setMembershipExpTime(user.getMembershipExpTime().plusMonths(3L));

                                    }
                                }
                                if (membershipTime.equals(3L)) {
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

                            } else if (rechargeAmount.compareTo(cashableAmount0) == 1 && rechargeAmount.compareTo(total) < 1) {
                                if (membershipTime.equals(1L)) {
                                    //充值会员
                                    if (user.getGrade().equals(0)) {
                                        user.setMembershipExpTime(localDateTime.plusMonths(1L));
                                    }
                                    if (user.getGrade().equals(1)) {
                                        //续费会员
                                        user.setMembershipExpTime(user.getMembershipExpTime().plusMonths(1L));

                                    }
                                }
                                if (membershipTime.equals(2L)) {
                                    //充值会员
                                    if (user.getGrade().equals(0)) {
                                        user.setMembershipExpTime(localDateTime.plusMonths(3L));
                                    }
                                    if (user.getGrade().equals(1)) {
                                        //续费会员
                                        user.setMembershipExpTime(user.getMembershipExpTime().plusMonths(3L));

                                    }
                                }
                                if (membershipTime.equals(3L)) {
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


                                //插入数据到team_msg
                                String teamMsg = user.getName() + "申请了充值，并且从团队钱包扣除了" + cashableAmount + "元";
                                teamMsgService.addRecharge(userId, teamMsg);



                            }
                            //uuid转hashcode
                            UUID uuid = UUID.randomUUID();
                            Integer uuidNo = uuid.toString().hashCode();
                            // String.hashCode()可能会是负数，如果为负数需要转换为正数
                            uuidNo = uuidNo < 0 ? -uuidNo : uuidNo;
                            Long outTradeNo = Long.valueOf(String.valueOf(uuidNo));

                            //插入冲值表
                            recharge.setStatus(1);
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
                        }
                        else {
                            return R.error("充值金额不足");
                        }

                    }

                }else {
                    return R.error("memberTime参数错误");
                }

            }else {
                return R.error("rechargeType参数错误");
            }
        }

        return R.success("success");
    }

    /**
     * 充值管理分页联表查询
     * author Kenlihankun
     * @Param Page 页码
     * @Param PageSize 条数
     * @Param chooseType 选择类型 0:全部 1：充值成功 2：充值失败
     * @Param beginTime 选择开始时间
     * @Param endTime 选择截至时间
     * @return
     */
    @GetMapping("/getRechargePageInfo")
    public R<Page> Page(int Page, int PageSize, Integer chooseType, String beginTime, String endTime) {
        Page<Recharge> pageInfo = new Page(Page, PageSize);
        Page<RechargeDto> rechargeDtoPage = new Page<>();
        QueryWrapper<Recharge> wrapper = new QueryWrapper<>();
        String finalEndTime = endTime;


        if (chooseType.equals(0) || chooseType.equals(1) || chooseType.equals(2)){
            if (chooseType.equals(0)) {
                wrapper.ge("create_time", beginTime).and(c -> c.le("create_time", finalEndTime));
            }
            if (chooseType.equals(1)){
                wrapper.ge("create_time", beginTime).and(c -> c.le("create_time", finalEndTime))
                        .and(c -> c.eq("status", 1));

            }
            if (chooseType.equals(2)){
                wrapper.ge("create_time", beginTime).and(c -> c.le("create_time", finalEndTime))
                        .and(c -> c.eq("status",2));
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
                    rechargeDto.setUserId(userId);
                }
                return rechargeDto;
            }).collect(Collectors.toList());

            rechargeDtoPage.setRecords(list);

        }
        return R.success(rechargeDtoPage);

    }
}
