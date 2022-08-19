package com.metabubble.BWC.dto.Imp;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public abstract class PageConverter {
    public static PageConverter INSTANCES = Mappers.getMapper(PageConverter.class);

    @Mapping(target = "records",ignore = true)
    public abstract Page PageToPage(Page page);
}
