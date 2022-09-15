package com.metabubble.BWC.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.metabubble.BWC.dto.CashableDto;
import com.metabubble.BWC.dto.RechargeDto;
import com.metabubble.BWC.entity.Orders;
import com.metabubble.BWC.entity.Recharge;

public interface RechargeService extends IService<Recharge> {
    IPage<RechargeDto> select(Page<RechargeDto> rechargeDtoPage, QueryWrapper<Object> wrapper);

    public void otherRecharge(Recharge recharge);
}
