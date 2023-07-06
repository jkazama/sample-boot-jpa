package sample.controller.admin;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import sample.context.audit.AuditActor;
import sample.context.audit.AuditActor.FindAuditActor;
import sample.context.audit.AuditEvent;
import sample.context.audit.AuditEvent.FindAuditEvent;
import sample.context.support.AppSetting;
import sample.context.support.AppSetting.FindAppSetting;
import sample.controller.ControllerUtils;
import sample.model.constraints.IdStr;
import sample.model.constraints.OutlineEmpty;
import sample.usecase.admin.SystemAdminService;

/**
 * Processes internal UI requests related to the system.
 */
@RestController
@RequestMapping("/api/admin/system")
@RequiredArgsConstructor
public class SystemAdminController {
    private final SystemAdminService service;

    /** Search actor audit logs. */
    @GetMapping(value = "/audit/actor")
    public Page<AuditActor> findAuditActor(@Valid FindAuditActor param) {
        return service.findAuditActor(param);
    }

    /** Search system event audit logs. */
    @GetMapping(value = "/audit/event")
    public Page<AuditEvent> findAuditEvent(@Valid FindAuditEvent param) {
        return service.findAuditEvent(param);
    }

    /** FInd application configuration information. */
    @GetMapping(value = "/setting")
    public List<AppSetting> findAppSetting(@Valid FindAppSetting param) {
        return service.findAppSetting(param);
    }

    /** Change application configuration information. */
    @PostMapping("/setting")
    public ResponseEntity<Void> changeAppSetting(@RequestBody @Valid ChgAppSetting param) {
        return ControllerUtils.resultEmpty(() -> service.changeAppSetting(param.id, param.value));
    }

    public static record ChgAppSetting(
            @IdStr(max = 120) String id,
            @OutlineEmpty(max = 1300) String value) {
    }

}
