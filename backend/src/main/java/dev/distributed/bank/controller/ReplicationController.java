package dev.distributed.bank.controller;

import dev.distributed.bank.service.ReplicationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Controller: API giám sát và demo Data Replication.
 *
 * Endpoints:
 * - GET  /api/replication/status          — Trạng thái replication 3 cặp M-S
 * - GET  /api/replication/compare/{branch} — So sánh Master vs Slave
 * - POST /api/replication/demo-lag/{branch} — Demo replication lag
 */
@RestController
@RequestMapping("/api/replication")
public class ReplicationController {

    private final ReplicationService replicationService;

    public ReplicationController(ReplicationService replicationService) {
        this.replicationService = replicationService;
    }

    /**
     * GET /api/replication/status
     * Trả về trạng thái Slave Replication của cả 3 chi nhánh.
     */
    @GetMapping("/status")
    public ResponseEntity<List<Map<String, Object>>> getReplicationStatus() {
        return ResponseEntity.ok(replicationService.getReplicationStatus());
    }

    /**
     * GET /api/replication/compare/{branchId}
     * So sánh dữ liệu (COUNT) giữa Master và Slave cho 1 chi nhánh.
     */
    @GetMapping("/compare/{branchId}")
    public ResponseEntity<Map<String, Object>> compareData(
            @PathVariable String branchId) {
        return ResponseEntity.ok(replicationService.compareData(branchId.toUpperCase()));
    }

    /**
     * GET /api/replication/compare-all
     * So sánh dữ liệu Master vs Slave cho TẤT CẢ chi nhánh.
     */
    @GetMapping("/compare-all")
    public ResponseEntity<List<Map<String, Object>>> compareAllData() {
        List<Map<String, Object>> results = List.of(
                replicationService.compareData("HN"),
                replicationService.compareData("DN"),
                replicationService.compareData("HCM")
        );
        return ResponseEntity.ok(results);
    }

    /**
     * POST /api/replication/demo-lag/{branchId}
     * Demo replication lag: ghi vào Master, đọc từ Slave để đo delay.
     */
    @PostMapping("/demo-lag/{branchId}")
    public ResponseEntity<Map<String, Object>> demoReplicationLag(
            @PathVariable String branchId) {
        return ResponseEntity.ok(replicationService.demoReplicationLag(branchId.toUpperCase()));
    }
}
