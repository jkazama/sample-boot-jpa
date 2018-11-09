package sample.controller.admin;

import java.util.*;

import javax.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import lombok.*;
import sample.ValidationException;
import sample.ValidationException.ErrorKeys;
import sample.context.actor.Actor;
import sample.context.security.*;
import sample.context.security.SecurityActorFinder.ActorDetails;
import sample.controller.ControllerUtils;
import sample.model.master.Holiday.RegHoliday;
import sample.usecase.MasterAdminService;

/**
 * マスタに関わる社内のUI要求を処理します。
 */
@RestController
@RequestMapping("/api/admin/master")
public class MasterAdminController {

    private final MasterAdminService service;
    private final SecurityProperties securityProps;

    public MasterAdminController(MasterAdminService service, SecurityProperties securityProps) {
        this.service = service;
        this.securityProps = securityProps;
    }

    /** 社員ログイン状態を確認します。 */
    @GetMapping("/loginStatus")
    public boolean loginStatus() {
        return true;
    }

    /** 社員ログイン情報を取得します。 */
    @GetMapping("/loginStaff")
    public LoginStaff loadLoginStaff() {
        if (securityProps.auth().isEnabled()) {
            ActorDetails actorDetails = SecurityActorFinder.actorDetails()
                    .orElseThrow(() -> new ValidationException(ErrorKeys.Authentication));
            Actor actor = actorDetails.actor();
            return new LoginStaff(actor.getId(), actor.getName(), actorDetails.getAuthorityIds());
        } else { // for dummy login
            return new LoginStaff("sample", "sample", new ArrayList<>());
        }
    }

    /** クライアント利用用途に絞ったパラメタ */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LoginStaff {
        private String id;
        private String name;
        private Collection<String> authorities;
    }

    /** 休日を登録します。 */
    @PostMapping("/holiday/")
    public ResponseEntity<Void> registerHoliday(@Valid RegHoliday p) {
        return ControllerUtils.resultEmpty(() -> service.registerHoliday(p));
    }

}
