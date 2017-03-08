package sample.context.orm;

import java.io.Serializable;
import java.util.*;
import java.util.function.*;

import javax.persistence.*;
import javax.persistence.criteria.CriteriaQuery;

import org.springframework.data.jpa.repository.query.QueryUtils;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.util.Assert;

import sample.ValidationException;
import sample.ValidationException.ErrorKeys;

/**
 * JPA の EntityManager に対する簡易アクセサ。 ( セッション毎に生成して利用してください )
 * <p>EntityManager のメソッドで利用したい処理があれば必要に応じてラップメソッドを追加してください。
 */
public class OrmTemplate {

    private final EntityManager em;
    private final Optional<OrmQueryMetadata> metadata;

    public OrmTemplate(EntityManager em) {
        this.em = em;
        this.metadata = Optional.empty();
    }
    
    public OrmTemplate(EntityManager em, OrmQueryMetadata metadata) {
        this.em = em;
        this.metadata = Optional.ofNullable(metadata);
    }

    private <T> TypedQuery<T> query(final CriteriaQuery<T> query) {
        TypedQuery<T> q = em.createQuery(query);
        metadata.ifPresent(meta -> {
            meta.hints().forEach((k, v) -> q.setHint(k, v)); 
            meta.lockMode().ifPresent(l -> q.setLockMode(l)); 
        });
        return q;
    }

    /** 指定したエンティティの ID 値を取得します。 */
    @SuppressWarnings("unchecked")
    public <T> Serializable idValue(T entity) {
        return ((JpaEntityInformation<T, Serializable>)OrmUtils.entityInformation(em, entity.getClass())).getId(entity);
    }
    
    /** Criteriaで一件取得します。 */
    public <T> Optional<T> get(final CriteriaQuery<T> criteria) {
        return find(criteria).stream().findFirst();
    }

    /** Criteriaで一件取得します。(存在しない時はValidationException) */
    public <T> T load(final CriteriaQuery<T> criteria) {
        return get(criteria).orElseThrow(() -> new ValidationException(ErrorKeys.EntityNotFound));
    }

    /**
     * Criteria で検索します。
     * ※ランダムな条件検索等、可変条件検索が必要となる時に利用して下さい
     */
    public <T> List<T> find(final CriteriaQuery<T> criteria) {
        return query(criteria).getResultList();
    }
    
    /**
     * Criteria でページング検索します。
     * <p>Pagination に設定された検索条件は無視されます。 CriteriaQuery 構築時に設定するようにしてください。
     */   
    public <T> PagingList<T> find(final CriteriaQuery<T> criteria, Optional<CriteriaQuery<Long>> criteriaCount, final Pagination page) {
        Assert.notNull(page, "page is required");
        long total = criteriaCount.map(cnt -> query(cnt).getResultList().get(0)).orElse(-1L);
        if (total == 0) return new PagingList<>(new ArrayList<>(), new Pagination(page, 0));
        
        TypedQuery<T> query = query(criteria);
        if (0 < page.getPage()) query.setFirstResult(page.getFirstResult());
        if (0 < page.getSize()) query.setMaxResults(page.getSize());
        return new PagingList<T>(query.getResultList(), new Pagination(page, total));
    }


    /**
     * Criteriaで一件取得します。
     * <p>クロージャ戻り値は引数に取るOrmCriteriaのresult*の実行結果を返すようにしてください。
     */
    public <T> Optional<T> get(Class<T> entityClass, Function<OrmCriteria<T>, CriteriaQuery<T>> func) {
        return get(func.apply(OrmCriteria.of(em, entityClass)));
    }

    public <T> Optional<T> get(Class<T> entityClass, String alias, Function<OrmCriteria<T>, CriteriaQuery<T>> func) {
        return get(func.apply(OrmCriteria.of(em, entityClass, alias)));
    }

