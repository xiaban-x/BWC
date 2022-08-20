package com.metabubble.BWC.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.metabubble.BWC.entity.Recruitment;
import com.metabubble.BWC.mapper.RecruitmentMapper;
import com.metabubble.BWC.service.RecruitmentService;
import org.springframework.stereotype.Service;

@Service
public class RecruitmentServiceImpl extends ServiceImpl<RecruitmentMapper, Recruitment>
        implements RecruitmentService {
}
