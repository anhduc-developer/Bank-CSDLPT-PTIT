package dev.distributed.bank.controller;

import dev.distributed.bank.distributed.SiteRouter;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/demo")
public class DemoController {

    private final SiteRouter siteRouter;

    public DemoController(SiteRouter siteRouter) {
        this.siteRouter = siteRouter;
    }

    @PostMapping("/simulate-site-down")
    public ResponseEntity<?> simulateSiteDown(@RequestBody Map<String, Object> payload) {
        String branchId = (String) payload.get("branchId");
        Boolean enabled = (Boolean) payload.get("enabled");

        if (Boolean.TRUE.equals(enabled)) {
            siteRouter.simulateSiteDown(branchId);
            return ResponseEntity.ok().body(Map.of("message", "Master " + branchId + " is now simulated DOWN. System operating in Read-Only Fallback mode."));
        } else {
            siteRouter.clearSiteDown();
            return ResponseEntity.ok().body(Map.of("message", "All sites are now simulated UP."));
        }
    }
}