    /**
     * Criteriaで一件取得します。(存在しない時はValidationException)
     * <p>クロージャ戻り値は引数に取るOrmCriteriaのresult*の実行結果を返すようにしてください。
     */
    public <T> T load(Class<T> entityClass, Function<OrmCriteria<T>, CriteriaQuery<T>> func) {
        return load(func.apply(OrmCriteria.of(em, entityClass)));
    }

    public <T> T load(Class<T> entityClass, String alias, Function<OrmCriteria<T>, CriteriaQuery<T>> func) {
        return load(func.apply(OrmCriteria.of(em, entityClass, alias)));
    }

    /**
     * Criteriaで検索します。
     * <p>クロージャ戻り値は引数に取るOrmCriteriaのresult*の実行結果を返すようにしてください。
     */
    public <T> List<T> find(Class<T> entityClass, Function<OrmCriteria<T>, CriteriaQuery<T>> func) {
        return find(func.apply(OrmCriteria.of(em, entityClass)));
    }

    public <T> List<T> find(Class<T> entityClass, String alias, Function<OrmCriteria<T>, CriteriaQuery<T>> func) {
        return find(func.apply(OrmCriteria.of(em, entityClass, alias)));
    }

    /**
     * Criteriaでページング検索します。
     * <p>Pagination に設定された検索条件は無視されます。 OrmCriteria 構築時に設定するようにしてください。
     */
    public <T> PagingList<T> find(Class<T> entityClass, Function<OrmCriteria<T>, OrmCriteria<T>> func,
            final Pagination page) {
        OrmCriteria<T> criteria = OrmCriteria.of(em, entityClass);
        func.apply(criteria);
        return find(criteria.result(), page.isIgnoreTotal() ? Optional.empty() : Optional.of(criteria.resultCount()), page);
    }
    
    public <T> PagingList<T> find(Class<T> entityClass, String alias, Function<OrmCriteria<T>, OrmCriteria<T>> func,
            final Pagination page) {
        OrmCriteria<T> criteria = OrmCriteria.of(em, entityClass, alias);
        func.apply(criteria);
        return find(criteria.result(), page.isIgnoreTotal() ? Optional.empty() : Optional.of(criteria.resultCount()), page);
    }
    
    /**
     * Criteriaでページング検索します。
     * <p>CriteriaQuery が提供する subquery や groupBy 等の構文を利用したい時はこちらの extension で指定してください。
     * <p>Pagination に設定された検索条件は無視されます。 OrmCriteria 構築時に設定するようにしてください。
     */
    public <T> PagingList<T> find(Class<T> entityClass, Function<OrmCriteria<T>, OrmCriteria<T>> func,
            Function<CriteriaQuery<?>, CriteriaQuery<?>> extension, final Pagination page) {
        OrmCriteria<T> criteria = OrmCriteria.of(em, entityClass);
        func.apply(criteria);
        return find(criteria.result(extension), page.isIgnoreTotal() ? Optional.empty() : Optional.of(criteria.resultCount(extension)), page);
    }
    
    public <T> PagingList<T> find(Class<T> entityClass, String alias, Function<OrmCriteria<T>, OrmCriteria<T>> func,
            Function<CriteriaQuery<?>, CriteriaQuery<?>> extension, final Pagination page) {
        OrmCriteria<T> criteria = OrmCriteria.of(em, entityClass, alias);
        func.apply(criteria);
        return find(criteria.result(extension), page.isIgnoreTotal() ? Optional.empty() : Optional.of(criteria.resultCount(extension)), page);
    }

    /**
     * JPQL で一件取得します。
     * <p>args に Map を指定した時は名前付き引数として取り扱います。 ( Map のキーには文字列を指定してください )
     */
    public <T> Optional<T> get(final String qlString, final Object... args) {
        List<T> list = find(qlString, args);
        return list.stream().findFirst();
    }

    /**
     * JPQL で一件取得します。(存在しない時は ValidationException )
     * <p>args に Map を指定した時は名前付き引数として取り扱います。 ( Map のキーには文字列を指定してください )
     */
    public <T> T load(final String qlString, final Object... args) {
        Optional<T> v = get(qlString, args);
        return v.orElseThrow(() -> new ValidationException(ErrorKeys.EntityNotFound));
    }

