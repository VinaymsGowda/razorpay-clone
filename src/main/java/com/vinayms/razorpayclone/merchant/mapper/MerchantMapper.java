package com.vinayms.razorpayclone.merchant.mapper;

import com.vinayms.razorpayclone.merchant.dto.request.MerchantSignUpRequest;
import com.vinayms.razorpayclone.merchant.dto.response.MerchantSignUpResponse;
import com.vinayms.razorpayclone.merchant.entity.Merchant;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface MerchantMapper {

    Merchant toMerchant(MerchantSignUpRequest request);

    MerchantSignUpResponse  toMerchantSignUpResponse(Merchant merchant);
}
