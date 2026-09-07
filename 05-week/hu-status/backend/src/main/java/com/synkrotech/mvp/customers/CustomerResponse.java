package com.synkrotech.mvp.customers;

import java.time.LocalDateTime;
import java.util.UUID;

public record CustomerResponse(
        UUID id,
        String name,
        String identityDocument,
        String email,
        String phone,
        String address,
        LocalDateTime registrationDate,
        boolean active) {

    public static CustomerResponse from(Customer customer) {
        return new CustomerResponse(
                customer.getId(),
                customer.getName(),
                customer.getIdentityDocument(),
                customer.getEmail(),
                customer.getPhone(),
                customer.getAddress(),
                customer.getRegistrationDate(),
                customer.isActive());
    }
}
