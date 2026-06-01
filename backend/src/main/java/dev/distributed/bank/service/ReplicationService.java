package dev.distributed.bank.service;

import dev.distributed.bank.distributed.SiteRouter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Service: Giám sát và demo Replication.
 *
 * Chức năng:
 * 1. Xem trạng thái Slave Replication (IO Thread, SQL Thread, Lag)
 * 2. So sánh dữ liệu Master vs Slave (đếm bản ghi)
 * 3. Demo Replication Lag — ghi Master, đọc Slave ngay lập tức
 */
@Service
public class ReplicationService {

    private final SiteRouter siteRouter;

    public ReplicationService(SiteRouter siteRouter) {
        this.siteRouter = siteRouter;
    }

    // ============================================================
    // 1. Trạng thái Replication của tất cả sites
    // ============================================================

    /**
     * Lấy trạng thái SHOW REPLICA STATUS từ mỗi Slave.
     * (MySQL 8.4+ dùng SHOW REPLICA STATUS thay cho SHOW SLAVE STATUS)
     * Trả về danh sách 3 objects (HN, DN, HCM) với thông tin:
     * - Replica_IO_Running
     * - Replica_SQL_Running
     * - Seconds_Behind_Source
     * - Source_Host
     * - Relay_Log_File
     * - Last_Error
     */
    public List<Map<String, Object>> getReplicationStatus() {
        System.out.println("═══ [REPLICATION] Checking Replication Status ═══");

        List<Map<String, Object>> statusList = new ArrayList<>();
        Map<String, String> branchNames = Map.of(
                "HN", "Chi nhánh Hà Nội",
                "DN", "Chi nhánh Đà Nẵng",
                "HCM", "Chi nhánh TP.HCM");

        for (String branchId : siteRouter.getAllBranchIds()) {
            Map<String, Object> status = new LinkedHashMap<>();
            status.put("branchId", branchId);
            status.put("branchName", branchNames.getOrDefault(branchId, branchId));

            try {
                JdbcTemplate slaveJdbc = siteRouter.getSlaveJdbcTemplate(branchId);

                // Chạy SHOW REPLICA STATUS trên Slave (MySQL 8.4+)
                List<Map<String, Object>> slaveStatus = slaveJdbc.queryForList("SHOW REPLICA STATUS");

                if (!slaveStatus.isEmpty()) {
                    Map<String, Object> ss = slaveStatus.get(0);

                    // MySQL 8.4+ dùng Replica_IO_Running, Source_Host, v.v.
                    status.put("slaveIORunning", ss.get("Replica_IO_Running"));
                    status.put("slaveSQLRunning", ss.get("Replica_SQL_Running"));
                    status.put("secondsBehindMaster", ss.get("Seconds_Behind_Source"));
                    status.put("masterHost", ss.get("Source_Host"));
                    status.put("masterPort", ss.get("Source_Port"));
                    status.put("relayLogFile", ss.get("Relay_Log_File"));
                    status.put("masterLogFile", ss.get("Source_Log_File"));
                    status.put("readMasterLogPos", ss.get("Read_Source_Log_Pos"));
                    status.put("execMasterLogPos", ss.get("Exec_Source_Log_Pos"));
                    status.put("lastIOError", ss.get("Last_IO_Error"));
                    status.put("lastSQLError", ss.get("Last_SQL_Error"));
                    status.put("slaveIOState", ss.get("Replica_IO_State"));
                    status.put("replicationConnected", true);

                    String ioRunning = String.valueOf(ss.get("Replica_IO_Running"));
                    String sqlRunning = String.valueOf(ss.get("Replica_SQL_Running"));
                    status.put("healthy", "Yes".equals(ioRunning) && "Yes".equals(sqlRunning));

                    System.out.println("  Site " + branchId + " Slave: IO=" + ioRunning +
                            ", SQL=" + sqlRunning +
                            ", Lag=" + ss.get("Seconds_Behind_Source") + "s");
                } else {
                    status.put("replicationConnected", false);
                    status.put("healthy", false);
                    status.put("error", "SHOW REPLICA STATUS trả về rỗng — Slave chưa được cấu hình");
                    System.out.println("  Site " + branchId + " Slave: NOT CONFIGURED");
                }

            } catch (Exception e) {
                status.put("replicationConnected", false);
                status.put("healthy", false);
                status.put("error", "Không kết nối được Slave: " + e.getMessage());
                System.out.println("  Site " + branchId + " Slave: UNREACHABLE - " + e.getMessage());
            }

            statusList.add(status);
        }

        System.out.println("═══════════════════════════════════════════");
        return statusList;
    }

