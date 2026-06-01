package dev.distributed.bank.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class TransferResultResponse {

    private String transactionId;
    private String status;
    private String fromBranch;
    private Long fromAccountId;
    private String toBranch;
    private Long toAccountId;
    private BigDecimal amount;
    private BigDecimal sourceBalanceBefore;
    private BigDecimal destBalanceBefore;
    private BigDecimal sourceBalanceAfterDebit;
    private BigDecimal sourceBalanceAfter;
    private BigDecimal destBalanceAfter;
    private String message;
    private LocalDateTime timestamp;
    private List<String> logs;

    public TransferResultResponse() {
        this.timestamp = LocalDateTime.now();
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getFromBranch() {
        return fromBranch;
    }

    public void setFromBranch(String fromBranch) {
        this.fromBranch = fromBranch;
    }

    public Long getFromAccountId() {
        return fromAccountId;
    }

    public void setFromAccountId(Long fromAccountId) {
        this.fromAccountId = fromAccountId;
    }

    public String getToBranch() {
        return toBranch;
    }

    public void setToBranch(String toBranch) {
        this.toBranch = toBranch;
    }

    public Long getToAccountId() {
        return toAccountId;
    }

    public void setToAccountId(Long toAccountId) {
        this.toAccountId = toAccountId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public BigDecimal getSourceBalanceBefore() {
        return sourceBalanceBefore;
    }

    public void setSourceBalanceBefore(BigDecimal sourceBalanceBefore) {
        this.sourceBalanceBefore = sourceBalanceBefore;
    }

    public BigDecimal getDestBalanceBefore() {
        return destBalanceBefore;
    }

    public void setDestBalanceBefore(BigDecimal destBalanceBefore) {
        this.destBalanceBefore = destBalanceBefore;
    }

    public BigDecimal getSourceBalanceAfterDebit() {
        return sourceBalanceAfterDebit;
    }

    public void setSourceBalanceAfterDebit(BigDecimal sourceBalanceAfterDebit) {
        this.sourceBalanceAfterDebit = sourceBalanceAfterDebit;
    }

    public BigDecimal getSourceBalanceAfter() {
        return sourceBalanceAfter;
    }

    public void setSourceBalanceAfter(BigDecimal sourceBalanceAfter) {
        this.sourceBalanceAfter = sourceBalanceAfter;
    }

    public BigDecimal getDestBalanceAfter() {
        return destBalanceAfter;
    }

    public void setDestBalanceAfter(BigDecimal destBalanceAfter) {
        this.destBalanceAfter = destBalanceAfter;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public List<String> getLogs() {
        return logs;
    }

    public void setLogs(List<String> logs) {
        this.logs = logs;
    }
}
