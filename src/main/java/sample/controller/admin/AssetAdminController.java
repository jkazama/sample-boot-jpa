package sample.controller.admin;

import java.util.List;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import lombok.Setter;
import sample.controller.ControllerSupport;
import sample.model.asset.CashInOut;
import sample.model.asset.CashInOut.FindCashInOut;
import sample.usecase.AssetAdminService;

/**
 * API controller of the asset domain in the organization.
 */
@RestController
@RequestMapping("/api/admin/asset")
@Setter
public class AssetAdminController extends ControllerSupport {

    @Autowired
    private AssetAdminService service;

    @GetMapping("/cio/")
    public List<CashInOut> findCashInOut(@Valid FindCashInOut p) {
        return service.findCashInOut(p);
    }

}
