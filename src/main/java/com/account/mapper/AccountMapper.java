package com.account.mapper;


import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;
import org.springframework.stereotype.Component;

import com.account.dto.AccountRequest;
import com.account.dto.AccountResponse;
import com.account.model.Account;

@Mapper(componentModel = "spring")
public interface AccountMapper {

    Account toEntity(AccountRequest request);

    @Mapping(target = "maskedAccountNumber", source = "accountNumber", qualifiedByName = "maskedAccountNumber")
    AccountResponse toDto(Account account);

    @Named("maskedAccountNumber")
    default String mask(String accountNumber) {
        if (accountNumber == null || accountNumber.length() < 4) {
            return "*****";
        }
        return "*****" + accountNumber.substring(accountNumber.length() - 4);
    }
}