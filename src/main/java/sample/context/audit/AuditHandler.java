package sample.context.audit;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Propagation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import sample.context.ErrorKeys;
import sample.context.InvocationException;
import sample.context.ValidationException;
import sample.context.actor.Actor;
import sample.context.actor.ActorSession;
import sample.context.audit.AuditActor.RegAuditActor;
import sample.context.audit.AuditEvent.RegAuditEvent;
import sample.context.orm.TxTemplate;
import sample.context.orm.repository.SystemRepository;

/**
 * this component handle user audits and system audits (regular jobs, daily
 * jobs, etc.).
 * <p>
 * If implicit application is desired, consider working with AOP.
 * <p>
 * Target logs are written not only to the Logger but also to the audit table in
 * the system schema.
 * (It is possible to detect no-response conditions by setting separate TXs for
 * start and completion times.)
 */
@Component
@RequiredArgsConstructor(staticName = "of")
@Slf4j
public class AuditHandler {
    public static final Logger LoggerActor = LoggerFactory.getLogger("Audit.Actor");
    public static final Logger LoggerEvent = LoggerFactory.getLogger("Audit.Event");
    public static final String AUDIT_PREFIX = "audit";

    private final MessageSource msg;
    private final AuditPersister persister;

    /** Audit logs are logged for the given process. */
    public <T> T audit(String message, Supplier<T> callable) {
        return audit(message, List.of(), callable);
    }

    /** Audit logs are logged for the given process. */
    public <T> T audit(String message, Collection<Object> messageArgs, Supplier<T> callable) {
        return audit("default", message, messageArgs, callable);
    }

    /** Audit logs are recorded for the given process. */
    public void audit(String message, Runnable command) {
        audit(message, List.of(), command);
    }

    /** Audit logs are logged for the given process. */
    public void audit(String message, Collection<Object> messageArgs, Runnable command) {
        audit(message, messageArgs, () -> {
            command.run();
            return true;
        });
    }

    /** Audit logs are recorded for the given process. */
    public <T> T audit(String category, String message, Supplier<T> callable) {
        return audit(category, message, List.of(), callable);
    }

    /** Audit logs are recorded for the given process. */
    public <T> T audit(String category, String message, Collection<Object> messageArgs, Supplier<T> callable) {
        Locale locale = ActorSession.actor().locale();
        String keyMessage = AUDIT_PREFIX + "." + category + "." + message;
        String mergeMessage;
        if (messageArgs == null || messageArgs.isEmpty()) {
            mergeMessage = msg.getMessage(keyMessage, new Object[0], message, locale);
        } else {
            mergeMessage = msg.getMessage(keyMessage, messageArgs.toArray(), message, locale);
        }
        logger().trace(message(mergeMessage, "[Start]", null));
        long start = System.currentTimeMillis();
        try {
            T v = ActorSession.actor().roleType().isSystem()
                    ? callEvent(category, mergeMessage, callable)
                    : callAudit(category, mergeMessage, callable);
            logger().info(message(mergeMessage, "[Finish]", start));
            return v;
        } catch (ValidationException e) {
            logger().warn(message(mergeMessage, "[Warn]", start));
            throw e;
        } catch (RuntimeException e) {
            logger().warn(message(mergeMessage, "[Error]", start));
            throw (RuntimeException) e;
        } catch (Exception e) {
            logger().warn(message(mergeMessage, "[Fatal]", start));
            throw InvocationException.of(ErrorKeys.Exception, e);
        }
    }

    /** Audit logs are recorded for the given process. */
    public void audit(String category, String message, Runnable command) {
        audit(category, message, List.of(), command);
    }

    /** Audit logs are recorded for the given process. */
    public void audit(String category, String message, Collection<Object> messageArgs, Runnable command) {
        audit(category, message, messageArgs, () -> {
            command.run();
            return true;
        });
    }

    private Logger logger() {
        return ActorSession.actor().roleType().isSystem() ? LoggerEvent : LoggerActor;
    }

    private String message(String message, String prefix, Long startMillis) {
        Actor actor = ActorSession.actor();
        StringBuilder sb = new StringBuilder(prefix + " ");
        if (actor.roleType().notSystem()) {
            sb.append("[" + actor.id() + "] ");
        }
        sb.append(message);
        if (startMillis != null) {
            sb.append(" [" + (System.currentTimeMillis() - startMillis) + "ms]");
        }
        return sb.toString();
    }

