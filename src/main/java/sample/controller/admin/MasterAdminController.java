package sample.controller.admin;

import java.util.*;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import lombok.*;
import sample.ValidationException;
import sample.ValidationException.ErrorKeys;
import sample.context.actor.Actor;
import sample.context.security.*;
import sample.context.security.SecurityActorFinder.ActorDetails;
import sample.controller.ControllerSupport;
import sample.model.master.Holiday.RegHoliday;
import sample.usecase.MasterAdminService;

/**
 * API controller of the master domain in the organization.
 */
@RestController
@RequestMapping("/api/admin/master")
@Setter
public class MasterAdminController extends ControllerSupport {

    @Autowired
    private MasterAdminService service;
    @Autowired
    private SecurityProperties securityProps;

    @GetMapping("/loginStatus")
    public boolean loginStatus() {
        return true;
    }

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

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LoginStaff {
        private String id;
        private String name;
        private Collection<String> authorities;
    }

    @PostMapping("/holiday/")
    public ResponseEntity<Void> registerHoliday(@Valid RegHoliday p) {
        return resultEmpty(() -> service.registerHoliday(p));
    }

}
