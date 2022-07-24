package com.metabubble.BWC.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.metabubble.BWC.entity.Cashable;
import com.metabubble.BWC.mapper.CashableMapper;
import com.metabubble.BWC.service.CashableService;
import org.springframework.stereotype.Service;

@Service
public class CashableServiceImpl extends ServiceImpl<CashableMapper, Cashable>
        implements CashableService {
}
