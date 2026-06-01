package dev.distributed.bank.distributed;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.Arrays;
import java.util.List;

/**
 * SiteRouter — Bộ định tuyến trung tâm.
 *
 * Vai trò: Cho biết branchId nào → dùng JdbcTemplate nào.
 * Đây là thành phần quan trọng nhất trong hệ thống phân tán:
 * khi service cần truy vấn 1 site, nó hỏi SiteRouter để lấy
 * đúng JdbcTemplate (tức đúng database connection).
 *
 * Hỗ trợ Read/Write Splitting:
 * - getJdbcTemplate()      → Master (đọc/ghi)
 * - getSlaveJdbcTemplate() → Slave  (chỉ đọc)
 *
 * Ví dụ:
 * siteRouter.getJdbcTemplate("HN") → JdbcTemplate kết nối MySQL Master Hà Nội
 * siteRouter.getSlaveJdbcTemplate("HN") → JdbcTemplate kết nối MySQL Slave Hà Nội
 *
 * Tương đương trong thực tế: Tổng đài ngân hàng — bạn nói "chi nhánh Hà Nội",
 * tổng đài nối bạn đến đúng chi nhánh.
 */
@Component
public class SiteRouter {

    // ============================================================
    // MASTER connections — đọc/ghi
    // ============================================================
    private final JdbcTemplate hanoiJdbcTemplate;
    private final JdbcTemplate danangJdbcTemplate;
    private final JdbcTemplate hcmJdbcTemplate;

    private final PlatformTransactionManager hanoiTxManager;
    private final PlatformTransactionManager danangTxManager;
    private final PlatformTransactionManager hcmTxManager;

    // ============================================================
    // SLAVE connections — chỉ đọc (Replication)
    // ============================================================
    private final JdbcTemplate hanoiSlaveJdbcTemplate;
    private final JdbcTemplate danangSlaveJdbcTemplate;
    private final JdbcTemplate hcmSlaveJdbcTemplate;

    /** Flag mô phỏng site down — toggle qua API demo */
    private volatile String simulatedDownSite = null;

    public SiteRouter(
            @Qualifier("hanoiJdbcTemplate") JdbcTemplate hanoiJdbcTemplate,
            @Qualifier("danangJdbcTemplate") JdbcTemplate danangJdbcTemplate,
            @Qualifier("hcmJdbcTemplate") JdbcTemplate hcmJdbcTemplate,
            @Qualifier("hanoiTransactionManager") PlatformTransactionManager hanoiTxManager,
            @Qualifier("danangTransactionManager") PlatformTransactionManager danangTxManager,
            @Qualifier("hcmTransactionManager") PlatformTransactionManager hcmTxManager,
            @Qualifier("hanoiSlaveJdbcTemplate") JdbcTemplate hanoiSlaveJdbcTemplate,
            @Qualifier("danangSlaveJdbcTemplate") JdbcTemplate danangSlaveJdbcTemplate,
            @Qualifier("hcmSlaveJdbcTemplate") JdbcTemplate hcmSlaveJdbcTemplate) {
        this.hanoiJdbcTemplate = hanoiJdbcTemplate;
        this.danangJdbcTemplate = danangJdbcTemplate;
        this.hcmJdbcTemplate = hcmJdbcTemplate;
        this.hanoiTxManager = hanoiTxManager;
        this.danangTxManager = danangTxManager;
        this.hcmTxManager = hcmTxManager;
        this.hanoiSlaveJdbcTemplate = hanoiSlaveJdbcTemplate;
        this.danangSlaveJdbcTemplate = danangSlaveJdbcTemplate;
        this.hcmSlaveJdbcTemplate = hcmSlaveJdbcTemplate;
    }

    /**
     * Lấy JdbcTemplate MASTER theo branchId.
     * Dùng cho: INSERT, UPDATE, DELETE và SELECT yêu cầu dữ liệu chính xác.
     *
     * @param branchId Mã chi nhánh: "HN", "DN", "HCM"
     * @return JdbcTemplate kết nối đến MASTER database của chi nhánh đó
     * @throws dev.distributed.bank.exception.SiteDownException nếu site đang bị mô
     *                                                          phỏng down
     * @throws IllegalArgumentException                         nếu branchId không
     *                                                          hợp lệ
     */
    public JdbcTemplate getJdbcTemplate(String branchId) {
        // Kiểm tra site có đang bị mô phỏng down không
        if (branchId.equals(simulatedDownSite)) {
            System.out.println("⚠️ [FAILOVER] Master " + branchId + " is DOWN (simulated). Fallback to SLAVE for read availability.");
            return getSlaveJdbcTemplate(branchId);
        }

        return switch (branchId.toUpperCase()) {
            case "HN" -> hanoiJdbcTemplate;
            case "DN" -> danangJdbcTemplate;
            case "HCM" -> hcmJdbcTemplate;
            default -> throw new IllegalArgumentException(
                    "Unknown branch: " + branchId + ". Valid: HN, DN, HCM");
        };
    }

