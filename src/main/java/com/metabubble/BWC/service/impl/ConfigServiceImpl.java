package com.metabubble.BWC.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.metabubble.BWC.entity.Admin;
import com.metabubble.BWC.entity.Config;
import com.metabubble.BWC.mapper.AdminMapper;
import com.metabubble.BWC.mapper.ConfigMapper;
import com.metabubble.BWC.service.AdminService;
import com.metabubble.BWC.service.ConfigService;
import org.springframework.stereotype.Service;

@Service
public class ConfigServiceImpl extends ServiceImpl<ConfigMapper, Config>
        implements ConfigService {


}
