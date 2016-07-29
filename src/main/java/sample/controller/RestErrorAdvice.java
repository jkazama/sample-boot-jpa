package sample.controller;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import javax.persistence.EntityNotFoundException;
import javax.validation.ConstraintViolationException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindException;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.*;

import sample.ValidationException;
import sample.ValidationException.*;
import sample.context.actor.ActorSession;

/**
 * REST用の例外Map変換サポート。
 * <p>AOPアドバイスで全てのRestControllerに対して例外処理を当て込みます。
 */
@ControllerAdvice(annotations = RestController.class)
public class RestErrorAdvice {

    protected Log log = LogFactory.getLog(getClass());

    @Autowired
    private MessageSource msg;
    @Autowired
    private ActorSession session;

    /** Servlet例外 */
    @ExceptionHandler(ServletRequestBindingException.class)
    public ResponseEntity<Map<String, String[]>> handleServletRequestBinding(ServletRequestBindingException e) {
        log.warn(e.getMessage());
        return new ErrorHolder(msg, locale(), "error.ServletRequestBinding").result(HttpStatus.BAD_REQUEST);
    }

    private Locale locale() {
        return session.actor().getLocale();
    }

    /** メディアタイプのミスマッチ例外 */
    @ExceptionHandler(HttpMediaTypeNotAcceptableException.class)
    public ResponseEntity<Map<String, String[]>> handleHttpMediaTypeNotAcceptable(
            HttpMediaTypeNotAcceptableException e) {
        log.warn(e.getMessage());
        return new ErrorHolder(msg, locale(), "error.HttpMediaTypeNotAcceptable").result(HttpStatus.BAD_REQUEST);
    }

    /** 楽観的排他(Hibernateのバージョンチェック)の例外 */
    @ExceptionHandler(OptimisticLockingFailureException.class)
    public ResponseEntity<Map<String, String[]>> handleOptimisticLockingFailureException(
            OptimisticLockingFailureException e) {
        log.warn(e.getMessage(), e);
        return new ErrorHolder(msg, locale(), "error.OptimisticLockingFailure").result(HttpStatus.BAD_REQUEST);
    }

    /** 権限例外 */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, String[]>> handleAccessDeniedException(AccessDeniedException e) {
        log.warn(e.getMessage());
        return new ErrorHolder(msg, locale(), ErrorKeys.AccessDenied).result(HttpStatus.UNAUTHORIZED);
    }

    /** 指定した情報が存在しない例外 */
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<Map<String, String[]>> handleEntityNotFoundException(EntityNotFoundException e) {
        log.warn(e.getMessage(), e);
        return new ErrorHolder(msg, locale(), ErrorKeys.EntityNotFound).result(HttpStatus.BAD_REQUEST);
    }

    /** BeanValidation(JSR303)の制約例外 */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Map<String, String[]>> handleConstraintViolation(ConstraintViolationException e) {
        log.warn(e.getMessage());
        Warns warns = Warns.init();
        e.getConstraintViolations().forEach((v) -> warns.add(v.getPropertyPath().toString(), v.getMessage()));
        return new ErrorHolder(msg, locale(), warns.list()).result(HttpStatus.BAD_REQUEST);
    }

    /** Controllerへのリクエスト紐付け例外 */
    @ExceptionHandler(BindException.class)
    public ResponseEntity<Map<String, String[]>> handleBind(BindException e) {
        log.warn(e.getMessage());
        Warns warns = Warns.init();
        e.getAllErrors().forEach((oe) -> {
            String field = "";
            if (1 == oe.getCodes().length) {
                field = bindField(oe.getCodes()[0]);
            } else if (1 < oe.getCodes().length) {
                // low: プリフィックスは冗長なので外してます
                field = bindField(oe.getCodes()[1]);
            }
            List<String> args = Arrays.stream(oe.getArguments())
                    .filter((arg) -> !(arg instanceof MessageSourceResolvable))
                    .map(Object::toString)
                    .collect(Collectors.toList());
            String message = oe.getDefaultMessage();
            if (0 <= oe.getCodes()[0].indexOf("typeMismatch")) {
                message = oe.getCodes()[2];
            }
            warns.add(field, message, args.toArray(new String[0]));
        });
        return new ErrorHolder(msg, locale(), warns.list()).result(HttpStatus.BAD_REQUEST);
    }

