package sample.context.orm;

import java.time.LocalDateTime;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import sample.context.DomainMetaEntity;
import sample.context.Timestamper;
import sample.context.actor.Actor;
import sample.context.actor.ActorSession;

/**
 * Interceptor that plugs in the AOP process at the Entity's persistence timing.
 */
@Component
@RequiredArgsConstructor(staticName = "of")
public class OrmInterceptor {
    private final Timestamper time;

    /** Pre-insertion process at registration. */
    public void touchForCreate(Object entity) {
        if (entity instanceof DomainMetaEntity metaEntity) {
            Actor staff = ActorSession.actor();
            LocalDateTime now = time.date();
            metaEntity.setCreateId(staff.id());
            metaEntity.setCreateDate(now);
            metaEntity.setUpdateId(staff.id());
            metaEntity.setUpdateDate(now);
        }
    }

    /** Pre-insertion process for changes. */
    public void touchForUpdate(final Object entity) {
        if (entity instanceof DomainMetaEntity metaEntity) {
            Actor staff = ActorSession.actor();
            LocalDateTime now = time.date();
            if (metaEntity.getCreateDate() == null) {
                metaEntity.setCreateId(staff.id());
                metaEntity.setCreateDate(now);
            }
            metaEntity.setUpdateId(staff.id());
            metaEntity.setUpdateDate(now);
        }
    }

}
