package sample.context.orm;

import java.io.Serializable;
import java.time.LocalDateTime;

import sample.context.Entity;

/**
 * The base class which gave registration / change meta concept to OrmActiveRecord.
 * <p>In Entity made in succession to this class, automatic meta information update
 *  is carried out at the time of permanency.
 * @see OrmInterceptor
 */
public abstract class OrmActiveMetaRecord<T extends Entity> extends OrmActiveRecord<T> implements Serializable, Entity {
    private static final long serialVersionUID = 1L;

    public abstract String getCreateId();
    public abstract void setCreateId(String createId);

    public abstract LocalDateTime getCreateDate();
    public abstract void setCreateDate(LocalDateTime createDate);

    public abstract String getUpdateId();
    public abstract void setUpdateId(String updateId);

    public abstract LocalDateTime getUpdateDate();
    public abstract void setUpdateDate(LocalDateTime updateDate);

}
