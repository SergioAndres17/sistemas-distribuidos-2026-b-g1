package com.synkrotech.mvp.customers;

import java.util.UUID;

public record CustomerResponse(
        UUID id,
        String name,
        String taxId,
        String email,
        String phone,
        String address,
        boolean active) {

    public static CustomerResponse from(Customer customer) {
        return new CustomerResponse(
                customer.getId(),
                customer.getName(),
                customer.getTaxId(),
                customer.getEmail(),
                customer.getPhone(),
                customer.getAddress(),
                customer.isActive());
    }
}
