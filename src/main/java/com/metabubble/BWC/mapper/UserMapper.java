package com.metabubble.BWC.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.metabubble.BWC.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
}
