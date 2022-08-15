package com.metabubble.BWC.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.metabubble.BWC.entity.MerchantType;
import com.metabubble.BWC.entity.TeamMsg;
import com.metabubble.BWC.mapper.MerchantTypeMapper;
import com.metabubble.BWC.mapper.TeamMsgMapper;
import com.metabubble.BWC.service.MerchantTypeService;
import com.metabubble.BWC.service.TeamMsgService;
import com.metabubble.BWC.service.TeamService;
import org.springframework.stereotype.Service;

@Service
public class TeamMsgServiceImpl extends ServiceImpl<TeamMsgMapper, TeamMsg>
        implements TeamMsgService {

    /**
     * 添加团队详细信息
     * @param id    用户id
     * @param msg   信息
     */
    @Override
    public void add(Long id, String msg) {
        TeamMsg teamMsg = new TeamMsg();
        teamMsg.setUserId(id);
        teamMsg.setInformation(msg);
        this.save(teamMsg);
    }
}
