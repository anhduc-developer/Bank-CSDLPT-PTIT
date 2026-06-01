package dev.distributed.bank.controller;

import dev.distributed.bank.dto.request.InterBranchTransferRequest;
import dev.distributed.bank.dto.request.InternalTransferRequest;
import dev.distributed.bank.dto.response.ApiResponse;
import dev.distributed.bank.dto.response.TransferResultResponse;
import dev.distributed.bank.service.TransferService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/transfers")
public class TransferController {

    private final TransferService transferService;

    public TransferController(TransferService transferService) {
        this.transferService = transferService;
    }

    @PostMapping("/internal")
    public ApiResponse<TransferResultResponse> internalTransfer(
            @RequestBody InternalTransferRequest request) {
        TransferResultResponse result = transferService.internalTransfer(request);
        return ApiResponse.ok("Internal transfer completed", result);
    }

    @PostMapping("/inter-branch")
    public ApiResponse<TransferResultResponse> interBranchTransfer(
            @RequestBody InterBranchTransferRequest request) {
        TransferResultResponse result = transferService.interBranchTransfer(request);

        if ("SUCCESS".equals(result.getStatus())) {
            return ApiResponse.ok("Inter-branch transfer committed (2PC)", result);
        } else {
            return new ApiResponse<>(false, result.getMessage(), result);
        }
    }
}
