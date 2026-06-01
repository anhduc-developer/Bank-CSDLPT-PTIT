package dev.distributed.bank.service;

import dev.distributed.bank.distributed.SiteRouter;
import dev.distributed.bank.dto.request.CreateAccountRequest;
import dev.distributed.bank.dto.request.DepositRequest;
import dev.distributed.bank.dto.request.WithdrawRequest;
import dev.distributed.bank.dto.response.BalanceResponse;
import dev.distributed.bank.dto.response.ConcurrentWithdrawResponse;
import dev.distributed.bank.entity.Account;
import dev.distributed.bank.entity.TransactionHistory;
import dev.distributed.bank.exception.AccountInactiveException;
import dev.distributed.bank.exception.AccountNotFoundException;
import dev.distributed.bank.exception.InsufficientBalanceException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.*;
import java.util.concurrent.*;

@Service
public class AccountService {

    private final SiteRouter siteRouter;
    private final RowMapper<Account> accountRowMapper = (rs, rowNum) -> {
        Account a = new Account();
        a.setAccountId(rs.getLong("account_id"));
        a.setCustomerId(rs.getLong("customer_id"));
        a.setBranchId(rs.getString("branch_id"));
        a.setBalance(rs.getBigDecimal("balance"));
        a.setStatus(rs.getString("status"));
        a.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        return a;
    };

    /** RowMapper cho TransactionHistory */
    private final RowMapper<TransactionHistory> txnRowMapper = (rs, rowNum) -> {
        TransactionHistory t = new TransactionHistory();
        t.setTransactionId(rs.getLong("transaction_id"));
        t.setTransactionType(rs.getString("transaction_type"));
        t.setAmount(rs.getBigDecimal("amount"));
        t.setAccountId(rs.getLong("account_id"));

        // Nullable fields
        long relatedAccId = rs.getLong("related_account_id");
        t.setRelatedAccountId(rs.wasNull() ? null : relatedAccId);
        t.setRelatedBranchId(rs.getString("related_branch_id"));
        t.setBalanceAfter(rs.getBigDecimal("balance_after"));
        t.setStatus(rs.getString("status"));
        t.setDistributedTxnId(rs.getString("distributed_txn_id"));
        t.setDescription(rs.getString("description"));
        t.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        return t;
    };

    public AccountService(SiteRouter siteRouter) {
        this.siteRouter = siteRouter;
    }

    public List<Account> getAccountsByBranch(String branchId) {
        JdbcTemplate jdbc = siteRouter.getJdbcTemplate(branchId);
        return jdbc.query("SELECT * FROM account ORDER BY account_id", accountRowMapper);
    }

    public Account getAccountById(Long id, String branchId) {
        JdbcTemplate jdbc = siteRouter.getJdbcTemplate(branchId);
        List<Account> accounts = jdbc.query(
                "SELECT * FROM account WHERE account_id = ?",
                accountRowMapper, id);
        if (accounts.isEmpty()) {
            throw new AccountNotFoundException(
                    "Account " + id + " not found at branch " + branchId);
        }
        return accounts.get(0);
    }

