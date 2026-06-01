package dev.distributed.bank.entity;

import java.time.LocalDateTime;

public class Branch {

    private String branchId;
    private String branchName;
    private String city;
    private LocalDateTime createdAt;

    public Branch() {
    }

    public Branch(String branchId, String branchName, String city) {
        this.branchId = branchId;
        this.branchName = branchName;
        this.city = city;
    }

    public String getBranchId() {
        return branchId;
    }

    public void setBranchId(String branchId) {
        this.branchId = branchId;
    }

    public String getBranchName() {
        return branchName;
    }

    public void setBranchName(String branchName) {
        this.branchName = branchName;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}