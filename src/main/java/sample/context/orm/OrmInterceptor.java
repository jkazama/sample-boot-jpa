package sample.context.orm;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;

import lombok.*;
import sample.context.Timestamper;
import sample.context.actor.*;

/**
 * Interceptor to insert AOP processing in a permanency timing of Entity.
 */
@Getter
@Setter
public class OrmInterceptor {

    @Autowired
    private ActorSession session;
    @Autowired
    private Timestamper time;

    public void touchForCreate(Object entity) {
        if (entity instanceof OrmActiveMetaRecord) {
            Actor staff = session.actor();
            LocalDateTime now = time.date();
            OrmActiveMetaRecord<?> metaEntity = (OrmActiveMetaRecord<?>) entity;
            metaEntity.setCreateId(staff.getId());
            metaEntity.setCreateDate(now);
            metaEntity.setUpdateId(staff.getId());
            metaEntity.setUpdateDate(now);
        }
    }

    public boolean touchForUpdate(final Object entity) {
        if (entity instanceof OrmActiveMetaRecord) {
            Actor staff = session.actor();
            LocalDateTime now = time.date();
            OrmActiveMetaRecord<?> metaEntity = (OrmActiveMetaRecord<?>) entity;
            if (metaEntity.getCreateDate() == null) {
                metaEntity.setCreateId(staff.getId());
                metaEntity.setCreateDate(now);
            }
            metaEntity.setUpdateId(staff.getId());
            metaEntity.setUpdateDate(now);
        }
        return false;
    }

}
