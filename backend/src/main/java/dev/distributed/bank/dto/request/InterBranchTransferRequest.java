package dev.distributed.bank.dto.request;

import java.math.BigDecimal;

public class InterBranchTransferRequest {

    private String fromBranch;
    private Long fromAccountId;
    private String toBranch;
    private Long toAccountId;
    private BigDecimal amount;
    private boolean simulateCrash;

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

    public boolean isSimulateCrash() {
        return simulateCrash;
    }

    public void setSimulateCrash(boolean simulateCrash) {
        this.simulateCrash = simulateCrash;
    }
}