    /** 対象 Entity を全件取得します。*/
    public <T> List<T> loadAll(final Class<T> entityClass) {
        return find(OrmCriteria.of(em, entityClass).result());
    }

    /**
     * JPQL で検索します。
     * <p>args に Map を指定した時は名前付き引数として取り扱います。 ( Map のキーには文字列を指定してください )
     */
    @SuppressWarnings("unchecked")
    public <T> List<T> find(final String qlString, final Object... args) {
        return bindArgs(em.createQuery(qlString), args).getResultList();
    }

    /**
     * JPQL でページング検索します。
     * <p>カウント句がうまく構築されない時はPagination#ignoreTotalをtrueにして、
     * 別途通常の検索でトータル件数を算出するようにして下さい。
     * <p>page に設定されたソート条件は無視されるので、 qlString 構築時に明示的な設定をしてください。
     * <p>args に Map を指定した時は名前付き引数として取り扱います。 ( Map のキーには文字列を指定してください )
     */
    @SuppressWarnings("unchecked")
    public <T> PagingList<T> find(final String qlString, final Pagination page, final Object... args) {
        long total = page.isIgnoreTotal() ? -1L : load(QueryUtils.createCountQueryFor(qlString), args);
        List<T> list = bindArgs(em.createQuery(qlString), args, page).getResultList();
        return new PagingList<>(list, new Pagination(page, total));
    }

    /**
     * 定義済み JPQL で一件取得します。
     * <p>事前に name に合致する @NamedQuery 定義が必要です。
     * <p>args に Map を指定した時は名前付き引数として取り扱います。 ( Map のキーには文字列を指定してください )
     */
    public <T> Optional<T> getNamed(final String name, final Object... args) {
        List<T> list = findNamed(name, args);
        return list.stream().findFirst();
    }

    /**
     * 定義済み JPQL で一件取得をします。(存在しない時は ValidationException )
     * <p>事前に name に合致する @NamedQuery 定義が必要です。
     * <p>args に Map を指定した時は名前付き引数として取り扱います。 ( Map のキーには文字列を指定してください )
     */
    public <T> T loadNamed(final String name, final Object... args) {
        Optional<T> v = getNamed(name, args);
        return v.orElseThrow(() -> new ValidationException(ErrorKeys.EntityNotFound));
    }

    /**
     * 定義済み JPQL で検索します。
     * <p>事前に name に合致する @NamedQuery 定義が必要です。
     * <p>args に Map を指定した時は名前付き引数として取り扱います。 ( Map のキーには文字列を指定してください )
     */
    @SuppressWarnings("unchecked")
    public <T> List<T> findNamed(final String name, final Object... args) {
        return bindArgs(em.createNamedQuery(name), args).getResultList();
    }

    /**
     * 定義済み JPQL でページング検索します。
     * <p>事前に name に合致する @NamedQuery 定義が必要です。
     * <p>args に Map を指定した時は名前付き引数として取り扱います。 ( Map のキーには文字列を指定してください )
     * <p>page に設定されたソート条件は無視されます。
     */
    @SuppressWarnings("unchecked")
    public <T> PagingList<T> findNamed(final String name, final String nameCount, final Pagination page, final Map<String, Object> args) {
        long total = page.isIgnoreTotal() ? -1L : loadNamed(nameCount, args);
        List<T> list = bindArgs(em.createNamedQuery(name), page, args).getResultList();
        return new PagingList<>(list, new Pagination(page, total));
    }

    /**
     * SQLで検索します。
     * <p>検索結果としてselectの値配列一覧が返されます。
     * <p>args に Map を指定した時は名前付き引数として取り扱います。 ( Map のキーには文字列を指定してください )
     */
    @SuppressWarnings("unchecked")
    public <T> List<T> findBySql(final String sql, final Object... args) {
        return bindArgs(em.createNativeQuery(sql), args).getResultList();
    }
    
