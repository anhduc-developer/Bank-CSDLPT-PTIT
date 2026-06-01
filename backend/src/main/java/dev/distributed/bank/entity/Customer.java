package dev.distributed.bank.entity;

import java.time.LocalDateTime;

public class Customer {

    private Long customerId;
    private String fullName;
    private String phone;
    private String email;
    private String address;
    private String branchId;
    private LocalDateTime createdAt;

    public Customer() {
    }

    public Customer(String fullName, String phone, String email, String address, String branchId) {
        this.fullName = fullName;
        this.phone = phone;
        this.email = email;
        this.address = address;
        this.branchId = branchId;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getBranchId() {
        return branchId;
    }

    public void setBranchId(String branchId) {
        this.branchId = branchId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}