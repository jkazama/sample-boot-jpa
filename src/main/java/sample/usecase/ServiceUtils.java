package sample.usecase;

import sample.ValidationException;
import sample.ValidationException.ErrorKeys;
import sample.context.actor.Actor;

/**
 * Serviceで利用されるユーティリティ処理。
 */
public abstract class ServiceUtils {

    /** 匿名以外の利用者情報を返します。 */
    public static Actor actorUser(Actor actor) {
        if (actor.getRoleType().isAnonymous()) {
            throw new ValidationException(ErrorKeys.Authentication);
        }
        return actor;
    }

}
