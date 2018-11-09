package sample.controller;

import java.util.*;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import lombok.*;
import sample.ValidationException;
import sample.ValidationException.ErrorKeys;
import sample.context.actor.Actor;
import sample.context.security.*;
import sample.context.security.SecurityActorFinder.ActorDetails;
import sample.usecase.AccountService;

/**
 * 口座に関わる顧客のUI要求を処理します。
 */
@RestController
@RequestMapping("/api/account")
public class AccountController {

    private final AccountService service;
    private final SecurityProperties securityProps;
    
    public AccountController(AccountService service, SecurityProperties securityProps) {
        this.service = service;
        this.securityProps = securityProps;
    }

    /** ログイン状態を確認します。 */
    @GetMapping("/loginStatus")
    public boolean loginStatus() {
        return true;
    }

    /** 口座ログイン情報を取得します。 */
    @GetMapping("/loginAccount")
    public LoginAccount loadLoginAccount() {
        if (securityProps.auth().isEnabled()) {
            ActorDetails actorDetails = SecurityActorFinder.actorDetails()
                    .orElseThrow(() -> new ValidationException(ErrorKeys.Authentication));
            Actor actor = actorDetails.actor();
            return new LoginAccount(actor.getId(), actor.getName(), actorDetails.getAuthorityIds());
        } else { // for dummy login
            return new LoginAccount("sample", "sample", new ArrayList<>());
        }
    }

    /** クライアント利用用途に絞ったパラメタ */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LoginAccount {
        private String id;
        private String name;
        private Collection<String> authorities;
    }

    // for warning
    public ResponseEntity<Void> anyUsecase() {
        return ControllerUtils.resultEmpty(() -> service.hashCode());
    }

}
