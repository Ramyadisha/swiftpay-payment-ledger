package com.swiftpay.ledger.mapper;

import com.swiftpay.ledger.dto.TransactionDto;
import com.swiftpay.ledger.entity.Transaction;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface TransactionMapper {
    TransactionDto toDto(Transaction transaction);
}
