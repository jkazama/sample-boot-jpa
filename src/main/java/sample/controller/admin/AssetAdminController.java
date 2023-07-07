package sample.controller.admin;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import sample.model.asset.CashInOut;
import sample.model.asset.CashInOut.FindCashInOut;
import sample.usecase.admin.AssetAdminService;

/**
 * Processes internal UI requests for assets.
 */
@RestController
@RequestMapping("/api/admin/asset")
@RequiredArgsConstructor
public class AssetAdminController {
    private final AssetAdminService service;

    /** Searches for unprocessed transfer request information. */
    @GetMapping("/cio")
    public List<CashInOut> findCashInOut(@Valid FindCashInOut p) {
        return service.findCashInOut(p);
    }

}
