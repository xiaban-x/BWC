package com.metabubble.BWC.dto.Imp;

import com.metabubble.BWC.dto.OrdersDto;
import com.metabubble.BWC.entity.Orders;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public abstract class OrdersConverter {
    public static OrdersConverter INSTANCES = Mappers.getMapper(OrdersConverter.class);

    /**
     * 进行entity转换dto
     * @param OrdersDto
     * @return
     */
    public abstract OrdersDto OrdersToMerOrdersDto(Orders orders);

}
