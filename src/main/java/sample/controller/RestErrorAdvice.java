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
 * Exception Map conversion support for RestController.
 * <p>Insert an exception handling by AOP advice.
 */
@ControllerAdvice(annotations = RestController.class)
public class RestErrorAdvice {

    protected Log log = LogFactory.getLog(getClass());

    @Autowired
    private MessageSource msg;
    @Autowired
    private ActorSession session;

    @ExceptionHandler(ServletRequestBindingException.class)
    public ResponseEntity<Map<String, String[]>> handleServletRequestBinding(ServletRequestBindingException e) {
        log.warn(e.getMessage());
        return new ErrorHolder(msg, locale(), "error.ServletRequestBinding").result(HttpStatus.BAD_REQUEST);
    }

    private Locale locale() {
        return session.actor().getLocale();
    }

    @ExceptionHandler(HttpMediaTypeNotAcceptableException.class)
    public ResponseEntity<Map<String, String[]>> handleHttpMediaTypeNotAcceptable(
            HttpMediaTypeNotAcceptableException e) {
        log.warn(e.getMessage());
        return new ErrorHolder(msg, locale(), "error.HttpMediaTypeNotAcceptable").result(HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(OptimisticLockingFailureException.class)
    public ResponseEntity<Map<String, String[]>> handleOptimisticLockingFailureException(
            OptimisticLockingFailureException e) {
        log.warn(e.getMessage(), e);
        return new ErrorHolder(msg, locale(), "error.OptimisticLockingFailure").result(HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, String[]>> handleAccessDeniedException(AccessDeniedException e) {
        log.warn(e.getMessage());
        return new ErrorHolder(msg, locale(), ErrorKeys.AccessDenied).result(HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<Map<String, String[]>> handleEntityNotFoundException(EntityNotFoundException e) {
        log.warn(e.getMessage(), e);
        return new ErrorHolder(msg, locale(), ErrorKeys.EntityNotFound).result(HttpStatus.BAD_REQUEST);
    }

    /** Bean Validation (JSR303) */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Map<String, String[]>> handleConstraintViolation(ConstraintViolationException e) {
        log.warn(e.getMessage());
        Warns warns = Warns.init();
        e.getConstraintViolations().forEach((v) -> warns.add(v.getPropertyPath().toString(), v.getMessage()));
        return new ErrorHolder(msg, locale(), warns.list()).result(HttpStatus.BAD_REQUEST);
    }

    /** Spring MVC */
    @ExceptionHandler(BindException.class)
    public ResponseEntity<Map<String, String[]>> handleBind(BindException e) {
        log.warn(e.getMessage());
        Warns warns = Warns.init();
        e.getAllErrors().forEach((oe) -> {
            String field = "";
            if (1 == oe.getCodes().length) {
                field = bindField(oe.getCodes()[0]);
            } else if (1 < oe.getCodes().length) {
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

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<Map<String, String[]>> handleValidation(ValidationException e) {
        log.warn(e.getMessage());
        return new ErrorHolder(msg, locale(), e).result(HttpStatus.BAD_REQUEST);
    }

    /** Broken pipe of Tomcat is not a duty of the server sides, exclude it. */
    @ExceptionHandler(IOException.class)
    public ResponseEntity<Map<String, String[]>> handleIOException(IOException e) {
        if (e.getMessage() != null && e.getMessage().contains("Broken pipe")) {
            log.info("Processing was canceled in a client reason.");
            return new ResponseEntity<>(HttpStatus.OK);
        } else {
            return handleException(e);
        }
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String[]>> handleException(Exception e) {
        log.error("An unexpected exception occurred.", e);
        return new ErrorHolder(msg, locale(), ErrorKeys.Exception, "A problem might occur in a server side.")
                .result(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * The stack of the exception information.
     * <p> can convert the exception information that I stacked into ResponseEntity having Map by calling {@link #result(HttpStatus)}.
     * <p>The key when You registered in {@link #errorGlobal} becomes the null.
     * <p>The client-side receives a return value in [{"fieldA": "messageA"}, {"fieldB": "messageB"}].
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

        public ErrorHolder errorGlobal(String msgKey, String defaultMsg, String... msgArgs) {
            if (!errors.containsKey(""))
                errors.put("", new ArrayList<>());
            errors.get("").add(msg.getMessage(msgKey, msgArgs, defaultMsg, locale));
            return this;
        }

        public ErrorHolder errorGlobal(String msgKey, String... msgArgs) {
            return errorGlobal(msgKey, msgKey, msgArgs);
        }

        public ErrorHolder error(String field, String msgKey, String... msgArgs) {
            if (!errors.containsKey(field))
                errors.put(field, new ArrayList<>());
            errors.get(field).add(msg.getMessage(msgKey, msgArgs, msgKey, locale));
            return this;
        }

        /** Convert exception information to hold it into ResponseEntity. */
        public ResponseEntity<Map<String, String[]>> result(HttpStatus status) {
            return new ResponseEntity<Map<String, String[]>>(
                    errors.entrySet().stream().collect(Collectors.toMap(
                            Map.Entry::getKey, (entry) -> entry.getValue().toArray(new String[0]))),
                    status);
        }
    }

}
