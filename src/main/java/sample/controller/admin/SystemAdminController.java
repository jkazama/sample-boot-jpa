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
 * システムに関わる社内のUI要求を処理します。
 */
@RestController
@RequestMapping("/api/admin/system")
@Setter
public class SystemAdminController extends ControllerSupport {

    @Autowired
    private SystemAdminService service;

    /** 利用者監査ログを検索します。 */
    @GetMapping(value = "/audit/actor/")
    public PagingList<AuditActor> findAuditActor(@Valid FindAuditActor p) {
        return service.findAuditActor(p);
    }

    /** イベント監査ログを検索します。 */
    @GetMapping(value = "/audit/event/")
    public PagingList<AuditEvent> findAuditEvent(@Valid FindAuditEvent p) {
        return service.findAuditEvent(p);
    }

    /** アプリケーション設定一覧を検索します。 */
    @GetMapping(value = "/setting/")
    public List<AppSetting> findAppSetting(@Valid FindAppSetting p) {
        return service.findAppSetting(p);
    }

    /** アプリケーション設定情報を変更します。 */
    @PostMapping("/setting/{id}")
    public ResponseEntity<Void> changeAppSetting(@PathVariable String id, String value) {
        return resultEmpty(() -> service.changeAppSetting(id, value));
    }

}
