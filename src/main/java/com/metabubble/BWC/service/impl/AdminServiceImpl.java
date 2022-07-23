package com.metabubble.BWC.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.metabubble.BWC.entity.Admin;
import com.metabubble.BWC.mapper.AdminMapper;
import com.metabubble.BWC.service.AdminService;
import org.springframework.stereotype.Service;

@Service
public class AdminServiceImpl extends ServiceImpl<AdminMapper, Admin>
        implements AdminService {
}