    public <T> T callAudit(String category, String message, Supplier<T> callable) {
        Optional<AuditActor> audit = Optional.empty();
        try {
            try { // Failure of the system schema should not affect the intrinsic errorに
                audit = Optional.of(persister.start(RegAuditActor.of(category, message)));
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
            T v = callable.get();
            try {
                audit.ifPresent(persister::finish);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
            return v;
        } catch (ValidationException e) {
            try {
                audit.ifPresent((v) -> persister.cancel(v, e.getMessage()));
            } catch (Exception ex) {
                log.error(ex.getMessage(), ex);
            }
            throw e;
        } catch (RuntimeException e) {
            try {
                audit.ifPresent((v) -> persister.error(v, e.getMessage()));
            } catch (Exception ex) {
                log.error(ex.getMessage(), ex);
            }
            throw e;
        } catch (Exception e) {
            try {
                audit.ifPresent((v) -> persister.error(v, e.getMessage()));
            } catch (Exception ex) {
                log.error(ex.getMessage(), ex);
            }
            throw new InvocationException(e);
        }
    }

    public <T> T callEvent(String category, String message, Supplier<T> callable) {
        Optional<AuditEvent> audit = Optional.empty();
        try {
            try { // Failure of the system schema should not affect the intrinsic error
                audit = Optional.of(persister.start(RegAuditEvent.of(category, message)));
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
            T v = callable.get();
            try {
                audit.ifPresent(persister::finish);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
            return v;
        } catch (ValidationException e) {
            try {
                audit.ifPresent((v) -> persister.cancel(v, e.getMessage()));
            } catch (Exception ex) {
                log.error(ex.getMessage(), ex);
            }
            throw e;
        } catch (RuntimeException e) {
            try {
                audit.ifPresent((v) -> persister.error(v, e.getMessage()));
            } catch (Exception ex) {
                log.error(ex.getMessage(), ex);
            }
            throw (RuntimeException) e;
        } catch (Exception e) {
            try {
                audit.ifPresent((v) -> persister.error(v, e.getMessage()));
            } catch (Exception ex) {
                log.error(ex.getMessage(), ex);
            }
            throw new InvocationException(e);
        }
    }

    /**
     * Persist audit logs to the system schema.
     */
    @Component
    @RequiredArgsConstructor(staticName = "of")
    public static class AuditPersister {
        private final SystemRepository rep;
        @Qualifier(SystemRepository.BeanNameTx)
        private final PlatformTransactionManager txm;

        public AuditActor start(final RegAuditActor p) {
            return TxTemplate.of(txm).propagation(Propagation.REQUIRES_NEW).tx(() -> {
                return AuditActor.register(rep, p);
            });
        }

        public AuditActor finish(final AuditActor audit) {
            return TxTemplate.of(txm).propagation(Propagation.REQUIRES_NEW).tx(() -> {
                return audit.finish(rep);
            });
        }

        public AuditActor cancel(final AuditActor audit, String errorReason) {
            return TxTemplate.of(txm).propagation(Propagation.REQUIRES_NEW).tx(() -> {
                return audit.cancel(rep, errorReason);
            });
        }

        public AuditActor error(final AuditActor audit, String errorReason) {
            return TxTemplate.of(txm).propagation(Propagation.REQUIRES_NEW).tx(() -> {
                return audit.error(rep, errorReason);
            });
        }

        public AuditEvent start(final RegAuditEvent p) {
            return TxTemplate.of(txm).propagation(Propagation.REQUIRES_NEW).tx(() -> {
                return AuditEvent.register(rep, p);
            });
        }

        public AuditEvent finish(final AuditEvent event) {
            return TxTemplate.of(txm).propagation(Propagation.REQUIRES_NEW).tx(() -> {
                return event.finish(rep);
            });
        }

        public AuditEvent cancel(final AuditEvent event, String errorReason) {
            return TxTemplate.of(txm).propagation(Propagation.REQUIRES_NEW).tx(() -> {
                return event.cancel(rep, errorReason);
            });
        }

        public AuditEvent error(final AuditEvent event, String errorReason) {
            return TxTemplate.of(txm).propagation(Propagation.REQUIRES_NEW).tx(() -> {
                return event.error(rep, errorReason);
            });
        }
    }

}
