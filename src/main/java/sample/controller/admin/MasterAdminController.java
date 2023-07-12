package sample.controller.admin;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import sample.context.actor.type.ActorRoleType;
import sample.controller.ControllerUtils;
import sample.model.master.Holiday;
import sample.model.master.Holiday.FindHoliday;
import sample.model.master.Holiday.RegHoliday;
import sample.usecase.admin.MasterAdminService;

/**
 * Handles internal UI requests related to the master.
 */
@RestController
@RequestMapping("/api/admin/master")
@PreAuthorize(ActorRoleType.AUTHORIZE_INTERNAL)
@RequiredArgsConstructor
public class MasterAdminController {
    private final MasterAdminService service;

    /** Find annual holidays. */
    @GetMapping("/holiday")
    public List<Holiday> findHoliday(@RequestBody @Valid FindHoliday param) {
        return service.findHoliday(param);
    }

    /** Register annual holidays. */
    @PostMapping("/holiday")
    public ResponseEntity<Void> registerHoliday(@RequestBody @Valid RegHoliday param) {
        return ControllerUtils.resultEmpty(() -> service.registerHoliday(param));
    }

}
