package com.metabubble.BWC.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.metabubble.BWC.dto.CashableDto;
import com.metabubble.BWC.entity.Cashable;
import com.metabubble.BWC.mapper.CashableMapper;
import com.metabubble.BWC.service.CashableService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CashableServiceImpl extends ServiceImpl<CashableMapper, Cashable>
        implements CashableService {
    @Autowired
    CashableMapper cashableMapper;

    @Override
    public IPage<CashableDto> select(Page<CashableDto> cashableDtoPage, QueryWrapper<Object> wrapper) {
        return cashableMapper.dto(cashableDtoPage,wrapper);
    }
}
