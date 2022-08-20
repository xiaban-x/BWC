package com.metabubble.BWC.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.metabubble.BWC.entity.Cooperation;
import com.metabubble.BWC.mapper.CooperationMapper;
import com.metabubble.BWC.service.CooperationService;
import org.springframework.stereotype.Service;

@Service
public class CooperationServiceImpl extends ServiceImpl<CooperationMapper, Cooperation>
        implements CooperationService {
}
