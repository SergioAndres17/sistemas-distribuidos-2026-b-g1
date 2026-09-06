package com.synkrotech.mvp.sales;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record SaleResponse(
        UUID id,
        UUID customerId,
        String customerName,
        LocalDateTime date,
        BigDecimal total,
        boolean active,
        List<Item> items) {

    public record Item(
            UUID id,
            UUID productId,
            String productName,
            int quantity,
            BigDecimal unitPrice,
            BigDecimal subtotal) {

        static Item from(SaleDetail detail) {
            return new Item(
                    detail.getId(),
                    detail.getProduct().getId(),
                    detail.getProduct().getName(),
                    detail.getQuantity(),
                    detail.getUnitPrice(),
                    detail.getSubtotal());
        }
    }

    public static SaleResponse from(Sale sale) {
        return new SaleResponse(
                sale.getId(),
                sale.getCustomer().getId(),
                sale.getCustomer().getName(),
                sale.getDate(),
                sale.getTotal(),
                sale.isActive(),
                sale.getDetails().stream().map(Item::from).toList());
    }
}
