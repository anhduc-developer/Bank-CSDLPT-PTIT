package dev.distributed.bank.controller;

import dev.distributed.bank.dto.request.DeadlockDemoRequest;
import dev.distributed.bank.dto.response.ApiResponse;
import dev.distributed.bank.dto.response.DeadlockDemoResponse;
import dev.distributed.bank.service.DeadlockDemoService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/demo")
public class DeadlockDemoController {

    private final DeadlockDemoService deadlockDemoService;

    public DeadlockDemoController(DeadlockDemoService deadlockDemoService) {
        this.deadlockDemoService = deadlockDemoService;
    }

    @PostMapping("/deadlock")
    public ApiResponse<DeadlockDemoResponse> simulateDeadlock(
            @RequestBody DeadlockDemoRequest request) {

        DeadlockDemoResponse result = deadlockDemoService.simulateDeadlock(request);
        return ApiResponse.ok("Deadlock demo completed", result);
    }
}
