package dev.distributed.bank.dto.response;

import dev.distributed.bank.entity.Account;

import java.math.BigDecimal;
import java.util.List;

public class ConcurrentWithdrawResponse {

    private Account account;
    private BigDecimal initialBalance;
    private BigDecimal finalBalance;
    private BigDecimal expectedBalance;
    private boolean lostUpdate;
    private String thread1Result;
    private String thread2Result;
    private List<String> logs;

    public ConcurrentWithdrawResponse() {
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public BigDecimal getInitialBalance() {
        return initialBalance;
    }

    public void setInitialBalance(BigDecimal initialBalance) {
        this.initialBalance = initialBalance;
    }

    public BigDecimal getFinalBalance() {
        return finalBalance;
    }

    public void setFinalBalance(BigDecimal finalBalance) {
        this.finalBalance = finalBalance;
    }

    public BigDecimal getExpectedBalance() {
        return expectedBalance;
    }

    public void setExpectedBalance(BigDecimal expectedBalance) {
        this.expectedBalance = expectedBalance;
    }

    public boolean isLostUpdate() {
        return lostUpdate;
    }

    public void setLostUpdate(boolean lostUpdate) {
        this.lostUpdate = lostUpdate;
    }

    public String getThread1Result() {
        return thread1Result;
    }

    public void setThread1Result(String thread1Result) {
        this.thread1Result = thread1Result;
    }

    public String getThread2Result() {
        return thread2Result;
    }

    public void setThread2Result(String thread2Result) {
        this.thread2Result = thread2Result;
    }

    public List<String> getLogs() {
        return logs;
    }

    public void setLogs(List<String> logs) {
        this.logs = logs;
    }
}
