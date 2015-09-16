package sample.controller;

import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import lombok.*;
import sample.ValidationException;
import sample.ValidationException.ErrorKeys;
import sample.context.actor.Actor;
import sample.context.security.SecurityActorFinder;
import sample.context.security.SecurityActorFinder.ActorDetails;
import sample.usecase.AccountService;

/**
 * 口座に関わる顧客のUI要求を処理します。
 */
@RestController
@RequestMapping("/api/asset")
@Setter
public class AccountController extends ControllerSupport {

	@Autowired
	private AccountService service;
	
	/** 口座情報を取得します。 */
	@RequestMapping(value = "/loginStatus/")
	public static LoginAccount loadLoginAccount() {
		ActorDetails actorDetails = SecurityActorFinder.actorDetails().orElseThrow(() -> new ValidationException(ErrorKeys.Authentication));
		Actor actor = actorDetails.actor();
		return new LoginAccount(actor.getId(), actor.getName(), actorDetails.getAuthorityIds());
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
		return resultEmpty(() -> service.hashCode());
	}
	
}
