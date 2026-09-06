package com.synkrotech.mvp.customers;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CustomerRequest(
        @NotBlank(message = "name is required")
        @Size(max = 255, message = "name must be at most 255 characters")
        String name,

        @NotBlank(message = "taxId is required")
        @Size(max = 40, message = "taxId must be at most 40 characters")
        String taxId,

        @Email(message = "email must be a valid address")
        @Size(max = 255, message = "email must be at most 255 characters")
        String email,

        @Size(max = 40, message = "phone must be at most 40 characters")
        String phone,

        @Size(max = 255, message = "address must be at most 255 characters")
        String address) {
}
