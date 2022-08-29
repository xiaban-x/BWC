package com.metabubble.BWC.dto.Imp;

import com.metabubble.BWC.dto.OrdersDo;
import com.metabubble.BWC.dto.OrdersDto;
import com.metabubble.BWC.dto.OrdersListDto;
import com.metabubble.BWC.entity.Orders;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public abstract class OrdersConverter {

    public static OrdersConverter INSTANCES = Mappers.getMapper(OrdersConverter.class);


    public abstract OrdersDto OrdersToMerOrdersDto(Orders orders);


    public abstract OrdersListDto OrdersToOrdersListDto(Orders orders);


    public abstract OrdersDo OrdersToOrdersDo(Orders orders);

}