    // ============================================================
    // 2. So sánh dữ liệu Master vs Slave
    // ============================================================

    /**
     * So sánh COUNT(*) trên mỗi bảng giữa Master và Slave của 1 chi nhánh.
     * Cho thấy dữ liệu đã được nhân bản đầy đủ hay chưa.
     */
    public Map<String, Object> compareData(String branchId) {
        System.out.println("═══ [REPLICATION] Comparing Master vs Slave — " + branchId + " ═══");

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("branchId", branchId);
        result.put("timestamp", LocalDateTime.now().toString());

        String[] tables = { "branch", "customer", "account", "transaction_history",
                "distributed_transaction_log", "transaction_participant" };

        List<Map<String, Object>> tableComparisons = new ArrayList<>();

        try {
            JdbcTemplate masterJdbc = siteRouter.getJdbcTemplate(branchId);
            JdbcTemplate slaveJdbc = siteRouter.getSlaveJdbcTemplate(branchId);

            for (String table : tables) {
                Map<String, Object> comparison = new LinkedHashMap<>();
                comparison.put("table", table);

                try {
                    Integer masterCount = masterJdbc.queryForObject(
                            "SELECT COUNT(*) FROM " + table, Integer.class);
                    Integer slaveCount = slaveJdbc.queryForObject(
                            "SELECT COUNT(*) FROM " + table, Integer.class);

                    comparison.put("masterCount", masterCount);
                    comparison.put("slaveCount", slaveCount);
                    comparison.put("inSync", Objects.equals(masterCount, slaveCount));
                    comparison.put("difference", Math.abs(
                            (masterCount != null ? masterCount : 0) -
                                    (slaveCount != null ? slaveCount : 0)));

                    System.out.println("  " + table + ": Master=" + masterCount +
                            ", Slave=" + slaveCount +
                            (Objects.equals(masterCount, slaveCount) ? " ✅" : " ❌ KHÁC BIỆT"));

                } catch (Exception e) {
                    comparison.put("error", e.getMessage());
                    System.out.println("  " + table + ": LỖI - " + e.getMessage());
                }

                tableComparisons.add(comparison);
            }

            // Tổng kết
            boolean allInSync = tableComparisons.stream()
                    .allMatch(tc -> Boolean.TRUE.equals(tc.get("inSync")));
            result.put("allInSync", allInSync);

        } catch (Exception e) {
            result.put("error", "Không thể kết nối: " + e.getMessage());
        }

        result.put("tables", tableComparisons);
        System.out.println("═══════════════════════════════════════════");

        return result;
    }

    // ============================================================
    // 3. Demo Replication Lag
    // ============================================================

