package sample.context.orm;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.query.QueryUtils;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.persistence.StoredProcedureQuery;
import lombok.RequiredArgsConstructor;
import sample.context.ErrorKeys;
import sample.context.ValidationException;

/**
 * JPA の EntityManager に対する簡易アクセサ。 ( セッション毎に生成して利用してください )
 * <p>
 * EntityManager のメソッドで利用したい処理があれば必要に応じてラップメソッドを追加してください。
 */
@RequiredArgsConstructor(staticName = "of")
public class OrmTemplate {
    private final EntityManager em;

    /** 指定したエンティティの ID 値を取得します。 */
    public <T> Object idValue(T entity) {
        var info = OrmUtils.entityInformation(em, entity.getClass());
        return info.getId(entity);
    }

    /**
     * JPQL で一件取得します。
     * <p>
     * args に Map を指定した時は名前付き引数として取り扱います。 ( Map のキーには文字列を指定してください )
     */
    public <T> Optional<T> get(final String qlString, final Object... args) {
        List<T> list = find(qlString, args);
        return list.stream().findFirst();
    }

    /**
     * JPQL で一件取得します。(存在しない時は ValidationException )
     * <p>
     * args に Map を指定した時は名前付き引数として取り扱います。 ( Map のキーには文字列を指定してください )
     */
    public <T> T load(final String qlString, final Object... args) {
        Optional<T> v = get(qlString, args);
        return v.orElseThrow(() -> new ValidationException(ErrorKeys.EntityNotFound));
    }

    /**
     * JPQL で検索します。
     * <p>
     * args に Map を指定した時は名前付き引数として取り扱います。 ( Map のキーには文字列を指定してください )
     */
    @SuppressWarnings("unchecked")
    public <T> List<T> find(final String qlString, final Object... args) {
        return bindArgs(em.createQuery(qlString), args).getResultList();
    }

    /**
     * JPQL でページング検索します。
     * <p>
     * カウント句がうまく構築されない時はPagination#ignoreTotalをtrueにして、
     * 別途通常の検索でトータル件数を算出するようにして下さい。
     * <p>
     * page に設定されたソート条件は無視されるので、 qlString 構築時に明示的な設定をしてください。
     * <p>
     * args に Map を指定した時は名前付き引数として取り扱います。 ( Map のキーには文字列を指定してください )
     */
    @SuppressWarnings("unchecked")
    public <T> Page<T> find(final String qlString, final Pageable page, final Object... args) {
        @SuppressWarnings("deprecation")
        long total = load(QueryUtils.createCountQueryFor(qlString), args);
        List<T> list = bindArgs(em.createQuery(qlString), page, args).getResultList();
        return new PageImpl<>(list, page, total);
    }

    /**
     * 定義済み JPQL で一件取得します。
     * <p>
     * 事前に name に合致する @NamedQuery 定義が必要です。
     * <p>
     * args に Map を指定した時は名前付き引数として取り扱います。 ( Map のキーには文字列を指定してください )
     */
    public <T> Optional<T> getNamed(final String name, final Object... args) {
        List<T> list = findNamed(name, args);
        return list.stream().findFirst();
    }

    /**
     * 定義済み JPQL で一件取得をします。(存在しない時は ValidationException )
     * <p>
     * 事前に name に合致する @NamedQuery 定義が必要です。
     * <p>
     * args に Map を指定した時は名前付き引数として取り扱います。 ( Map のキーには文字列を指定してください )
     */
    public <T> T loadNamed(final String name, final Object... args) {
        Optional<T> v = getNamed(name, args);
        return v.orElseThrow(() -> new ValidationException(ErrorKeys.EntityNotFound));
    }

    /**
     * 定義済み JPQL で検索します。
     * <p>
     * 事前に name に合致する @NamedQuery 定義が必要です。
     * <p>
     * args に Map を指定した時は名前付き引数として取り扱います。 ( Map のキーには文字列を指定してください )
     */
    @SuppressWarnings("unchecked")
    public <T> List<T> findNamed(final String name, final Object... args) {
        return bindArgs(em.createNamedQuery(name), args).getResultList();
    }

    /**
     * 定義済み JPQL でページング検索します。
     * <p>
     * 事前に name に合致する @NamedQuery 定義が必要です。
     * <p>
     * args に Map を指定した時は名前付き引数として取り扱います。 ( Map のキーには文字列を指定してください )
     * <p>
     * page に設定されたソート条件は無視されます。
     */
    @SuppressWarnings("unchecked")
    public <T> Page<T> findNamed(
            String name,
            String nameCount,
            final Pageable page,
            final Map<String, Object> args) {
        long total = loadNamed(nameCount, args);
        List<T> list = bindArgs(em.createNamedQuery(name), page, args).getResultList();
        return new PageImpl<>(list, page, total);
    }

