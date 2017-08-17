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
 * Simple Accessor for EntityManager of JPA.
 * (you are formed every session, and please use it)
 * <p>If there is the handling of that You want to use by a method EntityManager,
 * add a wrap method as needed.
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

    /** Return an ID level of the entity. */
    @SuppressWarnings("unchecked")
    public <T> Serializable idValue(T entity) {
        return ((JpaEntityInformation<T, Serializable>)OrmUtils.entityInformation(em, entity.getClass())).getId(entity);
    }
    
    public <T> Optional<T> get(final CriteriaQuery<T> criteria) {
        return find(criteria).stream().findFirst();
    }

    public <T> T load(final CriteriaQuery<T> criteria) {
        return get(criteria).orElseThrow(() -> new ValidationException(ErrorKeys.EntityNotFound));
    }

    public <T> List<T> find(final CriteriaQuery<T> criteria) {
        return query(criteria).getResultList();
    }
    
    public <T> PagingList<T> find(final CriteriaQuery<T> criteria, Optional<CriteriaQuery<Long>> criteriaCount, final Pagination page) {
        Assert.notNull(page, "page is required.");
        long total = criteriaCount.map(cnt -> query(cnt).getResultList().get(0)).orElse(-1L);
        if (total == 0) return new PagingList<>(new ArrayList<>(), new Pagination(page, 0));
        
        TypedQuery<T> query = query(criteria);
        if (0 < page.getPage()) query.setFirstResult(page.getFirstResult());
        if (0 < page.getSize()) query.setMaxResults(page.getSize());
        return new PagingList<T>(query.getResultList(), new Pagination(page, total));
    }

    public <T> Optional<T> get(Class<T> entityClass, Function<OrmCriteria<T>, CriteriaQuery<T>> func) {
        return get(func.apply(OrmCriteria.of(em, entityClass)));
    }

    public <T> Optional<T> get(Class<T> entityClass, String alias, Function<OrmCriteria<T>, CriteriaQuery<T>> func) {
        return get(func.apply(OrmCriteria.of(em, entityClass, alias)));
    }

    public <T> T load(Class<T> entityClass, Function<OrmCriteria<T>, CriteriaQuery<T>> func) {
        return load(func.apply(OrmCriteria.of(em, entityClass)));
    }

    public <T> T load(Class<T> entityClass, String alias, Function<OrmCriteria<T>, CriteriaQuery<T>> func) {
        return load(func.apply(OrmCriteria.of(em, entityClass, alias)));
    }

    public <T> List<T> find(Class<T> entityClass, Function<OrmCriteria<T>, CriteriaQuery<T>> func) {
        return find(func.apply(OrmCriteria.of(em, entityClass)));
    }

    public <T> List<T> find(Class<T> entityClass, String alias, Function<OrmCriteria<T>, CriteriaQuery<T>> func) {
        return find(func.apply(OrmCriteria.of(em, entityClass, alias)));
    }

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
     * Return entity in JPQL.
     * <p>When you set Map in args, handle it as an argument with the names. (Map key is string only)
     */
    public <T> Optional<T> get(final String qlString, final Object... args) {
        List<T> list = find(qlString, args);
        return list.stream().findFirst();
    }

    /**
     * Return entity in JPQL.
     * <p>When you set Map in args, handle it as an argument with the names. (Map key is string only)
     */
    public <T> T load(final String qlString, final Object... args) {
        Optional<T> v = get(qlString, args);
        return v.orElseThrow(() -> new ValidationException(ErrorKeys.EntityNotFound));
    }

    public <T> List<T> loadAll(final Class<T> entityClass) {
        return find(OrmCriteria.of(em, entityClass).result());
    }

    /**
     * Find entity in JPQL.
     * <p>When you set Map in args, handle it as an argument with the names. (Map key is string only)
     */
    @SuppressWarnings("unchecked")
    public <T> List<T> find(final String qlString, final Object... args) {
        return bindArgs(em.createQuery(qlString), args).getResultList();
    }

    /**
     * Find paging entity in JPQL.
     * <p>When a count phrase is not built well, with Pagination#ignoreTotal as true,
     * calculate the number of total by a normal search separately.
     * <p>Because the sort condition set to the page is ignored,
     * do explicit setting at the time of qlString construction.
     * <p>When you set Map in args, handle it as an argument with the names. (Map key is string only)
     */
    @SuppressWarnings("unchecked")
    public <T> PagingList<T> find(final String qlString, final Pagination page, final Object... args) {
        long total = page.isIgnoreTotal() ? -1L : load(QueryUtils.createCountQueryFor(qlString), args);
        List<T> list = bindArgs(em.createQuery(qlString), page, args).getResultList();
        return new PagingList<>(list, new Pagination(page, total));
    }

    public <T> Optional<T> getNamed(final String name, final Object... args) {
        List<T> list = findNamed(name, args);
        return list.stream().findFirst();
    }

    public <T> T loadNamed(final String name, final Object... args) {
        Optional<T> v = getNamed(name, args);
        return v.orElseThrow(() -> new ValidationException(ErrorKeys.EntityNotFound));
    }

    @SuppressWarnings("unchecked")
    public <T> List<T> findNamed(final String name, final Object... args) {
        return bindArgs(em.createNamedQuery(name), args).getResultList();
    }

    @SuppressWarnings("unchecked")
    public <T> PagingList<T> findNamed(final String name, final String nameCount, final Pagination page, final Map<String, Object> args) {
        long total = page.isIgnoreTotal() ? -1L : loadNamed(nameCount, args);
        List<T> list = bindArgs(em.createNamedQuery(name), page, args).getResultList();
        return new PagingList<>(list, new Pagination(page, total));
    }

    /**
     * Find object in NativeQuery.
     * <p>A list of array is returned as search results.
     * <p>When you set Map in args, handle it as an argument with the names. (Map key is string only)
     */
    @SuppressWarnings("unchecked")
    public List<Object[]> findBySql(final String sql, final Object... args) {
        return bindArgs(em.createNativeQuery(sql), args).getResultList();
    }
    
    /**
     * Find entity in NativeQuery.
     * <p>When you set Map in args, handle it as an argument with the names. (Map key is string only)
     */
    @SuppressWarnings("unchecked")
    public <T> List<T> findBySql(String sql, Class<T> clazz, final Object... args) {
        return bindArgs(em.createNativeQuery(sql, clazz), args).getResultList();
    }

    /**
     * Find paging object in NativeQuery.
     * <p>A list of array is returned as search results.
     * <p>When you set Map in args, handle it as an argument with the names. (Map key is string only)
     */
    @SuppressWarnings("unchecked")
    public PagingList<Object[]> findBySql(String sql, String sqlCount, final Pagination page, final Object... args) {
        long total = page.isIgnoreTotal() ? -1L : findBySql(sqlCount, args).stream().findFirst().map(v -> Long.parseLong(v.toString())).orElse(0L);
        return new PagingList<Object[]>(bindArgs(em.createNativeQuery(sql), page, args).getResultList(), new Pagination(page, total));
    }
    
    /**
     * Find paging entity in NativeQuery.
     * <p>A list of array is returned as search results.
     * <p>When you set Map in args, handle it as an argument with the names. (Map key is string only)
     */
    @SuppressWarnings("unchecked")
    public <T> PagingList<T> findBySql(String sql, String sqlCount, Class<T> clazz, final Pagination page, final Object... args) {
        long total = page.isIgnoreTotal() ? -1L : findBySql(sqlCount, args).stream().findFirst().map(v -> Long.parseLong(v.toString())).orElse(0L);
        return new PagingList<T>(bindArgs(em.createNativeQuery(sql, clazz), page, args).getResultList(), new Pagination(page, total));
    }

    /**
     * Execute JPQL.
     * <p>When you set Map in args, handle it as an argument with the names. (Map key is string only)
     */
    public int execute(String qlString, final Object... args) {
        return bindArgs(em.createQuery(qlString), args).executeUpdate();
    }
    
    /**
     * Execute Named JPQL.
     * <p>{@literal @}NamedQuery definition with name is necessary beforehand.
     * <p>When you set Map in args, handle it as an argument with the names. (Map key is string only)
     */
    public int executeNamed(String name, final Object... args) {
        return bindArgs(em.createNamedQuery(name), args).executeUpdate();
    }
    
    /**
     * Execute NativeQuery.
     * <p>When you set Map in args, handle it as an argument with the names. (Map key is string only)
     */
    public int executeSql(String sql, final Object... args) {
        return bindArgs(em.createNativeQuery(sql), args).executeUpdate();
    }
    
    /** Execute Stored procedure.*/
    public void callStoredProcedure(String procedureName, Consumer<StoredProcedureQuery> proc) {
        proc.accept((StoredProcedureQuery)bindArgs(em.createStoredProcedureQuery(procedureName)));
    }

    /** Relate a value with a query. */
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