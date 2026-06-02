package dev.distributed.bank.distributed;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.Arrays;
import java.util.List;

@Component
public class SiteRouter {
    private final JdbcTemplate hanoiJdbcTemplate;
    private final JdbcTemplate danangJdbcTemplate;
    private final JdbcTemplate hcmJdbcTemplate;

    private final PlatformTransactionManager hanoiTxManager;
    private final PlatformTransactionManager danangTxManager;
    private final PlatformTransactionManager hcmTxManager;

    private final JdbcTemplate hanoiSlaveJdbcTemplate;
    private final JdbcTemplate danangSlaveJdbcTemplate;
    private final JdbcTemplate hcmSlaveJdbcTemplate;

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

    public JdbcTemplate getJdbcTemplate(String branchId) {
        if (branchId.equals(simulatedDownSite)) {
            System.out.println("[FAILOVER] Master " + branchId
                    + " is DOWN (simulated). Fallback to SLAVE for read availability.");
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

    public JdbcTemplate getRawSlaveJdbcTemplate(String branchId) {
        return switch (branchId.toUpperCase()) {
            case "HN" -> hanoiSlaveJdbcTemplate;
            case "DN" -> danangSlaveJdbcTemplate;
            case "HCM" -> hcmSlaveJdbcTemplate;
            default -> throw new IllegalArgumentException(
                    "Unknown branch: " + branchId + ". Valid: HN, DN, HCM");
        };
    }

    public JdbcTemplate getSlaveJdbcTemplate(String branchId) {

        try {
            JdbcTemplate slaveJdbc = switch (branchId.toUpperCase()) {
                case "HN" -> hanoiSlaveJdbcTemplate;
                case "DN" -> danangSlaveJdbcTemplate;
                case "HCM" -> hcmSlaveJdbcTemplate;
                default -> throw new IllegalArgumentException(
                        "Unknown branch: " + branchId + ". Valid: HN, DN, HCM");
            };

            slaveJdbc.execute("SELECT 1 FROM account LIMIT 1");
            System.out.println(">>>>>>>>>>>>>[READ] Đọc từ SLAVE " + branchId);
            return slaveJdbc;

        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            System.out.println(">>>>>>>>>>>>>>[FALLBACK] Slave " + branchId +
                    " không khả dụng, fallback về Master");
            return getJdbcTemplate(branchId);
        }
    }

    public PlatformTransactionManager getTransactionManager(String branchId) {
        if (branchId.equals(simulatedDownSite)) {
            throw new dev.distributed.bank.exception.SiteDownException(
                    "Master Site " + branchId
                            + " is DOWN (simulated). Cannot perform WRITE operations. System is operating in READ-ONLY mode via Slave.");
        }

        return switch (branchId.toUpperCase()) {
            case "HN" -> hanoiTxManager;
            case "DN" -> danangTxManager;
            case "HCM" -> hcmTxManager;
            default -> throw new IllegalArgumentException(
                    "Unknown branch: " + branchId);
        };
    }

    public List<JdbcTemplate> getAllJdbcTemplates() {
        return Arrays.asList(hanoiJdbcTemplate, danangJdbcTemplate, hcmJdbcTemplate);
    }

    public List<JdbcTemplate> getAllSlaveJdbcTemplates() {
        return Arrays.asList(hanoiSlaveJdbcTemplate, danangSlaveJdbcTemplate, hcmSlaveJdbcTemplate);
    }

    public List<String> getAllBranchIds() {
        return Arrays.asList("HN", "DN", "HCM");
    }

    public void simulateSiteDown(String branchId) {
        this.simulatedDownSite = branchId.toUpperCase();
        System.out.println(">>>>>>>>>>>>>>>>[SIMULATION] Site " + branchId + " is now DOWN");
    }

    public void clearSiteDown() {
        System.out.println(">>>>>>>>>>>>>>>>>[SIMULATION] All sites are now UP");
        this.simulatedDownSite = null;
    }

    public String getSimulatedDownSite() {
        return simulatedDownSite;
    }
}