    /**
     * SQL で検索します。
     * <p>args に Map を指定した時は名前付き引数として取り扱います。 ( Map のキーには文字列を指定してください )
     */
    @SuppressWarnings("unchecked")
    public <T> List<T> findBySql(String sql, Class<T> clazz, final Object... args) {
        return bindArgs(em.createNativeQuery(sql, clazz), args).getResultList();
    }

    /**
     * SQL でページング検索します。
     * <p>検索結果として select の値配列一覧が返されます。
     * <p>args に Map を指定した時は名前付き引数として取り扱います。 ( Map のキーには文字列を指定してください )
     */
    @SuppressWarnings("unchecked")
    public <T> PagingList<T> findBySql(String sql, String sqlCount, final Pagination page, final Object... args) {
        long total = page.isIgnoreTotal() ? -1L : findBySql(sqlCount, args).stream().findFirst().map(v -> Long.parseLong(v.toString())).orElse(0L);
        return new PagingList<T>(bindArgs(em.createNativeQuery(sql), page, args).getResultList(), new Pagination(page, total));
    }
    
    /**
     * SQL でページング検索します。
     * <p>page に設定されたソート条件は無視されるので、 sql 構築時に明示的な設定をしてください。
     * <p>args に Map を指定した時は名前付き引数として取り扱います。 ( Map のキーには文字列を指定してください )
     */
    @SuppressWarnings("unchecked")
    public <T> PagingList<T> findBySql(String sql, String sqlCount, Class<T> clazz, final Pagination page, final Object... args) {
        long total = page.isIgnoreTotal() ? -1L : findBySql(sqlCount, args).stream().findFirst().map(v -> Long.parseLong(v.toString())).orElse(0L);
        return new PagingList<T>(bindArgs(em.createNativeQuery(sql, clazz), page, args).getResultList(), new Pagination(page, total));
    }

    /**
     * JPQL を実行します。
     * <p>args に Map を指定した時は名前付き引数として取り扱います。 ( Map のキーには文字列を指定してください )
     */
    public int execute(String qlString, final Object... args) {
        return bindArgs(em.createQuery(qlString), args).executeUpdate();
    }
    
    /**
     * 定義済み JPQL を実行します。
     * <p>事前に name に合致する @NamedQuery 定義が必要です。
     * <p>args に Map を指定した時は名前付き引数として取り扱います。 ( Map のキーには文字列を指定してください )
     */
    public int executeNamed(String name, final Object... args) {
        return bindArgs(em.createNamedQuery(name), args).executeUpdate();
    }
    
    /**
     * SQL を実行をします。
     * <p>args に Map を指定した時は名前付き引数として取り扱います。 ( Map のキーには文字列を指定してください )
     */
    public int executeSql(String sql, final Object... args) {
        return bindArgs(em.createNativeQuery(sql), args).executeUpdate();
    }
    
    /** ストアド を処理をします。*/
    public void callStoredProcedure(String procedureName, Consumer<StoredProcedureQuery> proc) {
        proc.accept((StoredProcedureQuery)bindArgs(em.createStoredProcedureQuery(procedureName)));
    }

    /**
     * クエリに値を紐付けします。
     * <p>Map 指定時はキーに文字を指定します。それ以外は自動的に 1 開始のポジション指定をおこないます。
     */
    public Query bindArgs(final Query query, final Object... args) {
        return bindArgs(query, null, args);
    }

    public Query bindArgs(final Query query, final Pagination page, final Object... args) {
        Optional.ofNullable(page).ifPresent((pg) -> {
            if (page.getPage() > 0)
                query.setFirstResult(page.getFirstResult());
            if (page.getSize() > 0)
                query.setMaxResults(page.getSize());
        });
        if (args != null) {
            for (int i = 0; i < args.length; i++) {
                Object arg = args[i];
                if (arg instanceof Map<?, ?>) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> argNamed = (Map<String, Object>)arg;
                    argNamed.forEach((k, v) -> query.setParameter(k, v));
                } else {
                    query.setParameter(i + 1, args[i]);
                }
            }
        }
        return query;
    }

}