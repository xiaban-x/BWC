package com.metabubble.BWC.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.metabubble.BWC.entity.Recharge;
import com.metabubble.BWC.mapper.RechargeMapper;
import com.metabubble.BWC.service.RechargeService;
import org.springframework.stereotype.Service;

@Service
public class RechargeServiceImpl extends ServiceImpl<RechargeMapper, Recharge>
        implements RechargeService {
}
