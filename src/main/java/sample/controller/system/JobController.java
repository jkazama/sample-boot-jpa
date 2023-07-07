package sample.controller.system;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import sample.controller.ControllerUtils;
import sample.usecase.admin.AssetAdminService;
import sample.usecase.admin.SystemAdminService;

/**
 * Processes UI requests for system jobs.
 * <p>
 * URLs after /api/system are assumed to be executed by the job scheduler, so
 * they must be made inaccessible from the outside by L/B or other means. (A
 * better approach would be to cut it out as a batch process with only the
 * relevant process or apply individual authentication.)
 * low: Usually, a batch process (or internal process) is created separately and
 * executed from the job scheduler.
 * It is important to design the job in advance so that the load of the job does
 * not affect the online side.
 * low: When cutting out internal/batch processes, it is necessary to be aware
 * of information/exclusive synchronization when distributing VMs. (DB
 * synchronization / messaging synchronization / use of distributed products,
 * etc.)
 */
@RestController
@RequestMapping("/api/system/job")
@RequiredArgsConstructor
public class JobController {
    private final AssetAdminService asset;
    private final SystemAdminService system;

    /** Move forward with the business day. */
    @PostMapping("/daily/forwardDay")
    public ResponseEntity<Void> forwardDay() {
        return ControllerUtils.resultEmpty(() -> system.forwardDay());
    }

    /** Close the withdrawal request. */
    @PostMapping("/daily/closingCashOut")
    public ResponseEntity<Void> closingCashOut() {
        return ControllerUtils.resultEmpty(() -> asset.closingCashOut());
    }

    /** Realize cash flow. */
    @PostMapping("/daily/realizeCashflow")
    public ResponseEntity<Void> realizeCashflow() {
        return ControllerUtils.resultEmpty(() -> asset.realizeCashflow());
    }

}
