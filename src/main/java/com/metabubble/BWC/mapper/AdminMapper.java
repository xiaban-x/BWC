package com.metabubble.BWC.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.metabubble.BWC.entity.Admin;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface AdminMapper extends BaseMapper<Admin> {

}
