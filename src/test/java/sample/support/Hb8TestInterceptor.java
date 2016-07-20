package sample.support;

import java.io.Serializable;

import org.hibernate.EmptyInterceptor;
import org.hibernate.type.Type;

import sample.context.orm.*;

/**
 * JPA AOP [ PrePersist / PreUpdate ] 代替なプログラマティック Interceptor
 * <p> see hibernate.ejb.interceptor
 */
public class Hb8TestInterceptor extends EmptyInterceptor {

    private static final long serialVersionUID = 1L;
    
    public OrmInterceptor interceptor() {
        return MockTemporaryContext.get(OrmInterceptor.class);
    }
    
    @Override
    public boolean onSave(Object entity, Serializable id, Object[] state,
            String[] propertyNames, Type[] types) {
        if (entity instanceof OrmActiveMetaRecord) {
            OrmActiveMetaRecord<?> metaEntity = (OrmActiveMetaRecord<?>) entity;
            interceptor().touchForCreate(metaEntity);
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

    @Override
    public boolean onFlushDirty(final Object entity, final Serializable id, final Object[] currentState,
            final Object[] previousState, final String[] propertyNames, final Type[] types) {
        if (entity instanceof OrmActiveMetaRecord) {
            OrmActiveMetaRecord<?> metaEntity = (OrmActiveMetaRecord<?>) entity;
            boolean create = (metaEntity.getCreateDate() == null);
            interceptor().touchForUpdate(metaEntity);
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
