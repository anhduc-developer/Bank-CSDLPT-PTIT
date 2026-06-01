package dev.distributed.bank.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class DeadlockDemoResponse {

    private String status;
    private String branchId;

    private Long accountAId;
    private Long accountBId;

    private BigDecimal balanceABefore;
    private BigDecimal balanceBBefore;
    private BigDecimal balanceAAfter;
    private BigDecimal balanceBAfter;
    private BigDecimal amountAtoB;
    private BigDecimal amountBtoA;

    private String winnerThread;
    private String victimThread;
    private String winnerDirection;
    private String victimDirection;

    private List<String> thread1Logs;
    private List<String> thread2Logs;
    private List<String> combinedLogs;

    private LocalDateTime timestamp;

    public DeadlockDemoResponse() {
        this.timestamp = LocalDateTime.now();
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getBranchId() {
        return branchId;
    }

    public void setBranchId(String branchId) {
        this.branchId = branchId;
    }

    public Long getAccountAId() {
        return accountAId;
    }

    public void setAccountAId(Long accountAId) {
        this.accountAId = accountAId;
    }

    public Long getAccountBId() {
        return accountBId;
    }

    public void setAccountBId(Long accountBId) {
        this.accountBId = accountBId;
    }

    public BigDecimal getBalanceABefore() {
        return balanceABefore;
    }

    public void setBalanceABefore(BigDecimal balanceABefore) {
        this.balanceABefore = balanceABefore;
    }

    public BigDecimal getBalanceBBefore() {
        return balanceBBefore;
    }

    public void setBalanceBBefore(BigDecimal balanceBBefore) {
        this.balanceBBefore = balanceBBefore;
    }

    public BigDecimal getBalanceAAfter() {
        return balanceAAfter;
    }

    public void setBalanceAAfter(BigDecimal balanceAAfter) {
        this.balanceAAfter = balanceAAfter;
    }

    public BigDecimal getBalanceBAfter() {
        return balanceBAfter;
    }

    public void setBalanceBAfter(BigDecimal balanceBAfter) {
        this.balanceBAfter = balanceBAfter;
    }

    public BigDecimal getAmountAtoB() {
        return amountAtoB;
    }

    public void setAmountAtoB(BigDecimal amountAtoB) {
        this.amountAtoB = amountAtoB;
    }

    public BigDecimal getAmountBtoA() {
        return amountBtoA;
    }

    public void setAmountBtoA(BigDecimal amountBtoA) {
        this.amountBtoA = amountBtoA;
    }

    public String getWinnerThread() {
        return winnerThread;
    }

    public void setWinnerThread(String winnerThread) {
        this.winnerThread = winnerThread;
    }

    public String getVictimThread() {
        return victimThread;
    }

    public void setVictimThread(String victimThread) {
        this.victimThread = victimThread;
    }

    public String getWinnerDirection() {
        return winnerDirection;
    }

    public void setWinnerDirection(String winnerDirection) {
        this.winnerDirection = winnerDirection;
    }

    public String getVictimDirection() {
        return victimDirection;
    }

    public void setVictimDirection(String victimDirection) {
        this.victimDirection = victimDirection;
    }

    public List<String> getThread1Logs() {
        return thread1Logs;
    }

    public void setThread1Logs(List<String> thread1Logs) {
        this.thread1Logs = thread1Logs;
    }

    public List<String> getThread2Logs() {
        return thread2Logs;
    }

    public void setThread2Logs(List<String> thread2Logs) {
        this.thread2Logs = thread2Logs;
    }

    public List<String> getCombinedLogs() {
        return combinedLogs;
    }

    public void setCombinedLogs(List<String> combinedLogs) {
        this.combinedLogs = combinedLogs;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
