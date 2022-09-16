package com.metabubble.BWC.mapper;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.metabubble.BWC.dto.RechargeDto;
import com.metabubble.BWC.entity.Recharge;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface RechargeMapper extends BaseMapper<Recharge> {
    @Select("SELECT user.id,user.name," +
            "recharge.id,recharge.recharge_type," +
            "recharge.recharge_amount,recharge.status,recharge.create_time,recharge.days,user.tel,user.membership_exp_time" +
            " FROM recharge" +
            " JOIN user" +
            " on recharge.user_id=user.id" +
            " ${ew.customSqlSegment}" +
            " ORDER BY recharge.create_time DESC")
    IPage<RechargeDto> dto(Page<RechargeDto> page, @Param(Constants.WRAPPER) QueryWrapper<Object> wrapper);
}
