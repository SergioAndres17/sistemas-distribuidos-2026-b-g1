package com.synkrotech.mvp.customers;

import java.util.List;
import java.util.UUID;

import com.synkrotech.mvp.common.BusinessException;
import com.synkrotech.mvp.common.NotFoundException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CustomerService {

    private final CustomerRepository customers;

    public CustomerService(CustomerRepository customers) {
        this.customers = customers;
    }

    @Transactional(readOnly = true)
    public List<Customer> list(Boolean onlyActive) {
        return Boolean.TRUE.equals(onlyActive)
                ? customers.findByActiveTrueOrderByNameAsc()
                : customers.findAllByOrderByNameAsc();
    }

    /** Used by the sales flow, which needs the entity itself. */
    @Transactional(readOnly = true)
    public Customer getById(UUID id) {
        return customers.findById(id)
                .orElseThrow(() -> new NotFoundException("No customer exists with id " + id + "."));
    }

    @Transactional
    public Customer create(CustomerRequest request) {
        String identityDocument = request.identityDocument().trim();
        if (customers.existsByIdentityDocumentIgnoreCaseAndActiveTrue(identityDocument)) {
            throw new BusinessException("An active customer already uses the document " + identityDocument + ".");
        }
        return customers.save(new Customer(
                request.name().trim(),
                identityDocument,
                trimOrNull(request.email()),
                trimOrNull(request.phone()),
                trimOrNull(request.address())));
    }

    @Transactional
    public Customer update(UUID id, CustomerRequest request) {
        Customer customer = getById(id);
        String identityDocument = request.identityDocument().trim();
        if (customers.existsByIdentityDocumentIgnoreCaseAndActiveTrueAndIdNot(identityDocument, id)) {
            throw new BusinessException("An active customer already uses the document " + identityDocument + ".");
        }
        customer.setName(request.name().trim());
        customer.setIdentityDocument(identityDocument);
        customer.setEmail(trimOrNull(request.email()));
        customer.setPhone(trimOrNull(request.phone()));
        customer.setAddress(trimOrNull(request.address()));
        return customer;
    }

    /**
     * Soft delete: rows are never removed, so past sales keep pointing at a
     * customer that still resolves.
     */
    @Transactional
    public Customer deactivate(UUID id) {
        Customer customer = getById(id);
        customer.setActive(false);
        return customer;
    }

    private static String trimOrNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
