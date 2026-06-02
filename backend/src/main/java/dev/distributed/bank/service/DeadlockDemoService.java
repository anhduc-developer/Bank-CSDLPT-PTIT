package dev.distributed.bank.service;

import dev.distributed.bank.distributed.SiteRouter;
import dev.distributed.bank.dto.request.DeadlockDemoRequest;
import dev.distributed.bank.dto.response.DeadlockDemoResponse;
import org.springframework.dao.DeadlockLoserDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class DeadlockDemoService {

        private final SiteRouter siteRouter;

        public DeadlockDemoService(SiteRouter siteRouter) {
                this.siteRouter = siteRouter;
        }

        public DeadlockDemoResponse simulateDeadlock(DeadlockDemoRequest request) {

                String branchId = request.getBranchId().toUpperCase();
                Long accountAId = request.getAccountAId();
                Long accountBId = request.getAccountBId();
                BigDecimal amountAtoB = request.getAmountAtoB();
                BigDecimal amountBtoA = request.getAmountBtoA();

                JdbcTemplate jdbc = siteRouter.getJdbcTemplate(branchId);
                PlatformTransactionManager txManager = siteRouter.getTransactionManager(branchId);

                List<String> combinedLogs = Collections.synchronizedList(new ArrayList<>());
                List<String> thread1Logs = Collections.synchronizedList(new ArrayList<>());
                List<String> thread2Logs = Collections.synchronizedList(new ArrayList<>());

                // Kết quả của từng thread
                AtomicBoolean thread1Success = new AtomicBoolean(false);
                AtomicBoolean thread2Success = new AtomicBoolean(false);
                AtomicReference<String> thread1Error = new AtomicReference<>(null);
                AtomicReference<String> thread2Error = new AtomicReference<>(null);

                // Latch để đồng bộ 2 thread — đảm bảo cả 2 đã lock xong record đầu tiên
                CountDownLatch bothLockedFirst = new CountDownLatch(2);

                BigDecimal balanceA = jdbc.queryForObject(
                                "SELECT balance FROM account WHERE account_id = ?",
                                BigDecimal.class, accountAId);

                BigDecimal balanceB = jdbc.queryForObject(
                                "SELECT balance FROM account WHERE account_id = ?",
                                BigDecimal.class, accountBId);

                System.out.println();
                System.out.println("╔══════════════════════════════════════════════════════════╗");
                System.out.println("║                DEMO DEADLOCK — BẮT ĐẦU                   ║");
                System.out.println("╚══════════════════════════════════════════════════════════╝");
                System.out.println();
                System.out.println("  Chi nhánh  : " + branchId);
                System.out.println("  Account A  : #" + accountAId + " (Số dư: " + balanceA + ")");
                System.out.println("  Account B  : #" + accountBId + " (Số dư: " + balanceB + ")");
                System.out.println("  Thread-1   : A→B " + amountAtoB);
                System.out.println("  Thread-2   : B→A " + amountBtoA);
                System.out.println();
                System.out.println("  Thread-1: A chuyển cho B (Lock A trước → Lock B sau)");
                System.out.println("  Thread-2: B chuyển cho A (Lock B trước → Lock A sau)");
                System.out.println("──────────────────────────────────────────────────────────");

                addLog(combinedLogs, thread1Logs, null, "THREAD-1",
                                "BẮT ĐẦU: Account #" + accountAId + " → Account #" + accountBId + " | Số tiền: "
                                                + amountAtoB);
                addLog(combinedLogs, thread2Logs, null, "THREAD-2",
                                "BẮT ĐẦU: Account #" + accountBId + " → Account #" + accountAId + " | Số tiền: "
                                                + amountBtoA);
                addLog(combinedLogs, null, null, "SYSTEM",
                                "Số dư ban đầu: Account #" + accountAId + " = " + balanceA + " | Account #" + accountBId
                                                + " = " + balanceB);

                // THREAD 1: A → B (lock A trước, lock B sau)
                CompletableFuture<Void> future1 = CompletableFuture.runAsync(() -> {
                        TransactionStatus tx = txManager.getTransaction(new DefaultTransactionDefinition());
                        try {
                                // STEP 1: Lock Account A
                                addLog(combinedLogs, thread1Logs, null, "THREAD-1",
                                                "Đang lock Account #" + accountAId + " (SELECT ... FOR UPDATE)");

                                System.out.println("[Thread-1] LOCK Account #" + accountAId + "...");

                                jdbc.queryForObject(
                                                "SELECT balance FROM account WHERE account_id = ? FOR UPDATE",
                                                BigDecimal.class, accountAId);

                                addLog(combinedLogs, thread1Logs, null, "THREAD-1",
                                                "Đã lock Account #" + accountAId + " thành công!");

                                System.out.println("[Thread-1] LOCKED Account #" + accountAId + " thành công!");

                                bothLockedFirst.countDown();

                                addLog(combinedLogs, thread1Logs, null, "THREAD-1",
                                                "Chờ Thread-2 lock Account #" + accountBId + "...");
                                bothLockedFirst.await();

                                Thread.sleep(200);

                                addLog(combinedLogs, thread1Logs, null, "THREAD-1",
                                                "Đang cố lock Account #" + accountBId + " (SELECT ... FOR UPDATE)...");

                                System.out.println("[Thread-1] Đang cố lock Account #" + accountBId
                                                + "... (Thread-2 đang giữ!)");

                                jdbc.queryForObject(
                                                "SELECT balance FROM account WHERE account_id = ? FOR UPDATE",
                                                BigDecimal.class, accountBId);

                                addLog(combinedLogs, thread1Logs, null, "THREAD-1",
                                                "Lock Account #" + accountBId + " thành công! (Thread này THẮNG)");

                                System.out.println("[Thread-1] LOCKED Account #" + accountBId + " — WINNER!");

                                addLog(combinedLogs, thread1Logs, null, "THREAD-1",
                                                "Trừ " + amountAtoB + " từ Account #" + accountAId);

                                jdbc.update("UPDATE account SET balance = balance - ? WHERE account_id = ?",
                                                amountAtoB, accountAId);

                                addLog(combinedLogs, thread1Logs, null, "THREAD-1",
                                                "Cộng " + amountAtoB + " vào Account #" + accountBId);

                                jdbc.update("UPDATE account SET balance = balance + ? WHERE account_id = ?",
                                                amountAtoB, accountBId);

                                BigDecimal balanceAAfterT1 = jdbc.queryForObject(
                                                "SELECT balance FROM account WHERE account_id = ?",
                                                BigDecimal.class, accountAId);
                                BigDecimal balanceBAfterT1 = jdbc.queryForObject(
                                                "SELECT balance FROM account WHERE account_id = ?",
                                                BigDecimal.class, accountBId);

                                jdbc.update(
                                                "INSERT INTO transaction_history " +
                                                                "(transaction_type, amount, account_id, related_account_id, balance_after, status, description) "
                                                                +
                                                                "VALUES ('TRANSFER_OUT', ?, ?, ?, ?, 'SUCCESS', ?)",
                                                amountAtoB, accountAId, accountBId, balanceAAfterT1,
                                                "Deadlock demo: A→B thành công");

                                jdbc.update(
                                                "INSERT INTO transaction_history " +
                                                                "(transaction_type, amount, account_id, related_account_id, balance_after, status, description) "
                                                                +
                                                                "VALUES ('TRANSFER_IN', ?, ?, ?, ?, 'SUCCESS', ?)",
                                                amountAtoB, accountBId, accountAId, balanceBAfterT1,
                                                "Deadlock demo: nhận tiền từ A→B");

                                addLog(combinedLogs, thread1Logs, null, "THREAD-1",
                                                "Đã ghi lịch sử giao dịch cho cả 2 tài khoản");

                                txManager.commit(tx);
                                thread1Success.set(true);

                                addLog(combinedLogs, thread1Logs, null, "THREAD-1",
                                                "COMMIT thành công! Chuyển tiền A→B hoàn tất.");

                                System.out.println("[Thread-1] COMMIT thành công!");

                        } catch (DeadlockLoserDataAccessException e) {
                                addLog(combinedLogs, thread1Logs, null, "THREAD-1",
                                                "DEADLOCK DETECTED! MySQL chọn Thread-1 làm VICTIM");
                                addLog(combinedLogs, thread1Logs, null, "THREAD-1",
                                                "MySQL tự động ROLLBACK transaction của Thread-1");
                                addLog(combinedLogs, thread1Logs, null, "THREAD-1",
                                                "Error: " + e.getMessage());

                                System.out.println("[Thread-1] DEADLOCK! → MySQL ROLLBACK Thread-1 (VICTIM)");

                                thread1Error.set("DEADLOCK_VICTIM");

                                try {
                                        if (!tx.isCompleted()) {
                                                txManager.rollback(tx);
                                        }
                                } catch (Exception ignored) {
                                }

                                try {
                                        BigDecimal currentBalanceA = jdbc.queryForObject(
                                                        "SELECT balance FROM account WHERE account_id = ?",
                                                        BigDecimal.class, accountAId);
                                        jdbc.update(
                                                        "INSERT INTO transaction_history " +
                                                                        "(transaction_type, amount, account_id, related_account_id, balance_after, status, description) "
                                                                        +
                                                                        "VALUES ('TRANSFER_OUT', ?, ?, ?, ?, 'FAILED', ?)",
                                                        amountAtoB, accountAId, accountBId, currentBalanceA,
                                                        "Deadlock victim: giao dịch bị MySQL rollback");
                                } catch (Exception ignored) {
                                }

                        } catch (Exception e) {
                                addLog(combinedLogs, thread1Logs, null, "THREAD-1",
                                                "Lỗi: " + e.getMessage());

                                System.out.println("[Thread-1] Error: " + e.getMessage());

                                thread1Error.set(e.getMessage());

                                try {
                                        if (!tx.isCompleted()) {
                                                txManager.rollback(tx);
                                        }
                                } catch (Exception ignored) {
                                }
                        }
                });

                // THREAD 2: B → A (lock B trước, lock A sau) — NGƯỢC THỨ TỰ!
                CompletableFuture<Void> future2 = CompletableFuture.runAsync(() -> {
                        TransactionStatus tx = txManager.getTransaction(new DefaultTransactionDefinition());
                        try {
                                // Delay nhỏ để Thread 1 lock A trước
                                Thread.sleep(100);

                                // STEP 1: Lock Account B
                                addLog(combinedLogs, thread2Logs, null, "THREAD-2",
                                                "Đang lock Account #" + accountBId + " (SELECT ... FOR UPDATE)");

                                System.out.println("[Thread-2] LOCK Account #" + accountBId + "...");

                                jdbc.queryForObject(
                                                "SELECT balance FROM account WHERE account_id = ? FOR UPDATE",
                                                BigDecimal.class, accountBId);

                                addLog(combinedLogs, thread2Logs, null, "THREAD-2",
                                                "Đã lock Account #" + accountBId + " thành công!");

                                System.out.println("[Thread-2] LOCKED Account #" + accountBId + " thành công!");

                                bothLockedFirst.countDown();

                                addLog(combinedLogs, thread2Logs, null, "THREAD-2",
                                                "Chờ Thread-1 lock Account #" + accountAId + "...");
                                bothLockedFirst.await();

                                Thread.sleep(200);

                                // STEP 2: Cố lock Account A → SẼ BỊ BLOCK (vì Thread 1 đang giữ)
                                addLog(combinedLogs, thread2Logs, null, "THREAD-2",
                                                "Đang cố lock Account #" + accountAId + " (SELECT ... FOR UPDATE)...");

                                System.out.println("[Thread-2] Đang cố lock Account #" + accountAId
                                                + "... (Thread-1 đang giữ!)");

                                jdbc.queryForObject(
                                                "SELECT balance FROM account WHERE account_id = ? FOR UPDATE",
                                                BigDecimal.class, accountAId);

                                // Nếu đến được đây → thread này là WINNER
                                addLog(combinedLogs, thread2Logs, null, "THREAD-2",
                                                "Lock Account #" + accountAId + " thành công! (Thread này THẮNG)");

                                System.out.println("[Thread-2] LOCKED Account #" + accountAId + " — WINNER!");

                                addLog(combinedLogs, thread2Logs, null, "THREAD-2",
                                                "Trừ " + amountBtoA + " từ Account #" + accountBId);

                                jdbc.update("UPDATE account SET balance = balance - ? WHERE account_id = ?",
                                                amountBtoA, accountBId);

                                addLog(combinedLogs, thread2Logs, null, "THREAD-2",
                                                "Cộng " + amountBtoA + " vào Account #" + accountAId);

                                jdbc.update("UPDATE account SET balance = balance + ? WHERE account_id = ?",
                                                amountBtoA, accountAId);

                                BigDecimal balanceBAfterT2 = jdbc.queryForObject(
                                                "SELECT balance FROM account WHERE account_id = ?",
                                                BigDecimal.class, accountBId);
                                BigDecimal balanceAAfterT2 = jdbc.queryForObject(
                                                "SELECT balance FROM account WHERE account_id = ?",
                                                BigDecimal.class, accountAId);

                                jdbc.update(
                                                "INSERT INTO transaction_history " +
                                                                "(transaction_type, amount, account_id, related_account_id, balance_after, status, description) "
                                                                +
                                                                "VALUES ('TRANSFER_OUT', ?, ?, ?, ?, 'SUCCESS', ?)",
                                                amountBtoA, accountBId, accountAId, balanceBAfterT2,
                                                "Deadlock demo: B→A thành công");
                                jdbc.update(
                                                "INSERT INTO transaction_history " +
                                                                "(transaction_type, amount, account_id, related_account_id, balance_after, status, description) "
                                                                +
                                                                "VALUES ('TRANSFER_IN', ?, ?, ?, ?, 'SUCCESS', ?)",
                                                amountBtoA, accountAId, accountBId, balanceAAfterT2,
                                                "Deadlock demo: nhận tiền từ B→A");

                                addLog(combinedLogs, thread2Logs, null, "THREAD-2",
                                                "Đã ghi lịch sử giao dịch cho cả 2 tài khoản");

                                // COMMIT
                                txManager.commit(tx);
                                thread2Success.set(true);

                                addLog(combinedLogs, thread2Logs, null, "THREAD-2",
                                                "COMMIT thành công! Chuyển tiền B→A hoàn tất.");

                                System.out.println("[Thread-2] COMMIT thành công!");

                        } catch (DeadlockLoserDataAccessException e) {
                                // Thread này là VICTIM
                                addLog(combinedLogs, thread2Logs, null, "THREAD-2",
                                                "DEADLOCK DETECTED! MySQL chọn Thread-2 làm VICTIM");
                                addLog(combinedLogs, thread2Logs, null, "THREAD-2",
                                                "MySQL tự động ROLLBACK transaction của Thread-2");
                                addLog(combinedLogs, thread2Logs, null, "THREAD-2",
                                                "Error: " + e.getMessage());

                                System.out.println("[Thread-2] DEADLOCK! → MySQL ROLLBACK Thread-2 (VICTIM)");

                                thread2Error.set("DEADLOCK_VICTIM");

                                try {
                                        if (!tx.isCompleted()) {
                                                txManager.rollback(tx);
                                        }
                                } catch (Exception ignored) {
                                }

                                try {
                                        BigDecimal currentBalanceB = jdbc.queryForObject(
                                                        "SELECT balance FROM account WHERE account_id = ?",
                                                        BigDecimal.class, accountBId);
                                        jdbc.update(
                                                        "INSERT INTO transaction_history " +
                                                                        "(transaction_type, amount, account_id, related_account_id, balance_after, status, description) "
                                                                        +
                                                                        "VALUES ('TRANSFER_OUT', ?, ?, ?, ?, 'FAILED', ?)",
                                                        amountBtoA, accountBId, accountAId, currentBalanceB,
                                                        "Deadlock victim: giao dịch bị MySQL rollback");
                                } catch (Exception ignored) {
                                }

                        } catch (Exception e) {
                                addLog(combinedLogs, thread2Logs, null, "THREAD-2",
                                                "Lỗi: " + e.getMessage());

                                System.out.println("[Thread-2] Error: " + e.getMessage());

                                thread2Error.set(e.getMessage());

                                try {
                                        if (!tx.isCompleted()) {
                                                txManager.rollback(tx);
                                        }
                                } catch (Exception ignored) {
                                }
                        }
                });

                // CHỜ CẢ 2 THREAD HOÀN TẤT
                CompletableFuture.allOf(future1, future2).join();

                // ĐỌC SỐ DƯ SAU CÙNG
                BigDecimal balanceAAfter = jdbc.queryForObject(
                                "SELECT balance FROM account WHERE account_id = ?",
                                BigDecimal.class, accountAId);

                BigDecimal balanceBAfter = jdbc.queryForObject(
                                "SELECT balance FROM account WHERE account_id = ?",
                                BigDecimal.class, accountBId);

                addLog(combinedLogs, null, null, "SYSTEM",
                                "Số dư sau cùng: Account #" + accountAId + " = " + balanceAAfter
                                                + " | Account #" + accountBId + " = " + balanceBAfter);

                // XÁC ĐỊNH WINNER / VICTIM
                String winnerThread;
                String victimThread;
                String winnerDirection;
                String victimDirection;

                if (thread1Success.get() && !thread2Success.get()) {
                        winnerThread = "Thread-1";
                        victimThread = "Thread-2";
                        winnerDirection = "A → B (Account #" + accountAId + " → #" + accountBId + ")";
                        victimDirection = "B → A (Account #" + accountBId + " → #" + accountAId + ")";
                } else if (!thread1Success.get() && thread2Success.get()) {
                        winnerThread = "Thread-2";
                        victimThread = "Thread-1";
                        winnerDirection = "B → A (Account #" + accountBId + " → #" + accountAId + ")";
                        victimDirection = "A → B (Account #" + accountAId + " → #" + accountBId + ")";
                } else {
                        winnerThread = "N/A";
                        victimThread = "N/A";
                        winnerDirection = "N/A";
                        victimDirection = "N/A";
                }

                addLog(combinedLogs, null, null, "SYSTEM",
                                "WINNER: " + winnerThread + " (" + winnerDirection + ")");
                addLog(combinedLogs, null, null, "SYSTEM",
                                "VICTIM: " + victimThread + " (" + victimDirection + ") — Đã bị MySQL ROLLBACK");

                System.out.println();
                System.out.println("══════════════════════════════════════════════════════════");
                System.out.println("  KẾT QUẢ DEMO DEADLOCK");
                System.out.println("══════════════════════════════════════════════════════════");
                System.out.println("  Winner : " + winnerThread + " — " + winnerDirection);
                System.out.println("  Victim : " + victimThread + " — " + victimDirection);
                System.out.println();
                System.out.println("  Số dư Account #" + accountAId + ": " + balanceA + " → " + balanceAAfter);
                System.out.println("  Số dư Account #" + accountBId + ": " + balanceB + " → " + balanceBAfter);
                System.out.println("══════════════════════════════════════════════════════════");
                System.out.println();

                DeadlockDemoResponse response = new DeadlockDemoResponse();
                response.setStatus("DEADLOCK_DETECTED");
                response.setBranchId(branchId);
                response.setAccountAId(accountAId);
                response.setAccountBId(accountBId);
                response.setAmountAtoB(amountAtoB);
                response.setAmountBtoA(amountBtoA);
                response.setBalanceABefore(balanceA);
                response.setBalanceBBefore(balanceB);
                response.setBalanceAAfter(balanceAAfter);
                response.setBalanceBAfter(balanceBAfter);
                response.setWinnerThread(winnerThread);
                response.setVictimThread(victimThread);
                response.setWinnerDirection(winnerDirection);
                response.setVictimDirection(victimDirection);
                response.setThread1Logs(new ArrayList<>(thread1Logs));
                response.setThread2Logs(new ArrayList<>(thread2Logs));
                response.setCombinedLogs(new ArrayList<>(combinedLogs));

                return response;
        }

        private void addLog(List<String> combinedLogs,
                        List<String> threadLogs,
                        @SuppressWarnings("unused") Void unused,
                        String thread,
                        String message) {

                String timestamp = java.time.LocalTime.now()
                                .format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss.SSS"));

                String logEntry = "[" + timestamp + "] [" + thread + "] " + message;

                combinedLogs.add(logEntry);

                if (threadLogs != null) {
                        threadLogs.add(logEntry);
                }

                System.out.println(logEntry);
        }
}