    public Account createAccount(CreateAccountRequest request) {
        JdbcTemplate jdbc = siteRouter.getJdbcTemplate(request.getBranchId());
        BigDecimal initialBalance = request.getInitialBalance() != null
                ? request.getInitialBalance()
                : BigDecimal.ZERO;

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO account (customer_id, branch_id, balance, status) VALUES (?, ?, ?, 'ACTIVE')",
                    Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, request.getCustomerId());
            ps.setString(2, request.getBranchId().toUpperCase());
            ps.setBigDecimal(3, initialBalance);
            return ps;
        }, keyHolder);

        Long newId = keyHolder.getKey().longValue();
        return getAccountById(newId, request.getBranchId());
    }

    public BalanceResponse getBalance(Long accountId, String branchId) {
        JdbcTemplate jdbc = siteRouter.getJdbcTemplate(branchId);
        List<BalanceResponse> results = jdbc.query(
                "SELECT a.account_id, a.branch_id, a.balance, a.status, c.full_name " +
                        "FROM account a JOIN customer c ON a.customer_id = c.customer_id " +
                        "WHERE a.account_id = ?",
                (rs, rowNum) -> new BalanceResponse(
                        rs.getLong("account_id"),
                        rs.getString("branch_id"),
                        rs.getBigDecimal("balance"),
                        rs.getString("full_name"),
                        rs.getString("status")),
                accountId);
        if (results.isEmpty()) {
            throw new AccountNotFoundException(
                    "Account " + accountId + " not found at branch " + branchId);
        }
        return results.get(0);
    }

    private void checkAccountActive(JdbcTemplate jdbc, Long accountId) {
        String status = jdbc.queryForObject(
                "SELECT status FROM account WHERE account_id = ?",
                String.class, accountId);
        if (status == null) {
            throw new AccountNotFoundException("Account " + accountId + " not found");
        }
        if (!"ACTIVE".equals(status)) {
            throw new AccountInactiveException(
                    "Tài khoản " + accountId + " đang ở trạng thái " + status +
                            ". Chỉ tài khoản ACTIVE mới được phép giao dịch.");
        }
    }

    public Account deposit(DepositRequest request) {
        if (request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
        String branchId = request.getBranchId();

        JdbcTemplate jdbc = siteRouter.getJdbcTemplate(branchId);

        PlatformTransactionManager txManager = siteRouter.getTransactionManager(branchId);
        checkAccountActive(jdbc, request.getAccountId());

        TransactionStatus txStatus = txManager.getTransaction(new DefaultTransactionDefinition());

        try {

            System.out.println();
            System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
            System.out.println("DEPOSIT TRANSACTION");
            System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");

            BigDecimal beforeBalance = jdbc.queryForObject(
                    "SELECT balance FROM account WHERE account_id = ?",
                    BigDecimal.class,
                    request.getAccountId());

            System.out.println("BEFORE DEPOSIT");
            System.out.println("Balance = " + beforeBalance);

            jdbc.update(
                    "UPDATE account SET balance = balance + ? WHERE account_id = ?",
                    request.getAmount(),
                    request.getAccountId());

            BigDecimal afterUpdateBalance = jdbc.queryForObject(
                    "SELECT balance FROM account WHERE account_id = ?",
                    BigDecimal.class,
                    request.getAccountId());

            System.out.println();
            System.out.println("AFTER UPDATE (CHƯA COMMIT)");
            System.out.println("Deposit Amount = " + request.getAmount());
            System.out.println("Balance = " + afterUpdateBalance);

            jdbc.update(
                    "INSERT INTO transaction_history "
                            + "(transaction_type, amount, account_id, balance_after, status, description) "
                            + "VALUES ('DEPOSIT', ?, ?, ?, 'SUCCESS', 'Nạp tiền')",
                    request.getAmount(),
                    request.getAccountId(),
                    afterUpdateBalance);

            // if (true) {
            // System.out.println();
            // System.out.println("TRANSACTION HISTORY INSERTED");
            // System.out.println();
            // System.out.println("SERVER CRASH BEFORE COMMIT!");
            // Thread.sleep(3000);
            // throw new RuntimeException("SERVER CRASH");
            // }
            txManager.commit(txStatus);

            System.out.println();
            System.out.println("COMMIT SUCCESS");

            BigDecimal finalBalance = jdbc.queryForObject(
                    "SELECT balance FROM account WHERE account_id = ?",
                    BigDecimal.class,
                    request.getAccountId());

            System.out.println();
            System.out.println("AFTER COMMIT");
            System.out.println("Balance = " + finalBalance);

            System.out.println("══════════════════════════════════════");
            System.out.println();

            return getAccountById(request.getAccountId(), branchId);

        } catch (Exception e) {

            txManager.rollback(txStatus);

            System.out.println();
            System.out.println("ROLLBACK TRANSACTION");

            BigDecimal rollbackBalance = jdbc.queryForObject(
                    "SELECT balance FROM account WHERE account_id = ?",
                    BigDecimal.class,
                    request.getAccountId());

            System.out.println();
            System.out.println("AFTER ROLLBACK");
            System.out.println("Balance = " + rollbackBalance);

            System.out.println("══════════════════════════════════════");
            System.out.println();

            throw new RuntimeException(e.getMessage());
        }
    }

    public Account withdraw(WithdrawRequest request) {
        if (request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }

        String branchId = request.getBranchId();
        JdbcTemplate jdbc = siteRouter.getJdbcTemplate(branchId);

        checkAccountActive(jdbc, request.getAccountId());

        PlatformTransactionManager txManager = siteRouter.getTransactionManager(branchId);
        TransactionStatus txStatus = txManager.getTransaction(new DefaultTransactionDefinition());

        try {
            BigDecimal currentBalance = jdbc.queryForObject(
                    "SELECT balance FROM account WHERE account_id = ? FOR UPDATE",
                    BigDecimal.class, request.getAccountId());

            if (currentBalance == null) {
                throw new AccountNotFoundException(
                        "Account " + request.getAccountId() + " not found");
            }

            if (currentBalance.compareTo(request.getAmount()) < 0) {
                throw new InsufficientBalanceException(
                        "Insufficient balance. Current: " + currentBalance +
                                ", Requested: " + request.getAmount());
            }

            jdbc.update(
                    "UPDATE account SET balance = balance - ? WHERE account_id = ?",
                    request.getAmount(), request.getAccountId());

            BigDecimal newBalance = currentBalance.subtract(request.getAmount());

            jdbc.update(
                    "INSERT INTO transaction_history (transaction_type, amount, account_id, balance_after, status, description) "
                            +
                            "VALUES ('WITHDRAW', ?, ?, ?, 'SUCCESS', 'Rút tiền')",
                    request.getAmount(), request.getAccountId(), newBalance);

            txManager.commit(txStatus);

            System.out.println("[WITHDRAW] Account " + request.getAccountId() +
                    " at " + branchId + ": -" + request.getAmount() +
                    " → Balance = " + newBalance);

            return getAccountById(request.getAccountId(), branchId);

        } catch (InsufficientBalanceException | AccountNotFoundException e) {
            txManager.rollback(txStatus);
            throw e;
        } catch (Exception e) {
            txManager.rollback(txStatus);
            throw new RuntimeException("Withdraw failed: " + e.getMessage(), e);
        }
    }

    public ConcurrentWithdrawResponse concurrentWithdraw(WithdrawRequest request) {

        String branchId = request.getBranchId();
        Long accountId = request.getAccountId();

        BigDecimal amountThread1 = request.getAmountThread1();
        BigDecimal amountThread2 = request.getAmountThread2();

        boolean useLock = request.isUseLock();

        JdbcTemplate jdbc = siteRouter.getJdbcTemplate(branchId);

        if (amountThread1 == null || amountThread1.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Số tiền Thread 1 phải lớn hơn 0");
        }

        if (amountThread2 == null || amountThread2.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Số tiền Thread 2 phải lớn hơn 0");
        }

        checkAccountActive(jdbc, accountId);

        BigDecimal totalWithdraw = amountThread1.add(amountThread2);

        List<String> logs = Collections.synchronizedList(new ArrayList<>());

        ConcurrentWithdrawResponse response = new ConcurrentWithdrawResponse();

        BigDecimal initialBalance = jdbc.queryForObject(
                "SELECT balance FROM account WHERE account_id = ?",
                BigDecimal.class,
                accountId);

        response.setInitialBalance(initialBalance);

        String mode;

        if (useLock) {
            mode = "CÓ LOCK";
        } else {
            mode = "KHÔNG LOCK";
        }

        System.out.println();

        System.out.println("==========================================");
        System.out.println("MÔ PHỎNG RÚT TIỀN ĐỒNG THỜI");
        System.out.println("==========================================");

        System.out.println("Tài khoản          : " + accountId);
        System.out.println("Chi nhánh          : " + branchId);
        System.out.println("Chế độ             : " + mode);
        System.out.println("Số dư ban đầu      : " + initialBalance);
        System.out.println("Thread 1 rút       : " + amountThread1);
        System.out.println("Thread 2 rút       : " + amountThread2);
        System.out.println("Tổng tiền rút      : " + totalWithdraw);

        System.out.println("==========================================");

        logs.add("BẮT ĐẦU RÚT TIỀN ĐỒNG THỜI");
        logs.add("Chế độ = " + mode);
        logs.add("Số dư ban đầu = " + initialBalance);

        ExecutorService executor = Executors.newFixedThreadPool(2);

        CountDownLatch startLatch = new CountDownLatch(1);

        CyclicBarrier readBarrier = new CyclicBarrier(2);

        List<Future<String>> futures = new ArrayList<>();

        // T1
        futures.add(executor.submit(() -> {

            logs.add("[T1] ĐÃ SẴN SÀNG");

            startLatch.await();

            if (useLock) {

                return withdrawWithLock(
                        branchId,
                        accountId,
                        amountThread1,
                        1,
                        logs);

            } else {

                return withdrawWithoutLock(
                        branchId,
                        accountId,
                        amountThread1,
                        1,
                        logs,
                        readBarrier);
            }
        }));

        // T2
        futures.add(executor.submit(() -> {

            logs.add("[T2] ĐÃ SẴN SÀNG");

            startLatch.await();

            if (useLock) {

                return withdrawWithLock(
                        branchId,
                        accountId,
                        amountThread2,
                        2,
                        logs);

            } else {

                return withdrawWithoutLock(
                        branchId,
                        accountId,
                        amountThread2,
                        2,
                        logs,
                        readBarrier);
            }
        }));

        startLatch.countDown();

        String resultThread1 = "TIMEOUT";
        String resultThread2 = "TIMEOUT";

        try {

            resultThread1 = futures.get(0).get(15, TimeUnit.SECONDS);

        } catch (Exception e) {

            logs.add("[T1] ERROR: " + e.getMessage());
        }

        try {

            resultThread2 = futures.get(1).get(15, TimeUnit.SECONDS);

        } catch (Exception e) {

            logs.add("[T2] ERROR: " + e.getMessage());
        }

        executor.shutdown();

        BigDecimal finalBalance = jdbc.queryForObject(
                "SELECT balance FROM account WHERE account_id = ?",
                BigDecimal.class,
                accountId);

        BigDecimal expectedBalance = initialBalance;

        if (resultThread1.equals("SUCCESS")) {
            expectedBalance = expectedBalance.subtract(amountThread1);
        }

        if (resultThread2.equals("SUCCESS")) {
            expectedBalance = expectedBalance.subtract(amountThread2);
        }

        boolean lostUpdate = false;

        if (finalBalance.compareTo(expectedBalance) != 0) {
            lostUpdate = true;
        }

        logs.add(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
        logs.add("Số dư mong đợi = " + expectedBalance);
        logs.add("Số dư thực tế  = " + finalBalance);

        if (lostUpdate) {

            logs.add("PHÁT HIỆN LOST UPDATE");

            BigDecimal diff = finalBalance.subtract(expectedBalance);

            logs.add("Chênh lệch = " + diff);

            logs.add("Một transaction đã bị ghi đè");

        } else {

            logs.add("Dữ liệu chính xác");
        }

        System.out.println("------------------------------------------");

        System.out.println("Số dư mong đợi : " + expectedBalance);
        System.out.println("Số dư thực tế  : " + finalBalance);

        if (lostUpdate) {

            System.out.println("PHÁT HIỆN LOST UPDATE");

        } else {

            System.out.println("DỮ LIỆU CHÍNH XÁC");
        }

        System.out.println("==========================================");

        System.out.println();

        response.setAccount(
                getAccountById(accountId, branchId));

        response.setFinalBalance(finalBalance);

        response.setExpectedBalance(expectedBalance);

        response.setLostUpdate(lostUpdate);

        response.setThread1Result(resultThread1);

        response.setThread2Result(resultThread2);

        response.setLogs(logs);

        return response;
    }

    private String withdrawWithLock(
            String branchId,
            Long accountId,
            BigDecimal amount,
            int threadNum,
            List<String> logs) {

        String tag = "[T" + threadNum + "]";

        PlatformTransactionManager txManager = siteRouter.getTransactionManager(branchId);

        TransactionStatus txStatus = txManager.getTransaction(new DefaultTransactionDefinition());

        try {

            JdbcTemplate threadJdbc = siteRouter.getJdbcTemplate(branchId);

            logs.add(tag + " Đang chờ lock...");

            System.out.println(tag + " Đang chờ lock...");

            BigDecimal balance = threadJdbc.queryForObject(
                    "SELECT balance FROM account WHERE account_id = ? FOR UPDATE",
                    BigDecimal.class,
                    accountId);

            logs.add(tag + " Đã lock thành công");
            logs.add(tag + " Balance hiện tại = " + balance);

            System.out.println(tag + " Đã lock thành công");
            System.out.println(tag + " Balance hiện tại = " + balance);

            if (balance.compareTo(amount) < 0) {

                txManager.rollback(txStatus);

                String msg = tag + " THẤT BẠI - Không đủ tiền";

                logs.add(msg);

                System.out.println(msg);

                return "FAILED";
            }

            threadJdbc.update(
                    "UPDATE account SET balance = balance - ? WHERE account_id = ?",
                    amount,
                    accountId);

            BigDecimal newBalance = balance.subtract(amount);

            threadJdbc.update(
                    "INSERT INTO transaction_history " +
                            "(transaction_type, amount, account_id, balance_after, status, description) " +
                            "VALUES ('WITHDRAW', ?, ?, ?, 'SUCCESS', ?)",
                    amount,
                    accountId,
                    newBalance,
                    "Rút tiền concurrent có lock");

            txManager.commit(txStatus);

            String msg = tag + " Rút tiền thành công";

            logs.add(msg);
            logs.add(tag + " Balance mới = " + newBalance);

            System.out.println(msg);
            System.out.println(tag + " Balance mới = " + newBalance);

            return "SUCCESS";

        } catch (Exception e) {

            if (!txStatus.isCompleted()) {
                txManager.rollback(txStatus);
            }

            String msg = tag + " ERROR: " + e.getMessage();

            logs.add(msg);

            System.out.println(msg);

            return "ERROR";
        }
    }

    private String withdrawWithoutLock(
            String branchId,
            Long accountId,
            BigDecimal amount,
            int threadNum,
            List<String> logs,
            CyclicBarrier readBarrier) {

        String tag = "[T" + threadNum + "]";

        try {

            JdbcTemplate threadJdbc = siteRouter.getJdbcTemplate(branchId);

            logs.add(tag + " Đọc balance KHÔNG LOCK");

            System.out.println(tag + " Đọc balance KHÔNG LOCK");

            BigDecimal balance = threadJdbc.queryForObject(
                    "SELECT balance FROM account WHERE account_id = ?",
                    BigDecimal.class,
                    accountId);

            logs.add(tag + " Balance đọc được = " + balance);

            System.out.println(tag + " Balance đọc được = " + balance);

            readBarrier.await(5, TimeUnit.SECONDS);

            if (balance.compareTo(amount) < 0) {

                String msg = tag + " THẤT BẠI - Không đủ tiền";

                logs.add(msg);

                System.out.println(msg);

                return "FAILED";
            }

            BigDecimal newBalance = balance.subtract(amount);

            threadJdbc.update(
                    "UPDATE account SET balance = ? WHERE account_id = ?",
                    newBalance,
                    accountId);
            threadJdbc.update(
                    "INSERT INTO transaction_history " +
                            "(transaction_type, amount, account_id, balance_after, status, description) " +
                            "VALUES ('WITHDRAW', ?, ?, ?, 'SUCCESS', ?)",
                    amount,
                    accountId,
                    newBalance,
                    "Rút tiền concurrent không lock");

            String msg = tag + " Rút tiền thành công";

            logs.add(msg);

            logs.add(tag + " Balance đọc được = " + balance);
            logs.add(tag + " Balance sau update = " + newBalance);

            System.out.println(msg);

            System.out.println(tag + " Balance đọc được = " + balance);
            System.out.println(tag + " Balance sau update = " + newBalance);

            return "SUCCESS";

        } catch (Exception e) {

            String msg = tag + " ERROR: " + e.getMessage();

            logs.add(msg);

            System.out.println(msg);

            return "ERROR";
        }
    }

    public List<TransactionHistory> getTransactionHistory(Long accountId, String branchId) {
        JdbcTemplate jdbc = siteRouter.getJdbcTemplate(branchId);
        return jdbc.query(
                "SELECT * FROM transaction_history WHERE account_id = ? ORDER BY created_at DESC",
                txnRowMapper, accountId);
    }

    public Account updateAccountStatus(Long accountId, String branchId, String status) {
        JdbcTemplate jdbc = siteRouter.getJdbcTemplate(branchId);
        jdbc.update("UPDATE account SET status = ? WHERE account_id = ?", status, accountId);
        return getAccountById(accountId, branchId);
    }
}
