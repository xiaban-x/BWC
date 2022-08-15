package com.metabubble.BWC.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.metabubble.BWC.entity.MerchantType;
import com.metabubble.BWC.entity.TeamMsg;

public interface TeamMsgService extends IService<TeamMsg> {

    //添加团队信息
    public void add(Long id,String msg);



}
