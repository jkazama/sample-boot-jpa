package sample.context.orm;

import java.io.Serializable;
import java.time.LocalDateTime;

import org.hibernate.EmptyInterceptor;
import org.hibernate.type.Type;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import lombok.*;
import sample.context.Timestamper;
import sample.context.actor.*;

/**
 * Entityの永続化タイミングでAOP処理を差し込むHibernateInterceptor。
 */
@Component
@Getter
@Setter
public class OrmInterceptor extends EmptyInterceptor {
    private static final long serialVersionUID = 1l;

    @Autowired
    private ActorSession session;
    @Autowired
    private Timestamper time;

    /**
     * 登録時の事前差し込み処理を行います。
     * @see org.hibernate.EmptyInterceptor#onSave(java.lang.Object, java.io.Serializable, java.lang.Object[], java.lang.String[], org.hibernate.type.Type[])
     */
    @Override
    public boolean onSave(Object entity, Serializable id, Object[] state,
            String[] propertyNames, Type[] types) {
        if (entity instanceof OrmActiveMetaRecord) {
            Actor staff = session.actor();
            LocalDateTime now = time.date();
            OrmActiveMetaRecord<?> metaEntity = (OrmActiveMetaRecord<?>) entity;
            metaEntity.setCreateId(staff.getId());
            metaEntity.setCreateDate(now);
            metaEntity.setUpdateId(staff.getId());
            metaEntity.setUpdateDate(now);
            for (int i = 0; i < propertyNames.length; i++) {
                if (propertyNames[i].equals("createId")) {
                    state[i] = metaEntity.getCreateId();
                } else if (propertyNames[i].equals("createDate")) {
                    state[i] = metaEntity.getCreateDate();
                } else if (propertyNames[i].equals("updateId")) {
                    state[i] = metaEntity.getUpdateId();
                } else if (propertyNames[i].equals("updateDate")) {
                    state[i] = metaEntity.getUpdateDate();
                }
            }
        }
        return false;
    }

    /**
     * 変更時の事前差し込み処理を行います。
     * @see org.hibernate.EmptyInterceptor#onFlushDirty(java.lang.Object, java.io.Serializable, java.lang.Object[], java.lang.Object[], java.lang.String[], org.hibernate.type.Type[])
     */
    @Override
    public boolean onFlushDirty(final Object entity, final Serializable id, final Object[] currentState,
            final Object[] previousState, final String[] propertyNames, final Type[] types) {
        if (entity instanceof OrmActiveMetaRecord) {
            Actor staff = session.actor();
            LocalDateTime now = time.date();
            OrmActiveMetaRecord<?> metaEntity = (OrmActiveMetaRecord<?>) entity;
            boolean create = false;
            if (metaEntity.getCreateDate() == null) {
                create = true;
                metaEntity.setCreateId(staff.getId());
                metaEntity.setCreateDate(now);
            }
            metaEntity.setUpdateId(staff.getId());
            metaEntity.setUpdateDate(now);
            for (int i = 0; i < propertyNames.length; i++) {
                if (create) {
                    if (propertyNames[i].equals("createId")) {
                        currentState[i] = metaEntity.getCreateId();
                    } else if (propertyNames[i].equals("createDate")) {
                        currentState[i] = metaEntity.getCreateDate();
                    }
                }
                if (propertyNames[i].equals("updateId")) {
                    currentState[i] = metaEntity.getUpdateId();
                } else if (propertyNames[i].equals("updateDate")) {
                    currentState[i] = metaEntity.getUpdateDate();
                }
            }
        }
        return false;
    }

}
