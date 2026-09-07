package com.synkrotech.mvp.customers;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerRepository extends JpaRepository<Customer, UUID> {

    List<Customer> findAllByOrderByNameAsc();

    List<Customer> findByActiveTrueOrderByNameAsc();

    boolean existsByTaxIdIgnoreCaseAndActiveTrue(String taxId);

    boolean existsByTaxIdIgnoreCaseAndActiveTrueAndIdNot(String taxId, UUID id);
}
