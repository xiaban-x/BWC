package com.metabubble.BWC.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.metabubble.BWC.entity.Admin;
import com.metabubble.BWC.entity.Cusservice;
import com.metabubble.BWC.mapper.AdminMapper;
import com.metabubble.BWC.mapper.CusserviceMapper;
import com.metabubble.BWC.service.AdminService;
import com.metabubble.BWC.service.CusserviceService;
import org.springframework.stereotype.Service;

@Service
public class CusserviceServiceImpl extends ServiceImpl<CusserviceMapper, Cusservice>
        implements CusserviceService {


}
