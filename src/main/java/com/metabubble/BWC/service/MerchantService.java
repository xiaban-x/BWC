package com.metabubble.BWC.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.metabubble.BWC.entity.Merchant;

public interface MerchantService extends IService<Merchant> {

    //检查用户是否在黑名单中
    public Boolean checkBlackList(String tel ,Long merchantId);
}