    /**
     * SQLで検索します。
     * <p>
     * 検索結果としてselectの値配列一覧が返されます。
     * <p>
     * args に Map を指定した時は名前付き引数として取り扱います。 ( Map のキーには文字列を指定してください )
     */
    @SuppressWarnings("unchecked")
    public <T> List<T> findBySql(final String sql, final Object... args) {
        return bindArgs(em.createNativeQuery(sql), args).getResultList();
    }

    /**
     * SQL で検索します。
     * <p>
     * args に Map を指定した時は名前付き引数として取り扱います。 ( Map のキーには文字列を指定してください )
     */
    @SuppressWarnings("unchecked")
    public <T> List<T> findBySql(String sql, Class<T> clazz, final Object... args) {
        return bindArgs(em.createNativeQuery(sql, clazz), args).getResultList();
    }

    /**
     * SQL でページング検索します。
     * <p>
     * 検索結果として select の値配列一覧が返されます。
     * <p>
     * args に Map を指定した時は名前付き引数として取り扱います。 ( Map のキーには文字列を指定してください )
     */
    @SuppressWarnings("unchecked")
    public <T> Page<T> findBySql(
            String sql,
            String sqlCount,
            final Pageable page,
            final Object... args) {
        long total = findBySql(sqlCount, args).stream()
                .findFirst()
                .map(v -> Long.parseLong(v.toString()))
                .orElse(0L);
        return new PageImpl<>(
                bindArgs(em.createNativeQuery(sql), page, args).getResultList(),
                page,
                total);
    }

    /**
     * SQL でページング検索します。
     * <p>
     * page に設定されたソート条件は無視されるので、 sql 構築時に明示的な設定をしてください。
     * <p>
     * args に Map を指定した時は名前付き引数として取り扱います。 ( Map のキーには文字列を指定してください )
     */
    @SuppressWarnings("unchecked")
    public <T> Page<T> findBySql(
            String sql,
            String sqlCount,
            Class<T> clazz,
            final Pageable page,
            final Object... args) {
        long total = findBySql(sqlCount, args).stream()
                .findFirst()
                .map(v -> Long.parseLong(v.toString()))
                .orElse(0L);
        return new PageImpl<>(
                bindArgs(em.createNativeQuery(sql, clazz), page, args).getResultList(),
                page,
                total);
    }

    /**
     * JPQL を実行します。
     * <p>
     * args に Map を指定した時は名前付き引数として取り扱います。 ( Map のキーには文字列を指定してください )
     */
    public int execute(String qlString, final Object... args) {
        return bindArgs(em.createQuery(qlString), args).executeUpdate();
    }

    /**
     * 定義済み JPQL を実行します。
     * <p>
     * 事前に name に合致する @NamedQuery 定義が必要です。
     * <p>
     * args に Map を指定した時は名前付き引数として取り扱います。 ( Map のキーには文字列を指定してください )
     */
    public int executeNamed(String name, final Object... args) {
        return bindArgs(em.createNamedQuery(name), args).executeUpdate();
    }

    /**
     * SQL を実行をします。
     * <p>
     * args に Map を指定した時は名前付き引数として取り扱います。 ( Map のキーには文字列を指定してください )
     */
    public int executeSql(String sql, final Object... args) {
        return bindArgs(em.createNativeQuery(sql), args).executeUpdate();
    }

    /** ストアド を処理をします。 */
    public void callStoredProcedure(String procedureName, Consumer<StoredProcedureQuery> proc) {
        proc.accept((StoredProcedureQuery) bindArgs(em.createStoredProcedureQuery(procedureName)));
    }

    /**
     * クエリに値を紐付けします。
     * <p>
     * Map 指定時はキーに文字を指定します。それ以外は自動的に 1 開始のポジション指定をおこないます。
     */
    public Query bindArgs(final Query query, final Object... args) {
        return bindArgs(query, null, args);
    }

    public Query bindArgs(final Query query, final Pageable page, final Object... args) {
        Optional.ofNullable(page).ifPresent((pg) -> {
            if (page.getPageNumber() > 0) {
                query.setFirstResult((int) page.getOffset());
            }
            if (page.getPageSize() > 0) {
                query.setMaxResults(page.getPageSize());
            }
        });
        if (args != null) {
            for (int i = 0; i < args.length; i++) {
                Object arg = args[i];
                if (arg instanceof Map<?, ?>) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> argNamed = (Map<String, Object>) arg;
                    argNamed.forEach((k, v) -> query.setParameter(k, v));
                } else {
                    query.setParameter(i + 1, args[i]);
                }
            }
        }
        return query;
    }

}
