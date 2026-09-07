package com.synkrotech.mvp.sales;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.synkrotech.mvp.common.BusinessException;
import com.synkrotech.mvp.common.NotFoundException;
import com.synkrotech.mvp.customers.Customer;
import com.synkrotech.mvp.customers.CustomerService;
import com.synkrotech.mvp.products.Product;
import com.synkrotech.mvp.products.ProductService;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SaleService {

    private final SaleRepository sales;
    private final CustomerService customerService;
    private final ProductService productService;

    public SaleService(SaleRepository sales, CustomerService customerService, ProductService productService) {
        this.sales = sales;
        this.customerService = customerService;
        this.productService = productService;
    }

    @Transactional(readOnly = true)
    public List<Sale> history() {
        return sales.findAllForHistory();
    }

    @Transactional(readOnly = true)
    public Sale getById(UUID id) {
        return sales.findByIdWithDetails(id)
                .orElseThrow(() -> new NotFoundException("No sale exists with id " + id + "."));
    }

    /**
     * Registers a sale and takes the sold units out of stock.
     *
     * <p>Everything runs in one transaction: if any line fails a check, the
     * exception rolls back the stock already deducted for earlier lines, so a
     * rejected sale leaves no trace.
     */
    @Transactional
    public Sale create(CreateSaleRequest request) {
        Customer customer = findCustomer(request.customerId());
        if (!customer.isActive()) {
            throw new BusinessException(
                    "The customer " + customer.getName() + " is deactivated and cannot be sold to.");
        }

        Sale sale = new Sale(customer, LocalDateTime.now());
        for (CreateSaleRequest.Item item : request.items()) {
            Product product = findProduct(item.productId());
            if (!product.isActive()) {
                throw new BusinessException("The product " + product.getName() + " is deactivated.");
            }
            // Repeating a product across lines is fine: the same managed
            // instance comes back, so its stock already reflects earlier lines.
            if (item.quantity() > product.getStock()) {
                throw new BusinessException("Not enough stock for " + product.getName()
                        + ": " + item.quantity() + " requested, " + product.getStock() + " available.");
            }
            product.removeStock(item.quantity());
            sale.addDetail(new SaleDetail(product, item.quantity(), product.getPrice()));
        }

        return sales.save(sale);
    }

    /*
     * Sales talk to the other contexts through their services (same process, no
     * HTTP). Those services answer a missing row with 404, but inside a sale a
     * bad id is a rejected sale, not a missing page — so it becomes a 409 here.
     */
    private Customer findCustomer(UUID customerId) {
        try {
            return customerService.getById(customerId);
        } catch (NotFoundException ex) {
            throw new BusinessException("No customer exists with id " + customerId + ".");
        }
    }

    private Product findProduct(UUID productId) {
        try {
            return productService.getById(productId);
        } catch (NotFoundException ex) {
            throw new BusinessException("No product exists with id " + productId + ".");
        }
    }
}
