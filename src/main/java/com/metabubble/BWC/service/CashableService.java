package com.metabubble.BWC.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.metabubble.BWC.dto.CashableDto;
import com.metabubble.BWC.entity.Cashable;

public interface CashableService extends IService<Cashable> {
    IPage<CashableDto> select(Page<CashableDto> cashableDtoPage, QueryWrapper<Object> wrapper);
}
