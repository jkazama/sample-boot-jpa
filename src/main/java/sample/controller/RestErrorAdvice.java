package sample.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.HttpMediaTypeException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.client.HttpClientErrorException;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolationException;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import sample.context.ErrorKeys;
import sample.context.ValidationException;
import sample.context.ValidationException.Warn;
import sample.context.ValidationException.Warns;
import sample.context.actor.ActorSession;

/**
 * REST用の例外Map変換サポート。
 * <p>
 * AOPアドバイスで全てのRestControllerに対して例外処理を当て込みます。
 */
@ControllerAdvice(annotations = RestController.class)
@RequiredArgsConstructor(staticName = "of")
@Slf4j
public class RestErrorAdvice {

    private final MessageSource msg;

    /** Servlet例外 */
    @ExceptionHandler(ServletRequestBindingException.class)
    public ResponseEntity<Map<String, String[]>> handleServletRequestBinding(ServletRequestBindingException e) {
        log.warn(e.getMessage());
        return ErrorHolder.of(msg, locale(), ErrorKeys.ServletRequestBinding).result(HttpStatus.BAD_REQUEST);
    }

    private Locale locale() {
        return ActorSession.actor().locale();
    }

    /** メッセージ内容の読み込み失敗例外 */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, String[]>> handleHttpMessageNotReadable(HttpMessageNotReadableException e) {
        log.warn(e.getMessage());
        return ErrorHolder.of(msg, locale(), ErrorKeys.HttpMessageNotReadable).result(HttpStatus.BAD_REQUEST);
    }

    /** メディアタイプのミスマッチ例外 */
    @ExceptionHandler(HttpMediaTypeException.class)
    public ResponseEntity<Map<String, String[]>> handleHttpMediaTypeException(
            HttpMediaTypeException e) {
        log.warn(e.getMessage());
        return ErrorHolder.of(msg, locale(), ErrorKeys.HttpMediaTypeNotAcceptable).result(HttpStatus.BAD_REQUEST);
    }

    /** 楽観的排他(Hibernateのバージョンチェック)の例外 */
    @ExceptionHandler(OptimisticLockingFailureException.class)
    public ResponseEntity<Map<String, String[]>> handleOptimisticLockingFailureException(
            OptimisticLockingFailureException e) {
        log.warn(e.getMessage(), e);
        return ErrorHolder.of(msg, locale(), ErrorKeys.OptimisticLockingFailure).result(HttpStatus.BAD_REQUEST);
    }

    /** 権限例外 */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, String[]>> handleAccessDeniedException(AccessDeniedException e) {
        log.warn(e.getMessage());
        return ErrorHolder.of(msg, locale(), ErrorKeys.AccessDenied).result(HttpStatus.UNAUTHORIZED);
    }

    /** 指定した情報が存在しない例外 */
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<Map<String, String[]>> handleEntityNotFoundException(EntityNotFoundException e) {
        log.warn(e.getMessage(), e);
        return ErrorHolder.of(msg, locale(), ErrorKeys.EntityNotFound).result(HttpStatus.BAD_REQUEST);
    }

    /** BeanValidation(JSR303)の制約例外 */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Map<String, String[]>> handleConstraintViolation(ConstraintViolationException e) {
        log.warn(e.getMessage());
        Warns warns = Warns.init();
        e.getConstraintViolations().forEach((v) -> warns.add(v.getPropertyPath().toString(), v.getMessage()));
        return ErrorHolder.of(msg, locale(), warns.list()).result(HttpStatus.BAD_REQUEST);
    }

