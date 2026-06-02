package dev.distributed.bank.controller;

import dev.distributed.bank.service.ReplicationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/replication")
public class ReplicationController {

    private final ReplicationService replicationService;

    public ReplicationController(ReplicationService replicationService) {
        this.replicationService = replicationService;
    }

    @GetMapping("/status")
    public ResponseEntity<List<Map<String, Object>>> getReplicationStatus() {
        return ResponseEntity.ok(replicationService.getReplicationStatus());
    }

    @GetMapping("/compare/{branchId}")
    public ResponseEntity<Map<String, Object>> compareData(
            @PathVariable("branchId") String branchId) {
        return ResponseEntity.ok(replicationService.compareData(branchId.toUpperCase()));
    }

    @GetMapping("/compare-all")
    public ResponseEntity<List<Map<String, Object>>> compareAllData() {
        List<Map<String, Object>> results = List.of(
                replicationService.compareData("HN"),
                replicationService.compareData("DN"),
                replicationService.compareData("HCM"));
        return ResponseEntity.ok(results);
    }

    @PostMapping("/demo-lag/{branchId}")
    public ResponseEntity<Map<String, Object>> demoReplicationLag(
            @PathVariable("branchId") String branchId) {
        return ResponseEntity.ok(replicationService.demoReplicationLag(branchId.toUpperCase()));
    }
}