    protected String bindField(String field) {
        return Optional.ofNullable(field).map((v) -> v.substring(v.indexOf('.') + 1)).orElse("");
    }

    /** アプリケーション例外 */
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<Map<String, String[]>> handleValidation(ValidationException e) {
        log.warn(e.getMessage());
        return new ErrorHolder(msg, locale(), e).result(HttpStatus.BAD_REQUEST);
    }

    /** IO例外（Tomcatの Broken pipe はサーバー側の責務ではないので除外しています) */
    @ExceptionHandler(IOException.class)
    public ResponseEntity<Map<String, String[]>> handleIOException(IOException e) {
        if (e.getMessage() != null && e.getMessage().contains("Broken pipe")) {
            log.info("クライアント事由で処理が打ち切られました。");
            return new ResponseEntity<>(HttpStatus.OK);
        } else {
            return handleException(e);
        }
    }

    /** 汎用例外 */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String[]>> handleException(Exception e) {
        log.error("予期せぬ例外が発生しました。", e);
        return new ErrorHolder(msg, locale(), ErrorKeys.Exception, "サーバー側で問題が発生した可能性があります。")
                .result(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * 例外情報のスタックを表現します。
     * <p>スタックした例外情報は{@link #result(HttpStatus)}を呼び出す事でMapを持つResponseEntityへ変換可能です。
     * Mapのkeyはfiled指定値、valueはメッセージキーの変換値(messages-validation.properties)が入ります。
     * <p>{@link #errorGlobal}で登録した場合のキーは空文字となります。
     * <p>クライアント側は戻り値を [{"fieldA": "messageA"}, {"fieldB": "messageB"}]で受け取ります。
     */
    public static class ErrorHolder {
        private Map<String, List<String>> errors = new HashMap<>();
        private MessageSource msg;
        private Locale locale;

        public ErrorHolder(final MessageSource msg, final Locale locale) {
            this.msg = msg;
            this.locale = locale;
        }

        public ErrorHolder(final MessageSource msg, final Locale locale, final ValidationException e) {
            this(msg, locale, e.list());
        }

        public ErrorHolder(final MessageSource msg, final Locale locale, final List<Warn> warns) {
            this.msg = msg;
            this.locale = locale;
            warns.forEach((warn) -> {
                if (warn.global())
                    errorGlobal(warn.getMessage());
                else
                    error(warn.getField(), warn.getMessage());
            });
        }

        public ErrorHolder(final MessageSource msg, final Locale locale, String globalMsgKey, String... msgArgs) {
            this.msg = msg;
            this.locale = locale;
            errorGlobal(globalMsgKey, msgArgs);
        }

        /** グローバルな例外(フィールドキーが空)を追加します。 */
        public ErrorHolder errorGlobal(String msgKey, String defaultMsg, String... msgArgs) {
            if (!errors.containsKey(""))
                errors.put("", new ArrayList<>());
            errors.get("").add(msg.getMessage(msgKey, msgArgs, defaultMsg, locale));
            return this;
        }

        /** グローバルな例外(フィールドキーが空)を追加します。 */
        public ErrorHolder errorGlobal(String msgKey, String... msgArgs) {
            return errorGlobal(msgKey, msgKey, msgArgs);
        }

        /** フィールド単位の例外を追加します。 */
        public ErrorHolder error(String field, String msgKey, String... msgArgs) {
            if (!errors.containsKey(field))
                errors.put(field, new ArrayList<>());
            errors.get(field).add(msg.getMessage(msgKey, msgArgs, msgKey, locale));
            return this;
        }

        /** 保有する例外情報をResponseEntityへ変換します。 */
        public ResponseEntity<Map<String, String[]>> result(HttpStatus status) {
            return new ResponseEntity<Map<String, String[]>>(
                    errors.entrySet().stream().collect(Collectors.toMap(
                            Map.Entry::getKey, (entry) -> entry.getValue().toArray(new String[0]))),
                    status);
        }
    }

}
