package sample.context.audit;

import java.util.Optional;
import java.util.function.Supplier;

import org.slf4j.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.*;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import sample.*;
import sample.context.actor.*;
import sample.context.audit.AuditActor.RegAuditActor;
import sample.context.audit.AuditEvent.RegAuditEvent;
import sample.context.orm.SystemRepository;

/**
 * It deals with user inspection or EDP audit (an appointed hour batch or kind of day batch).
 * <p>When you expect an implicit application, please examine the cooperation with AOP. 
 * <p>The target log is begun to write as well as Logger to the inspection table of the system schema.
 * (You can detect a replyless state by making the other transaction at a start and completion.)
 */
@Slf4j
@Setter
public class AuditHandler {
    public static final Logger LoggerActor = LoggerFactory.getLogger("Audit.Actor");
    public static final Logger LoggerEvent = LoggerFactory.getLogger("Audit.Event");

    @Autowired
    private ActorSession session;
    @Autowired
    private AuditPersister persister;

    public <T> T audit(String message, final Supplier<T> callable) {
        return audit("default", message, callable);
    }

    public void audit(String message, final Runnable command) {
        audit(message, () -> {
            command.run();
            return true;
        });
    }

    public <T> T audit(String category, String message, final Supplier<T> callable) {
        logger().trace(message(message, "[Start]", null));
        long start = System.currentTimeMillis();
        try {
            T v = session.actor().getRoleType().isSystem() ? callEvent(category, message, callable)
                    : callAudit(category, message, callable);
            logger().info(message(message, "[ End ]", start));
            return v;
        } catch (ValidationException e) {
            logger().warn(message(message, "[Warning]", start));
            throw e;
        } catch (RuntimeException e) {
            logger().error(message(message, "[Exception]", start));
            throw (RuntimeException) e;
        } catch (Exception e) {
            logger().error(message(message, "[Exception]", start));
            throw new InvocationException("error.Exception", e);
        }
    }

    public void audit(String category, String message, final Runnable command) {
        audit(category, message, () -> {
            command.run();
            return true;
        });
    }

    private Logger logger() {
        return session.actor().getRoleType().isSystem() ? LoggerEvent : LoggerActor;
    }

    private String message(String message, String prefix, Long startMillis) {
        Actor actor = session.actor();
        StringBuilder sb = new StringBuilder(prefix + " ");
        if (actor.getRoleType().notSystem()) {
            sb.append("[" + actor.getId() + "] ");
        }
        sb.append(message);
        if (startMillis != null) {
            sb.append(" [" + (System.currentTimeMillis() - startMillis) + "ms]");
        }
        return sb.toString();
    }

    public <T> T callAudit(String category, String message, final Supplier<T> callable) {
        Optional<AuditActor> audit = Optional.empty();
        try {
            try { // So that the obstacle of the system schema does not affect the essential error
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

    public <T> T callEvent(String category, String message, final Supplier<T> callable) {
        Optional<AuditEvent> audit = Optional.empty();
        try {
            try { // So that the obstacle of the system schema does not affect the essential error
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

    @Setter
    public static class AuditPersister {
        @Autowired
        private SystemRepository rep;

        @Transactional(value = SystemRepository.BeanNameTx, propagation = Propagation.REQUIRES_NEW)
        public AuditActor start(RegAuditActor p) {
            return AuditActor.register(rep, p);
        }

        @Transactional(value = SystemRepository.BeanNameTx, propagation = Propagation.REQUIRES_NEW)
        public AuditActor finish(AuditActor audit) {
            return audit.finish(rep);
        }

        @Transactional(value = SystemRepository.BeanNameTx, propagation = Propagation.REQUIRES_NEW)
        public AuditActor cancel(AuditActor audit, String errorReason) {
            return audit.cancel(rep, errorReason);
        }

        @Transactional(value = SystemRepository.BeanNameTx, propagation = Propagation.REQUIRES_NEW)
        public AuditActor error(AuditActor audit, String errorReason) {
            return audit.error(rep, errorReason);
        }

        @Transactional(value = SystemRepository.BeanNameTx, propagation = Propagation.REQUIRES_NEW)
        public AuditEvent start(RegAuditEvent p) {
            return AuditEvent.register(rep, p);
        }

        @Transactional(value = SystemRepository.BeanNameTx, propagation = Propagation.REQUIRES_NEW)
        public AuditEvent finish(AuditEvent event) {
            return event.finish(rep);
        }

        @Transactional(value = SystemRepository.BeanNameTx, propagation = Propagation.REQUIRES_NEW)
        public AuditEvent cancel(AuditEvent event, String errorReason) {
            return event.cancel(rep, errorReason);
        }

        @Transactional(value = SystemRepository.BeanNameTx, propagation = Propagation.REQUIRES_NEW)
        public AuditEvent error(AuditEvent event, String errorReason) {
            return event.error(rep, errorReason);
        }
    }

}