    /**
     * Lấy JdbcTemplate SLAVE theo branchId.
     * Dùng cho: SELECT truy vấn thống kê, báo cáo (Read/Write Splitting).
     *
     * Nếu Slave không khả dụng, tự động fallback về Master.
     *
     * @param branchId Mã chi nhánh: "HN", "DN", "HCM"
     * @return JdbcTemplate kết nối đến SLAVE database (read-only)
     */
    public JdbcTemplate getSlaveJdbcTemplate(String branchId) {
        // Lưu ý: Ngay cả khi Master (simulatedDownSite) down, Slave vẫn hoạt động để phục vụ đọc.

        try {
            JdbcTemplate slaveJdbc = switch (branchId.toUpperCase()) {
                case "HN" -> hanoiSlaveJdbcTemplate;
                case "DN" -> danangSlaveJdbcTemplate;
                case "HCM" -> hcmSlaveJdbcTemplate;
                default -> throw new IllegalArgumentException(
                        "Unknown branch: " + branchId + ". Valid: HN, DN, HCM");
            };

            // Kiểm tra Slave có khả dụng không
            slaveJdbc.queryForObject("SELECT 1", Integer.class);
            System.out.println("  📖 [READ] Đọc từ SLAVE " + branchId);
            return slaveJdbc;

        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            // Slave không khả dụng → fallback về Master
            System.out.println("  ⚠️ [FALLBACK] Slave " + branchId +
                    " không khả dụng, fallback về Master");
            return getJdbcTemplate(branchId);
        }
    }

    /**
     * Lấy TransactionManager theo branchId.
     * Dùng khi cần quản lý transaction thủ công (BEGIN/COMMIT/ROLLBACK).
     */
    public PlatformTransactionManager getTransactionManager(String branchId) {
        if (branchId.equals(simulatedDownSite)) {
            throw new dev.distributed.bank.exception.SiteDownException(
                    "Master Site " + branchId + " is DOWN (simulated). Cannot perform WRITE operations. System is operating in READ-ONLY mode via Slave.");
        }

        return switch (branchId.toUpperCase()) {
            case "HN" -> hanoiTxManager;
            case "DN" -> danangTxManager;
            case "HCM" -> hcmTxManager;
            default -> throw new IllegalArgumentException(
                    "Unknown branch: " + branchId);
        };
    }

    /**
     * Lấy JdbcTemplate MASTER của TẤT CẢ sites.
     * Dùng cho distributed query trên Master (khi cần dữ liệu chính xác).
     */
    public List<JdbcTemplate> getAllJdbcTemplates() {
        return Arrays.asList(hanoiJdbcTemplate, danangJdbcTemplate, hcmJdbcTemplate);
    }

    /**
     * Lấy JdbcTemplate SLAVE của TẤT CẢ sites.
     * Dùng cho distributed query trên Slave (thống kê, báo cáo).
     */
    public List<JdbcTemplate> getAllSlaveJdbcTemplates() {
        return Arrays.asList(hanoiSlaveJdbcTemplate, danangSlaveJdbcTemplate, hcmSlaveJdbcTemplate);
    }

    /** Lấy danh sách tất cả mã chi nhánh */
    public List<String> getAllBranchIds() {
        return Arrays.asList("HN", "DN", "HCM");
    }

    // ============================================================
    // Phần mô phỏng lỗi (Failure Simulation)
    // ============================================================

    /** Bật mô phỏng site down */
    public void simulateSiteDown(String branchId) {
        this.simulatedDownSite = branchId.toUpperCase();
        System.out.println("⚠️ [SIMULATION] Site " + branchId + " is now DOWN");
    }

    /** Tắt mô phỏng site down */
    public void clearSiteDown() {
        System.out.println("✅ [SIMULATION] All sites are now UP");
        this.simulatedDownSite = null;
    }

    /** Kiểm tra site nào đang bị mô phỏng down */
    public String getSimulatedDownSite() {
        return simulatedDownSite;
    }
}
