package sample.controller.system;

import lombok.Setter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import sample.controller.ControllerSupport;
import sample.usecase.*;

/**
 * API controller of the system job.
 * <p>the URL after "/api/system" assumes what is carried out from job scheduler,
 * it is necessary to make it inaccessible from the outside in L/B.
 */
@RestController
@RequestMapping("/api/system/job")
@Setter
public class JobController extends ControllerSupport {

    @Autowired
    private AssetAdminService asset;
    @Autowired
    private SystemAdminService system;

    @PostMapping("/daily/processDay")
    public ResponseEntity<Void> processDay() {
        return resultEmpty(() -> system.processDay());
    }

    @PostMapping("/daily/closingCashOut")
    public ResponseEntity<Void> closingCashOut() {
        return resultEmpty(() -> asset.closingCashOut());
    }

    @PostMapping("/daily/realizeCashflow")
    public ResponseEntity<Void> realizeCashflow() {
        return resultEmpty(() -> asset.realizeCashflow());
    }

}
