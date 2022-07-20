package com.metabubble.BWC.dto.Imp;

import com.metabubble.BWC.dto.MerchantDto;
import com.metabubble.BWC.entity.Merchant;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;


/**
 * merchant类型转换
 */
@Mapper(componentModel = "spring")
public abstract class MerchantConverter {

    public static MerchantConverter INSTANCES = Mappers.getMapper(MerchantConverter.class);

    /**
     * 进行entity转换dto
     * @param merchant
     * @return
     */
    public abstract MerchantDto MerchantToMerchantDto(Merchant merchant);
}
