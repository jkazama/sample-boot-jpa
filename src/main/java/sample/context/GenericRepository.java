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
     */
    <T extends DomainEntity> Optional<T> get(final Class<T> clazz, final Object id);

    /**
     * プライマリキーに一致する{@link DomainEntity}を返します。
     *
     * @param <T>   戻り値の型
     * @param clazz 取得するインスタンスのクラス
     * @param id    プライマリキー
     * @return プライマリキーに一致した{@link DomainEntity}。一致しない時は例外。
     */
    <T extends DomainEntity> T load(final Class<T> clazz, final Object id);

    /**
     * プライマリキーに一致する{@link DomainEntity}を返します。
     * <p>
     * ロック付(for update)で取得を行うため、デッドロック回避を意識するようにしてください。
     *
     * @param <T>   戻り値の型
     * @param clazz 取得するインスタンスのクラス
     * @param id    プライマリキー
     * @return プライマリキーに一致した{@link DomainEntity}。一致しない時は例外。
     */
    <T extends DomainEntity> T loadForUpdate(final Class<T> clazz, final Object id);

    /**
     * プライマリキーに一致する{@link DomainEntity}が存在するか返します。
     *
     * @param <T>   確認型
     * @param clazz 対象クラス
     * @param id    プライマリキー
     * @return 存在する時はtrue
     */
    <T extends DomainEntity> boolean exists(final Class<T> clazz, final Object id);

    /**
     * 管理する{@link DomainEntity}を全件返します。
     * 条件検索などは#templateを利用して実行するようにしてください。
     *
     * @param <T>   戻り値の型
     * @param clazz 取得するインスタンスのクラス
     * @return {@link DomainEntity}一覧
     */
    <T extends DomainEntity> List<T> findAll(final Class<T> clazz);

    /**
     * {@link DomainEntity}を新規追加します。
     *
     * @param entity 追加対象{@link DomainEntity}
     * @return 追加した{@link DomainEntity}のプライマリキー
     */
    <T extends DomainEntity> T save(final T entity);

    /**
     * {@link DomainEntity}を新規追加または更新します。
     * <p>
     * 既に同一のプライマリキーが存在するときは更新。
     * 存在しない時は新規追加となります。
     *
     * @param entity 追加対象{@link DomainEntity}
     */
    <T extends DomainEntity> T saveOrUpdate(final T entity);

    /**
     * {@link DomainEntity}を更新します。
     *
     * @param entity 更新対象{@link DomainEntity}
     */
    <T extends DomainEntity> T update(final T entity);

    /**
     * {@link DomainEntity}を削除します。
     *
     * @param entity 削除対象{@link DomainEntity}
     */
    <T extends DomainEntity> T delete(final T entity);

}
