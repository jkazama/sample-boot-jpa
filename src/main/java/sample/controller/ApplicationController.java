package sample.controller;

import java.util.Collection;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.Builder;
import sample.context.Dto;
import sample.context.actor.Actor;
import sample.context.actor.ActorSession;
import sample.context.actor.type.ActorRoleType;

/**
 * Processes customer UI requests for appliation.
 */
@RestController
@RequestMapping("/api")
public class ApplicationController {

    /** Check login status. */
    @GetMapping("/loginStatus")
    public boolean loginStatus() {
        return true;
    }

    /** Returns account login information. */
    @GetMapping("/loginAccount")
    public LoginAccount loadLoginAccount() {
        Actor actor = ActorSession.actor();
        return LoginAccount.builder()
                .id(actor.id())
                .name(actor.name())
                .roleType(actor.roleType())
                .authorityIds(actor.authorityIds())
                .build();
    }

    /** Parameters focused on client use */
    @Builder
    public static record LoginAccount(
            String id,
            String name,
            ActorRoleType roleType,
            Collection<String> authorityIds) implements Dto {
    }

}
