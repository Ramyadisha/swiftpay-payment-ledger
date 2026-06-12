package com.swiftpay.gateway.mapper;

import com.swiftpay.gateway.dto.AccountDto;
import com.swiftpay.gateway.entity.Account;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AccountMapper {
    AccountDto toDto(Account account);
}