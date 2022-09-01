package com.metabubble.BWC.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.metabubble.BWC.entity.Merchant;
import com.metabubble.BWC.mapper.MerchantMapper;
import com.metabubble.BWC.service.MerchantService;
import org.springframework.stereotype.Service;

@Service
public class MerchantServiceImpl extends ServiceImpl<MerchantMapper, Merchant>
        implements MerchantService {




    @Override
    public Boolean checkBlackList(String tel, Long merchantId) {
        Merchant merchant = this.getById(merchantId);
        String blacklist = merchant.getBlacklist();
        if (blacklist!=null) {
            String[] split = blacklist.split(",");
            for (String s : split) {
                if (s.equals(merchantId)){
                    return true;
                }
            }
        }
        return false;
    }
}
