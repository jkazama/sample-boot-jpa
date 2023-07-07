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
 * A simple accessor to JPA's EntityManager. (Generate and use it for each
 * session.)
 * <p>
 * If there are any EntityManager methods that you wish to use, add wrap methods
 * as necessary.
 */
@RequiredArgsConstructor(staticName = "of")
public class OrmTemplate {
    private final EntityManager em;

    /** Returns the ID value of the specified entity. */
    public <T> Object idValue(T entity) {
        var info = OrmUtils.entityInformation(em, entity.getClass());
        return info.getId(entity);
    }

    /**
     * Returns one case with JPQL.
     * <p>
     * When Map is specified in args, it is treated as a named argument. (The key of
     * Map must be a string.)
     */
    public <T> Optional<T> get(String qlString, final Object... args) {
        List<T> list = find(qlString, args);
        return list.stream().findFirst();
    }

    /**
     * Returns one case in JPQL. (ValidationException if it does not exist )
     * <p>
     * When Map is specified in args, it is treated as a named argument. (The key of
     * Map must be a string.)
     */
    public <T> T load(String qlString, final Object... args) {
        Optional<T> v = get(qlString, args);
        return v.orElseThrow(() -> new ValidationException(ErrorKeys.EntityNotFound));
    }

    /**
     * Search by JPQL.
     * <p>
     * When Map is specified in args, it is treated as a named argument. (The key of
     * Map must be a string.)
     */
    @SuppressWarnings("unchecked")
    public <T> List<T> find(String qlString, final Object... args) {
        return bindArgs(em.createQuery(qlString), args).getResultList();
    }

    /**
     * Paging search in JPQL.
     * <p>
     * When Map is specified in args, it is treated as a named argument. (The key of
     * Map must be a string.)
     */
    @SuppressWarnings("unchecked")
    public <T> Page<T> find(String qlString, final Pageable page, final Object... args) {
        @SuppressWarnings("deprecation")
        long total = load(QueryUtils.createCountQueryFor(qlString), args);
        List<T> list = bindArgs(em.createQuery(qlString), page, args).getResultList();
        return new PageImpl<>(list, page, total);
    }

    /**
     * Paging search in JPQL.
     * <p>
     * When Map is specified in args, it is treated as a named argument. (The key of
     * Map must be a string.)
     */
    @SuppressWarnings("unchecked")
    public <T> Page<T> find(String qlString, String qlCount, final Pageable page, final Object... args) {
        long total = load(qlCount, args);
        List<T> list = bindArgs(em.createQuery(qlString), page, args).getResultList();
        return new PageImpl<>(list, page, total);
    }

    /**
     * Returns one case with predefined JPQL.
     * <p>
     * A prior @NamedQuery definition matching name is required.
     * <p>
     * When Map is specified in args, it is treated as a named argument. (The key of
     * Map must be a string.)
     */
    public <T> Optional<T> getNamed(String name, final Object... args) {
        List<T> list = findNamed(name, args);
        return list.stream().findFirst();
    }

    /**
     * Returns one case with predefined JPQL. (ValidationException if it does not
     * exist)
     * <p>
     * A prior @NamedQuery definition matching name is required.
     * <p>
     * When Map is specified in args, it is treated as a named argument. (The key of
     * Map must be a string.)
     */
    public <T> T loadNamed(String name, final Object... args) {
        Optional<T> v = getNamed(name, args);
        return v.orElseThrow(() -> new ValidationException(ErrorKeys.EntityNotFound));
    }

    /**
     * Search by predefined JPQL.
     * <p>
     * A prior @NamedQuery definition matching name is required.
     * <p>
     * When Map is specified in args, it is treated as a named argument. (The key of
     * Map must be a string.)
     */
    @SuppressWarnings("unchecked")
    public <T> List<T> findNamed(String name, final Object... args) {
        return bindArgs(em.createNamedQuery(name), args).getResultList();
    }

    /**
     * Search by predefined JPQL.
     * <p>
     * A prior @NamedQuery definition matching name is required.
     * <p>
     * When Map is specified in args, it is treated as a named argument. (The key of
     * Map must be a string.)
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
     * Search in SQL.
     * <p>
     * A list of select value arrays is returned as a result of the search.
     * <p>
     * When Map is specified in args, it is treated as a named argument. (The key of
     * Map must be a string.)
     */
    @SuppressWarnings("unchecked")
    public <T> List<T> findBySql(String sql, final Object... args) {
        return bindArgs(em.createNativeQuery(sql), args).getResultList();
    }

    /**
     * Search in SQL.
     * <p>
     * When Map is specified in args, it is treated as a named argument. (The key of
     * Map must be a string.)
     */
    @SuppressWarnings("unchecked")
    public <T> List<T> findBySql(String sql, Class<T> clazz, final Object... args) {
        return bindArgs(em.createNativeQuery(sql, clazz), args).getResultList();
    }

    /**
     * SQL paging search.
     * <p>
     * A list of value arrays of select is returned as a result of the search.
     * <p>
     * When Map is specified in args, it is treated as a named argument. (The key of
     * Map must be a string.)
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
     * SQL paging search.
     * <p>
     * When Map is specified in args, it is treated as a named argument. (The key of
     * Map must be a string.)
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
     * Execute JPQL.
     * <p>
     * When Map is specified in args, it is treated as a named argument. (The key of
     * Map must be a string.)
     */
    public int execute(String qlString, final Object... args) {
        return bindArgs(em.createQuery(qlString), args).executeUpdate();
    }

    /**
     * Execute predefined JPQL.
     * <p>
     * A prior @NamedQuery definition matching name is required.
     * <p>
     * When Map is specified in args, it is treated as a named argument. (The key of
     * Map must be a string.)
     */
    public int executeNamed(String name, final Object... args) {
        return bindArgs(em.createNamedQuery(name), args).executeUpdate();
    }

    /**
     * Execute SQL.
     * <p>
     * When Map is specified in args, it is treated as a named argument. (The key of
     * Map must be a string.)
     */
    public int executeSql(String sql, final Object... args) {
        return bindArgs(em.createNativeQuery(sql), args).executeUpdate();
    }

    /** Processes stored data. */
    public void callStoredProcedure(String procedureName, Consumer<StoredProcedureQuery> proc) {
        proc.accept((StoredProcedureQuery) bindArgs(em.createStoredProcedureQuery(procedureName)));
    }

    /**
     * Tie the value to the query.
     * <p>
     * When Map is specified in args, it is treated as a named argument. (The key of
     * Map must be a string.)
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
