package com.metabubble.BWC.dto.Imp;

import com.metabubble.BWC.dto.AdminDto;
import com.metabubble.BWC.entity.Admin;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * admin类型转换
 */
@Mapper(componentModel = "spring")
public abstract class AdminConverter {

    public static AdminConverter INSTANCES = Mappers.getMapper(AdminConverter.class);

    /**
     * 进行entity转换dto
     * @param admin 转换对象
     * @return
     */
    public abstract AdminDto toAdminRoleDto(Admin admin);

    /**
     * 进行dto转换entity
     * @param adminDto 转换对象
     * @return
     */
    public abstract Admin toAdminDtoRoleAdmin(AdminDto adminDto);
}

