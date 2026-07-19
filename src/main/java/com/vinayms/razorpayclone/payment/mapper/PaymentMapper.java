package com.vinayms.razorpayclone.payment.mapper;

import com.vinayms.razorpayclone.payment.dto.payment.response.PaymentResponse;
import com.vinayms.razorpayclone.payment.entity.Payment;
import com.vinayms.razorpayclone.payment.gateway.dto.PaymentRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface PaymentMapper {

    @Mapping(source = "order.id", target = "orderId")
    PaymentResponse toPaymentResponse(Payment payment);

    @Mapping(source = "order.id", target = "orderId")
    List<PaymentResponse> toPaymentResponseList(List<Payment> payments);

    @Mapping(source = "order.id", target = "orderId")
    @Mapping(source = "id",target = "paymentId")
    PaymentRequest toPaymentRequest(Payment payment);
}
