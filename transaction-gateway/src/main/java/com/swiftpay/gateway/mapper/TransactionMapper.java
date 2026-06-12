package com.swiftpay.gateway.mapper;

import com.swiftpay.gateway.dto.PaymentResponse;
import com.swiftpay.gateway.entity.Transaction;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TransactionMapper {

    @Mapping(target = "transactionId", source = "id")
    @Mapping(target = "senderId",      source = "sender.id")
    @Mapping(target = "receiverId",    source = "receiver.id")
    PaymentResponse toPaymentResponse(Transaction transaction);
}