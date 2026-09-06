package com.synkrotech.mvp.sales;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import com.synkrotech.mvp.customers.Customer;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "sales")
public class Sale {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    /** Mapped to sale_date because "date" is a type name in PostgreSQL. */
    @Column(name = "sale_date", nullable = false)
    private LocalDateTime date;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal total = BigDecimal.ZERO;

    @Column(nullable = false)
    private boolean active = true;

    @OneToMany(mappedBy = "sale", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SaleDetail> details = new ArrayList<>();

    protected Sale() {
        // for JPA
    }

    public Sale(Customer customer, LocalDateTime date) {
        this.customer = customer;
        this.date = date;
        this.active = true;
    }

    /** Attaches a line and keeps {@link #total} in step with it. */
    public void addDetail(SaleDetail detail) {
        detail.setSale(this);
        this.details.add(detail);
        this.total = this.total.add(detail.getSubtotal());
    }

    public UUID getId() {
        return id;
    }

    public Customer getCustomer() {
        return customer;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public List<SaleDetail> getDetails() {
        return Collections.unmodifiableList(details);
    }
}
