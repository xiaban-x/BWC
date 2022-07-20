package com.metabubble.BWC.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.metabubble.BWC.entity.Logs;
import com.metabubble.BWC.mapper.LogsMapper;
import com.metabubble.BWC.service.LogsService;
import org.springframework.stereotype.Service;

@Service
public class LogsServiceImpl extends ServiceImpl<LogsMapper, Logs>
        implements LogsService {
}
