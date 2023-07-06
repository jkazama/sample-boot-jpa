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

import jakarta.validation.ConstraintViolationException;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import sample.context.ErrorKeys;
import sample.context.ValidationException;
import sample.context.actor.ActorSession;
import sample.util.Warn;
import sample.util.Warns;

/**
 * Exception Map conversion support for REST.
 * <p>
 * Apply exception handling to all RestControllers with AOP advice.
 */
@ControllerAdvice(annotations = RestController.class)
@RequiredArgsConstructor(staticName = "of")
@Slf4j
public class RestErrorAdvice {

    private final MessageSource msg;

    @ExceptionHandler(ServletRequestBindingException.class)
    public ResponseEntity<Map<String, String[]>> handleServletRequestBinding(ServletRequestBindingException e) {
        log.warn(e.getMessage());
        return ErrorHolder.of(msg, locale(), ErrorKeys.ServletRequestBinding).result(HttpStatus.BAD_REQUEST);
    }

    private Locale locale() {
        return ActorSession.actor().locale();
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, String[]>> handleHttpMessageNotReadable(HttpMessageNotReadableException e) {
        log.warn(e.getMessage());
        return ErrorHolder.of(msg, locale(), ErrorKeys.HttpMessageNotReadable).result(HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(HttpMediaTypeException.class)
    public ResponseEntity<Map<String, String[]>> handleHttpMediaTypeException(
            HttpMediaTypeException e) {
        log.warn(e.getMessage());
        return ErrorHolder.of(msg, locale(), ErrorKeys.HttpMediaTypeNotAcceptable).result(HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(OptimisticLockingFailureException.class)
    public ResponseEntity<Map<String, String[]>> handleOptimisticLockingFailureException(
            OptimisticLockingFailureException e) {
        log.warn(e.getMessage(), e);
        return ErrorHolder.of(msg, locale(), ErrorKeys.OptimisticLockingFailure).result(HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, String[]>> handleAccessDeniedException(AccessDeniedException e) {
        log.warn(e.getMessage());
        return ErrorHolder.of(msg, locale(), ErrorKeys.AccessDenied).result(HttpStatus.UNAUTHORIZED);
    }

    /** BeanValidation(JSR303) Constraint Exception */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Map<String, String[]>> handleConstraintViolation(ConstraintViolationException e) {
        log.warn(e.getMessage());
        var warns = Warns.of();
        e.getConstraintViolations().forEach((v) -> warns.addField(v.getPropertyPath().toString(), v.getMessage()));
        return ErrorHolder.of(msg, locale(), warns.list()).result(HttpStatus.BAD_REQUEST);
    }

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

    @ExceptionHandler(BindException.class)
    public ResponseEntity<Map<String, String[]>> handleBind(BindException e) {
        log.warn(e.getMessage());
        return ErrorHolder.of(msg, locale(), convert(e).list()).result(HttpStatus.BAD_REQUEST);
    }

    private Warns convert(BindingResult e) {
        var warns = Warns.of();
        e.getFieldErrors().forEach(err -> {
            String[] args = Arrays.stream(err.getArguments())
                    .filter((arg) -> !(arg instanceof MessageSourceResolvable))
                    .map(Object::toString)
                    .toArray(String[]::new);
            warns.addField(err.getField(), err.getDefaultMessage(), args);
        });
        return warns;
    }

    public static sample.util.Warns convert(List<ObjectError> errors) {
        var warns = Warns.of();
        errors.forEach((oe) -> {
            String field = "";
            if (1 == oe.getCodes().length) {
                field = bindField(oe.getCodes()[0]);
            } else if (1 < oe.getCodes().length) {
                // Remove prefixes because they are redundant.
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
            warns.addField(field, message, args);
        });
        return warns;
    }

    public static String bindField(String field) {
        return Optional.ofNullable(field).map((v) -> v.substring(v.indexOf('.') + 1)).orElse("");
    }

    @ExceptionHandler(HttpClientErrorException.class)
    public ResponseEntity<String> handleHttpClientError(HttpClientErrorException e) {
        HttpHeaders headers = new HttpHeaders();
        headers.put(HttpHeaders.CONTENT_TYPE, Arrays.asList(MediaType.APPLICATION_JSON_VALUE));
        return new ResponseEntity<>(e.getResponseBodyAsString(), headers, e.getStatusCode());
    }

    /** business exception */
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<Map<String, String[]>> handleValidation(ValidationException e) {
        ErrorHolder error = ErrorHolder.of(msg, locale(), e);
        log.warn(e.getMessage());
        return error.result(HttpStatus.BAD_REQUEST);
    }

    /**
     * IO exception (Broken pipe in Tomcat is excluded because it is not a
     * server-side responsibility)
     */
    @ExceptionHandler(IOException.class)
    public ResponseEntity<Map<String, String[]>> handleIOException(IOException e) {
        if (e.getMessage() != null && e.getMessage().contains("Broken pipe")) {
            log.info("The process was terminated for client reasons.");
            return ResponseEntity.ok().build();
        } else {
            return handleException(e);
        }
    }

    /** general purpose exception */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String[]>> handleException(Exception e) {
        log.error("An unexpected exception occurred.", e);
        return ErrorHolder.of(msg, locale(), ErrorKeys.Exception)
                .result(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Represents a stack of exception information.
     * <p>
     * Stacked exception information can be converted to ResponseEntity with Map by
     * calling {@link #result(HttpStatus)}. The key of the map is the value
     * specified as filed, and the value is the converted value of the message key
     * (messages-validation.properties).
     * <p>
     * The key will be an empty string when registered with {@link #errorGlobal}.
     * <p>
     * The client side receives the return value as [{"fieldA": "messageA"},
     * {"fieldB": "messageB"}].
     */
    @Builder
    public static record ErrorHolder(
            MessageSource msg,
            Locale locale,
            Map<String, List<String>> errors) {

        /** Returns a global exception. */
        public String errorGlobal() {
            return Optional.ofNullable(errors.get("")).map(v -> v.get(0)).orElse(null);
        }

        /** Add a global exception (field key is empty). */
        public ErrorHolder errorGlobal(String msgKey, String defaultMsg, String... msgArgs) {
            if (!errors.containsKey(""))
                errors.put("", new ArrayList<>());
            errors.get("").add(msg.getMessage(msgKey, msgArgs, defaultMsg, locale));
            return this;
        }

        /** Add a global exception (field key is empty). */
        public ErrorHolder errorGlobal(String msgKey, String... msgArgs) {
            return errorGlobal(msgKey, msgKey, msgArgs);
        }

        /** Add per-field exceptions. */
        public ErrorHolder error(String field, String msgKey, String... msgArgs) {
            if (!errors.containsKey(field))
                errors.put(field, new ArrayList<>());
            errors.get(field).add(msg.getMessage(msgKey, msgArgs, msgKey, locale));
            return this;
        }

        /**
         * Converts the exception information held by the client into a ResponseEntity.
         */
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
