package sample.context;

import java.time.LocalDateTime;

/**
 * Marker interface for domain objects with the concept of meta-information.
 */
public interface DomainMetaEntity extends DomainEntity {

    String getCreateId();

    void setCreateId(String createId);

    LocalDateTime getCreateDate();

    void setCreateDate(LocalDateTime createDate);

    String getUpdateId();

    void setUpdateId(String updateId);

    LocalDateTime getUpdateDate();

    void setUpdateDate(LocalDateTime updateDate);

}
