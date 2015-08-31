package sample.controller.admin;

import java.util.Collection;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import lombok.*;
import sample.ValidationException;
import sample.ValidationException.ErrorKeys;
import sample.context.actor.Actor;
import sample.context.security.SecurityActorFinder;
import sample.context.security.SecurityActorFinder.ActorDetails;
import sample.controller.ControllerSupport;
import sample.model.master.Holiday.RegisterHoliday;
import sample.usecase.MasterAdminService;

/**
 * マスタに関わる社内のUI要求を処理します。
 */
@RestController
@RequestMapping("/api/admin/master")
@Setter
public class MasterAdminController extends ControllerSupport {

	@Autowired
	private MasterAdminService service;
	
	/** ログイン情報を取得します。 */
	@RequestMapping(value = "/loginStaff/")
	public static LoginStaff loadLoginStaff() {
		ActorDetails actorDetails = SecurityActorFinder.actorDetails().orElseThrow(() -> new ValidationException(ErrorKeys.Authentication));
		Actor actor = actorDetails.actor();
		return new LoginStaff(actor.getId(), actor.getName(), actorDetails.getAuthorityIds());
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
	@RequestMapping(value = "/holiday/", method = RequestMethod.POST)
	public ResponseEntity<Void> registerHoliday(@Valid RegisterHoliday p) {
		return resultEmpty(() -> service.registerHoliday(p)); 
	}
	
}
