package dev.distributed.bank.dto.response;

import java.math.BigDecimal;

public class TotalBalanceResponse {

    private BigDecimal hanoiTotalBalance;
    private BigDecimal danangTotalBalance;
    private BigDecimal hcmTotalBalance;
    private BigDecimal systemTotalBalance;
    private int hanoiAccountCount;
    private int danangAccountCount;
    private int hcmAccountCount;

    public BigDecimal getHanoiTotalBalance() {
        return hanoiTotalBalance;
    }

    public void setHanoiTotalBalance(BigDecimal hanoiTotalBalance) {
        this.hanoiTotalBalance = hanoiTotalBalance;
    }

    public BigDecimal getDanangTotalBalance() {
        return danangTotalBalance;
    }

    public void setDanangTotalBalance(BigDecimal danangTotalBalance) {
        this.danangTotalBalance = danangTotalBalance;
    }

    public BigDecimal getHcmTotalBalance() {
        return hcmTotalBalance;
    }

    public void setHcmTotalBalance(BigDecimal hcmTotalBalance) {
        this.hcmTotalBalance = hcmTotalBalance;
    }

    public BigDecimal getSystemTotalBalance() {
        return systemTotalBalance;
    }

    public void setSystemTotalBalance(BigDecimal systemTotalBalance) {
        this.systemTotalBalance = systemTotalBalance;
    }

    public int getHanoiAccountCount() {
        return hanoiAccountCount;
    }

    public void setHanoiAccountCount(int hanoiAccountCount) {
        this.hanoiAccountCount = hanoiAccountCount;
    }

    public int getDanangAccountCount() {
        return danangAccountCount;
    }

    public void setDanangAccountCount(int danangAccountCount) {
        this.danangAccountCount = danangAccountCount;
    }

    public int getHcmAccountCount() {
        return hcmAccountCount;
    }

    public void setHcmAccountCount(int hcmAccountCount) {
        this.hcmAccountCount = hcmAccountCount;
    }
}
