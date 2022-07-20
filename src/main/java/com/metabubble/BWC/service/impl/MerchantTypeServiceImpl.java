package com.metabubble.BWC.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.metabubble.BWC.entity.Admin;
import com.metabubble.BWC.entity.Merchant;
import com.metabubble.BWC.entity.MerchantType;
import com.metabubble.BWC.mapper.AdminMapper;
import com.metabubble.BWC.mapper.MerchantMapper;
import com.metabubble.BWC.mapper.MerchantTypeMapper;
import com.metabubble.BWC.service.AdminService;
import com.metabubble.BWC.service.MerchantTypeService;
import org.springframework.stereotype.Service;

@Service
public class MerchantTypeServiceImpl extends ServiceImpl<MerchantTypeMapper, MerchantType>
        implements MerchantTypeService {


}
