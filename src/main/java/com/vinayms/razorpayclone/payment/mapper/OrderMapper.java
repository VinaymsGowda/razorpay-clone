package com.vinayms.razorpayclone.payment.mapper;

import com.vinayms.razorpayclone.payment.dto.order.request.OrderResponse;
import com.vinayms.razorpayclone.payment.entity.Order;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface OrderMapper {

    OrderResponse toOrderResponse(Order order);

}
