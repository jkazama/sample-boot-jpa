package sample.context;

import java.util.List;
import java.util.Optional;

/**
 * It is a generic Repository that does not depend on a specific domain object.
 * <p>
 * It can be used as a non-typesafe Repository.
 */
public interface GenericRepository {

    /**
     * Returns helper utilities that provide access to infrastructure layer
     * components at the domain layer.
     */
    DomainHelper dh();

    /**
     * Returns {@link DomainEntity} matching the primary key.
     *
     * @param <T>   Return value type
     * @param clazz Class of the instance to retrieve
     * @param id    Primary key
     * @return {@link DomainEntity} matching the primary key. Throws exception if
     *         not found.
     */
    <T extends DomainEntity> Optional<T> get(final Class<T> clazz, final Object id);

    /**
     * Returns {@link DomainEntity} matching the primary key.
     *
     * @param <T>   Return value type
     * @param clazz Class of the instance to retrieve
     * @param id    Primary key
     * @return {@link DomainEntity} matching the primary key. Throws exception if
     *         not found.
     */
    <T extends DomainEntity> T load(final Class<T> clazz, final Object id);

    /**
     * Returns {@link DomainEntity} matching the primary key.
     * <p>
     * Acquires with lock (for update), so please be mindful of deadlock avoidance.
     *
     * @param <T>   Return value type
     * @param clazz Class of the instance to retrieve
     * @param id    Primary key
     * @return {@link DomainEntity} matching the primary key. Throws exception if
     *         not found.
     */
    <T extends DomainEntity> T loadForUpdate(final Class<T> clazz, final Object id);

    /**
     * Returns whether a {@link DomainEntity} matching the primary key exists.
     *
     * @param <T>   Verification type
     * @param clazz Target class
     * @param id    Primary key
     * @return true if exists
     */
    <T extends DomainEntity> boolean exists(final Class<T> clazz, final Object id);

    /**
     * Returns all managed {@link DomainEntity} instances.
     * For conditional searches, please use #template.
     *
     * @param <T>   Return value type
     * @param clazz Class of the instance to retrieve
     * @return List of {@link DomainEntity}
     */
    <T extends DomainEntity> List<T> findAll(final Class<T> clazz);

    /**
     * Adds a new {@link DomainEntity}.
     *
     * @param entity {@link DomainEntity} to add
     * @return Primary key of the added {@link DomainEntity}
     */
    <T extends DomainEntity> T save(final T entity);

    /**
     * Adds a new {@link DomainEntity} or updates an existing one.
     * <p>
     * Updates if the same primary key already exists.
     * Creates new if it doesn't exist.
     *
     * @param entity {@link DomainEntity} to add
     */
    <T extends DomainEntity> T saveOrUpdate(final T entity);

    /**
     * Updates a {@link DomainEntity}.
     *
     * @param entity {@link DomainEntity} to update
     */
    <T extends DomainEntity> T update(final T entity);

    /**
     * Deletes a {@link DomainEntity}.
     *
     * @param entity {@link DomainEntity} to delete
     */
    <T extends DomainEntity> T delete(final T entity);

}
