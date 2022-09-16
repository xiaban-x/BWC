package com.metabubble.BWC.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.metabubble.BWC.common.BaseContext;
import com.metabubble.BWC.dto.RechargeDto;
import com.metabubble.BWC.entity.Recharge;
import com.metabubble.BWC.mapper.RechargeMapper;
import com.metabubble.BWC.service.CashableService;
import com.metabubble.BWC.service.LogsService;
import com.metabubble.BWC.service.RechargeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;


@Service
public class RechargeServiceImpl extends ServiceImpl<RechargeMapper, Recharge>
        implements RechargeService {
    @Autowired
    RechargeService rechargeService;
    @Autowired
    LogsService logsService;
    @Autowired RechargeMapper rechargeMapper;
    @Override
    public IPage<RechargeDto> select(Page<RechargeDto> rechargeDtoPage, QueryWrapper<Object> wrapper) {
        return rechargeMapper.dto(rechargeDtoPage,wrapper);
    }


    //需要参数，userId，days
    @Override
    public void otherRecharge(@RequestBody Recharge recharge) {
        Recharge recharge1 = new Recharge();
        recharge1.setDays(recharge.getDays());


        //uuid转hashcode
        UUID uuid = UUID.randomUUID();
        Integer uuidNo = uuid.toString().hashCode();
        // String.hashCode()可能会是负数，如果为负数需要转换为正数
        uuidNo = uuidNo < 0 ? -uuidNo : uuidNo;
        Long outTradeNo = Long.valueOf(String.valueOf(uuidNo));
        //给订单编号加上日期
        SimpleDateFormat dmDate = new SimpleDateFormat("yyyyMMdd");
        Date time1 = new Date();
        String time = dmDate.format(time1);
        Long time0 = Long.valueOf(time);
        Long outTradeNo0 = time0 * 10000000000L + outTradeNo;

        recharge1.setOutTradeNo(outTradeNo0);
        recharge1.setRechargeType(1);
        recharge1.setUserId(recharge.getUserId());
        recharge1.setStatus(1);
        recharge1.setMembershipTime(0L);

        //保存管理员操作信息
        String name = "修改用户会员时间";
        String content = "会员时间变化"+recharge.getDays()+"天";
        logsService.saveLog(name,content);

        rechargeService.save(recharge1);

    }
}