    /** Controllerへのリクエスト紐付け例外(for JSON) */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String[]>> handleMethodArgumentNotValid(MethodArgumentNotValidException e) {
        log.warn(e.getMessage());
        return ErrorHolder.of(msg, locale(), convert(e.getBindingResult()).list())
                .result(HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(WebExchangeBindException.class)
    public ResponseEntity<Map<String, String[]>> handleExchangeBind(WebExchangeBindException e) {
        log.warn(e.getMessage());
        return ErrorHolder.of(msg, locale(), convert(e).list()).result(HttpStatus.BAD_REQUEST);
    }

    /** Controllerへのリクエスト紐付け例外 */
    @ExceptionHandler(BindException.class)
    public ResponseEntity<Map<String, String[]>> handleBind(BindException e) {
        log.warn(e.getMessage());
        return ErrorHolder.of(msg, locale(), convert(e).list()).result(HttpStatus.BAD_REQUEST);
    }

    private Warns convert(BindingResult e) {
        Warns warns = Warns.init();
        e.getFieldErrors().forEach(err -> {
            String[] args = Arrays.stream(err.getArguments())
                    .filter((arg) -> !(arg instanceof MessageSourceResolvable))
                    .map(Object::toString)
                    .toArray(String[]::new);
            warns.add(err.getField(), err.getDefaultMessage(), args);
        });
        return warns;
    }

    public static Warns convert(List<ObjectError> errors) {
        Warns warns = Warns.init();
        errors.forEach((oe) -> {
            String field = "";
            if (1 == oe.getCodes().length) {
                field = bindField(oe.getCodes()[0]);
            } else if (1 < oe.getCodes().length) {
                // プリフィックスは冗長なので外す
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
        return warns;
    }

    public static String bindField(String field) {
        return Optional.ofNullable(field).map((v) -> v.substring(v.indexOf('.') + 1)).orElse("");
    }

    /** RestTemplate 例外時のブリッジサポート */
    @ExceptionHandler(HttpClientErrorException.class)
    public ResponseEntity<String> handleHttpClientError(HttpClientErrorException e) {
        HttpHeaders headers = new HttpHeaders();
        headers.put(HttpHeaders.CONTENT_TYPE, Arrays.asList(MediaType.APPLICATION_JSON_VALUE));
        return new ResponseEntity<>(e.getResponseBodyAsString(), headers, e.getStatusCode());
    }

    /** アプリケーション例外 */
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<Map<String, String[]>> handleValidation(ValidationException e) {
        ErrorHolder error = ErrorHolder.of(msg, locale(), e);
        log.warn(e.getMessage());
        return error.result(HttpStatus.BAD_REQUEST);
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
        return ErrorHolder.of(msg, locale(), ErrorKeys.Exception, "サーバー側で問題が発生した可能性があります。")
                .result(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * 例外情報のスタックを表現します。
     * <p>
     * スタックした例外情報は{@link #result(HttpStatus)}を呼び出す事でMapを持つResponseEntityへ変換可能です。
     * Mapのkeyはfiled指定値、valueはメッセージキーの変換値(messages-validation.properties)が入ります。
     * <p>
     * {@link #errorGlobal}で登録した場合のキーは空文字となります。
     * <p>
     * クライアント側は戻り値を [{"fieldA": "messageA"}, {"fieldB": "messageB"}]で受け取ります。
     */
    @Builder
    public static record ErrorHolder(
            MessageSource msg,
            Locale locale,
            Map<String, List<String>> errors) {

        /** グローバルな例外を返します。 */
        public String errorGlobal() {
            return Optional.ofNullable(errors.get("")).map(v -> v.get(0)).orElse(null);
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

        public static ErrorHolder of(
                MessageSource msg,
                Locale locale) {
            return ErrorHolder.builder()
                    .msg(msg)
                    .locale(locale)
                    .errors(new HashMap<>())
                    .build();
        }

        public static ErrorHolder of(final MessageSource msg, final Locale locale, final ValidationException e) {
            return of(msg, locale, e.list());
        }

        public static ErrorHolder of(final MessageSource msg, final Locale locale, final List<Warn> warns) {
            var error = of(msg, locale);
            warns.forEach((warn) -> {
                if (warn.global()) {
                    error.errorGlobal(warn.message(), warn.messageArgs());
                } else {
                    error.error(warn.field(), warn.message(), warn.messageArgs());
                }
            });
            return error;
        }

        public static ErrorHolder of(
                final MessageSource msg,
                final Locale locale,
                String globalMsgKey,
                String... msgArgs) {
            return of(msg, locale).errorGlobal(globalMsgKey, msgArgs);
        }
    }

}