    /**
     * Demo Replication Lag:
     * 1. Ghi 1 record vào Master (transaction_history)
     * 2. Đọc ngay lập tức từ Slave — có thể chưa thấy
     * 3. Đợi 1 giây → đọc lại từ Slave — đã thấy
     *
     * Kết quả cho thấy thời gian trễ thực tế của replication.
     */
    public Map<String, Object> demoReplicationLag(String branchId) {
        System.out.println("═══ [REPLICATION] Demo Replication Lag — " + branchId + " ═══");

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("branchId", branchId);
        List<Map<String, Object>> steps = new ArrayList<>();

        try {
            JdbcTemplate masterJdbc = siteRouter.getJdbcTemplate(branchId);
            JdbcTemplate slaveJdbc = siteRouter.getSlaveJdbcTemplate(branchId);

            // Step 1: Đếm trước khi ghi
            Integer masterCountBefore = masterJdbc.queryForObject(
                    "SELECT COUNT(*) FROM transaction_history", Integer.class);
            Integer slaveCountBefore = slaveJdbc.queryForObject(
                    "SELECT COUNT(*) FROM transaction_history", Integer.class);

            Map<String, Object> step1 = new LinkedHashMap<>();
            step1.put("step", 1);
            step1.put("action", "ĐẾM BẢN GHI TRƯỚC KHI GHI");
            step1.put("masterCount", masterCountBefore);
            step1.put("slaveCount", slaveCountBefore);
            step1.put("timestamp", System.currentTimeMillis());
            steps.add(step1);

            System.out.println("  Step 1: Master=" + masterCountBefore + ", Slave=" + slaveCountBefore);

            // Step 2: Ghi 1 record mới vào Master
            long writeStartMs = System.currentTimeMillis();

            // Lấy account_id hợp lệ đầu tiên
            Long accountId = masterJdbc.queryForObject(
                    "SELECT account_id FROM account LIMIT 1", Long.class);

            masterJdbc.update(
                    "INSERT INTO transaction_history " +
                            "(transaction_type, amount, account_id, balance_after, status, description) " +
                            "VALUES ('DEPOSIT', 1.00, ?, 0.00, 'SUCCESS', ?)",
                    accountId,
                    "[REPLICATION TEST] Ghi lúc " + LocalDateTime.now());

            long writeEndMs = System.currentTimeMillis();

            Integer masterCountAfterWrite = masterJdbc.queryForObject(
                    "SELECT COUNT(*) FROM transaction_history", Integer.class);

            Map<String, Object> step2 = new LinkedHashMap<>();
            step2.put("step", 2);
            step2.put("action", "GHI 1 BẢN GHI VÀO MASTER");
            step2.put("masterCount", masterCountAfterWrite);
            step2.put("writeTimeMs", writeEndMs - writeStartMs);
            step2.put("timestamp", writeEndMs);
            steps.add(step2);

            System.out.println("  Step 2: Ghi xong Master=" + masterCountAfterWrite +
                    " (" + (writeEndMs - writeStartMs) + "ms)");

            // Step 3: Đọc ngay lập tức từ Slave (có thể bị lag)
            Integer slaveCountImmediate = slaveJdbc.queryForObject(
                    "SELECT COUNT(*) FROM transaction_history", Integer.class);
            long readImmedMs = System.currentTimeMillis();

            boolean foundImmediate = Objects.equals(masterCountAfterWrite, slaveCountImmediate);

            Map<String, Object> step3 = new LinkedHashMap<>();
            step3.put("step", 3);
            step3.put("action", "ĐỌC NGAY TỪ SLAVE (0ms delay)");
            step3.put("slaveCount", slaveCountImmediate);
            step3.put("masterCount", masterCountAfterWrite);
            step3.put("replicated", foundImmediate);
            step3.put("delayMs", readImmedMs - writeEndMs);
            step3.put("timestamp", readImmedMs);
            steps.add(step3);

            System.out.println("  Step 3: Slave=" + slaveCountImmediate +
                    " | " + (foundImmediate ? "ĐÃ ĐỒNG BỘ ✅" : "CHƯA ĐỒNG BỘ ⏳"));

            // Step 4: Đợi rồi đọc lại
            if (!foundImmediate) {
                // Polling until replicated or timeout
                boolean replicated = false;
                int attempts = 0;
                int maxAttempts = 10;
                long totalWaitMs = 0;

                while (!replicated && attempts < maxAttempts) {
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException ignored) {
                    }
                    totalWaitMs += 200;
                    attempts++;

                    Integer slaveCountRetry = slaveJdbc.queryForObject(
                            "SELECT COUNT(*) FROM transaction_history", Integer.class);
                    replicated = Objects.equals(masterCountAfterWrite, slaveCountRetry);

                    if (replicated) {
                        Map<String, Object> step4 = new LinkedHashMap<>();
                        step4.put("step", 4);
                        step4.put("action", "SLAVE ĐÃ ĐỒNG BỘ SAU " + totalWaitMs + "ms");
                        step4.put("slaveCount", slaveCountRetry);
                        step4.put("replicationLagMs", totalWaitMs);
                        step4.put("attempts", attempts);
                        step4.put("timestamp", System.currentTimeMillis());
                        steps.add(step4);

                        result.put("replicationLagMs", totalWaitMs);
                        System.out.println("  Step 4: Đồng bộ sau " + totalWaitMs + "ms ✅");
                    }
                }

                if (!replicated) {
                    Map<String, Object> step4 = new LinkedHashMap<>();
                    step4.put("step", 4);
                    step4.put("action", "SLAVE VẪN CHƯA ĐỒNG BỘ SAU " + totalWaitMs + "ms");
                    step4.put("replicationLagMs", ">" + totalWaitMs);
                    step4.put("timestamp", System.currentTimeMillis());
                    steps.add(step4);

                    result.put("replicationLagMs", ">" + totalWaitMs);
                    System.out.println("  Step 4: Vẫn chưa đồng bộ sau " + totalWaitMs + "ms ⚠️");
                }
            } else {
                result.put("replicationLagMs", 0);

                Map<String, Object> step4 = new LinkedHashMap<>();
                step4.put("step", 4);
                step4.put("action", "SLAVE ĐÃ ĐỒNG BỘ NGAY LẬP TỨC (< 1ms)");
                step4.put("replicationLagMs", 0);
                step4.put("timestamp", System.currentTimeMillis());
                steps.add(step4);
            }

        } catch (Exception e) {
            result.put("error", e.getMessage());
            System.out.println("  LỖI: " + e.getMessage());
        }

        result.put("steps", steps);
        System.out.println("═══════════════════════════════════════════");

        return result;
    }
}
