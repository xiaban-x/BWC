package com.metabubble.BWC.dto.Imp;

import com.metabubble.BWC.dto.UserDto;
import com.metabubble.BWC.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public abstract class UserConverter {

    public static UserConverter INSTANCES = Mappers.getMapper(UserConverter.class);

    public abstract UserDto toUserRoleDto(User user);
}

