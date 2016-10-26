package sample.controller.admin;

import java.util.List;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import lombok.Setter;
import sample.context.AppSetting;
import sample.context.AppSetting.FindAppSetting;
import sample.context.audit.*;
import sample.context.audit.AuditActor.FindAuditActor;
import sample.context.audit.AuditEvent.FindAuditEvent;
import sample.context.orm.PagingList;
import sample.controller.ControllerSupport;
import sample.usecase.SystemAdminService;

/**
 * API controller of the system domain in the organization.
 */
@RestController
@RequestMapping("/api/admin/system")
@Setter
public class SystemAdminController extends ControllerSupport {

    @Autowired
    private SystemAdminService service;

    @GetMapping(value = "/audit/actor/")
    public PagingList<AuditActor> findAuditActor(@Valid FindAuditActor p) {
        return service.findAuditActor(p);
    }

    @GetMapping(value = "/audit/event/")
    public PagingList<AuditEvent> findAuditEvent(@Valid FindAuditEvent p) {
        return service.findAuditEvent(p);
    }

    @GetMapping(value = "/setting/")
    public List<AppSetting> findAppSetting(@Valid FindAppSetting p) {
        return service.findAppSetting(p);
    }

    @PostMapping("/setting/{id}")
    public ResponseEntity<Void> changeAppSetting(@PathVariable String id, String value) {
        return resultEmpty(() -> service.changeAppSetting(id, value));
    }

}
