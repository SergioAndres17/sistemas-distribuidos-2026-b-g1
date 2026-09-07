package com.synkrotech.mvp.sales;

import java.util.List;
import java.util.UUID;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record CreateSaleRequest(
        @NotNull(message = "customerId is required")
        UUID customerId,

        @NotEmpty(message = "a sale needs at least one item")
        List<@Valid Item> items) {

    public record Item(
            @NotNull(message = "productId is required")
            UUID productId,

            @NotNull(message = "quantity is required")
            @Min(value = 1, message = "quantity must be greater than 0")
            Integer quantity) {
    }
}
