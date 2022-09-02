package com.metabubble.BWC.dto.Imp;

import com.metabubble.BWC.dto.TaskUserDto;
import com.metabubble.BWC.dto.UserDo;
import com.metabubble.BWC.dto.UserDto;
import com.metabubble.BWC.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * user类型转换
 */
@Mapper(componentModel = "spring")
public abstract class UserConverter {

    public static UserConverter INSTANCES = Mappers.getMapper(UserConverter.class);

    /**
     * 进行entity转换dto
     * @param user 转换对象
     * @return
     */
    public abstract UserDto toUserRoleDto(User user);

    /**
     * 进行dto转换entity
     * @param userDto 转换对象
     * @return
     */
    public abstract User toUserDtoRoleUser(UserDto userDto);

    /**
     * 进行entity转dto
     * @param user
     * @return
     */
    public abstract TaskUserDto UserToTaskUserDto(User user);


    public abstract UserDo UserToUserDo(User user);
}

