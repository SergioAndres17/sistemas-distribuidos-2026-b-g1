package com.synkrotech.mvp.customers;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "customers")
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    /**
     * Unique among active customers only. A deactivated customer keeps its
     * identity document for traceability, so this cannot be a plain unique
     * constraint; the check lives in {@link CustomerService}.
     */
    @Column(name = "identity_document", nullable = false, length = 40)
    private String identityDocument;

    private String email;

    @Column(length = 40)
    private String phone;

    private String address;

    @Column(name = "registration_date", nullable = false)
    private LocalDateTime registrationDate;

    @Column(nullable = false)
    private boolean active = true;

    protected Customer() {
        // for JPA
    }

    public Customer(String name, String identityDocument, String email, String phone, String address) {
        this.name = name;
        this.identityDocument = identityDocument;
        this.email = email;
        this.phone = phone;
        this.address = address;
        this.registrationDate = LocalDateTime.now();
        this.active = true;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIdentityDocument() {
        return identityDocument;
    }

    public void setIdentityDocument(String identityDocument) {
        this.identityDocument = identityDocument;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public LocalDateTime getRegistrationDate() {
        return registrationDate;
    }
}
