package dev.distributed.bank.distributed;

import dev.distributed.bank.entity.DistributedTransactionLog;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

@Component
public class TransactionLogger {

    private final SiteRouter siteRouter;

    private final RowMapper<DistributedTransactionLog> logRowMapper = (rs, rowNum) -> {
        DistributedTransactionLog log = new DistributedTransactionLog();
        log.setTxnId(rs.getString("txn_id"));
        log.setTxnType(rs.getString("txn_type"));
        log.setStatus(rs.getString("status"));
        log.setSourceBranch(rs.getString("source_branch"));
        log.setDestBranch(rs.getString("dest_branch"));
        log.setAmount(rs.getBigDecimal("amount"));
        log.setSourceAccountId(rs.getLong("source_account_id"));
        log.setDestAccountId(rs.getLong("dest_account_id"));
        log.setErrorMessage(rs.getString("error_message"));
        log.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        log.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        return log;
    };

    public TransactionLogger(SiteRouter siteRouter) {
        this.siteRouter = siteRouter;
    }

    private void executeRaw(String branchId, String sql, Object... args) {
        DataSource ds = siteRouter.getJdbcTemplate(branchId).getDataSource();
        DataSource rawDs = ds;
        try {
            if (ds.isWrapperFor(javax.sql.DataSource.class)) {
                rawDs = ds.unwrap(javax.sql.DataSource.class);
            }
        } catch (SQLException ignored) {
        }

        try (Connection conn = rawDs.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            conn.setAutoCommit(true);
            for (int i = 0; i < args.length; i++) {
                ps.setObject(i + 1, args[i]);
            }
            int rows = ps.executeUpdate();
            System.out.println("[TransactionLogger] Executed: " + rows + " row(s) affected | SQL: "
                    + sql.substring(0, Math.min(sql.length(), 60)) + "...");
        } catch (SQLException e) {
            System.out.println("[TransactionLogger] ERROR: " + e.getMessage());
            throw new RuntimeException("Failed to log transaction: " + e.getMessage(), e);
        }
    }

    public void createTransactionLog(String txnId, String txnType, String status,
            String sourceBranch, String destBranch,
            java.math.BigDecimal amount,
            Long sourceAccountId, Long destAccountId) {
        executeRaw(sourceBranch,
                "INSERT INTO distributed_transaction_log " +
                        "(txn_id, txn_type, status, source_branch, dest_branch, amount, source_account_id, dest_account_id) "
                        +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                txnId, txnType, status, sourceBranch, destBranch, amount,
                sourceAccountId, destAccountId);
    }

    public void updateTransactionStatus(String txnId, String sourceBranch,
            String newStatus, String errorMessage) {
        if (errorMessage != null) {
            executeRaw(sourceBranch,
                    "UPDATE distributed_transaction_log SET status = ?, error_message = ? WHERE txn_id = ?",
                    newStatus, errorMessage, txnId);
        } else {
            executeRaw(sourceBranch,
                    "UPDATE distributed_transaction_log SET status = ? WHERE txn_id = ?",
                    newStatus, txnId);
        }
    }

    public void addParticipant(String txnId, String sourceBranch, String participantBranchId,
            String role, String status, String action) {
        try {
            executeRaw(sourceBranch,
                    "INSERT INTO transaction_participant (txn_id, branch_id, role, status, action) " +
                            "VALUES (?, ?, ?, ?, ?)",
                    txnId, participantBranchId, role, status, action);
        } catch (Exception e) {
            System.out.println("Cannot log participant at " + participantBranchId + ": " + e.getMessage());
        }
    }

    public void updateParticipantStatus(String txnId, String sourceBranch,
            String participantBranchId, String newStatus) {
        try {
            executeRaw(sourceBranch,
                    "UPDATE transaction_participant SET status = ? WHERE txn_id = ? AND branch_id = ?",
                    newStatus, txnId, participantBranchId);
        } catch (Exception e) {
            System.out.println("Cannot update participant at " + participantBranchId);
        }
    }

    public List<DistributedTransactionLog> getAllTransactionLogs() {
        List<DistributedTransactionLog> allLogs = new java.util.ArrayList<>();
        for (String branchId : siteRouter.getAllBranchIds()) {
            try {
                JdbcTemplate jdbc = siteRouter.getJdbcTemplate(branchId);
                List<DistributedTransactionLog> logs = jdbc.query(
                        "SELECT * FROM distributed_transaction_log ORDER BY created_at DESC",
                        logRowMapper);
                allLogs.addAll(logs);
            } catch (Exception e) {
            }
        }
        return allLogs;
    }
}
