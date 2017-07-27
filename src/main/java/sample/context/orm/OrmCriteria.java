package sample.context.orm;

import java.time.*;
import java.util.*;
import java.util.function.Function;

import javax.persistence.EntityManager;
import javax.persistence.criteria.*;
import javax.persistence.metamodel.Metamodel;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.criterion.MatchMode;

import sample.context.orm.Sort.SortOrder;

/**
 * The CriteriaBuilder wrapper who handles a variable condition of ORM.
 * <p>Enable the simple handling of Criteria.
 * <p>Add the condition phrase to use in Criteria as needed.
 * <p>Receive CriteriaQuery as the build result by result* method.
 */
public class OrmCriteria<T> {
    
    public static final String DefaultAlias = "m";

    private final Class<T> clazz;
    private final String alias;
    private final Metamodel metamodel;
    private final CriteriaBuilder builder;
    private final CriteriaQuery<T> query;
    private final Root<T> root;
    private final Set<Predicate> predicates = new LinkedHashSet<>();
    private final Set<Order> orders = new LinkedHashSet<>();

    /** Create Criteria which connected alias with the Entity class. */
    private OrmCriteria(EntityManager em, Class<T> clazz, String alias) {
        this.clazz = clazz;
        this.metamodel = em.getMetamodel();
        this.builder = em.getCriteriaBuilder();
        this.query = builder.createQuery(clazz);
        this.root = query.from(clazz);
        this.alias = alias;
        this.root.alias(alias);
    }

    public Class<T> entityClass() {
        return clazz;
    }
    
    public Metamodel metamodel() {
        return metamodel;
    }
    
    public CriteriaBuilder builder() {
        return builder;
    }
    
    public Root<T> root() {
        return root;
    }
    
    /**
     * Target Field (@ManyToOne) who is available for Join in an argument.
     * <p>The element which did Join maintains it in an origin of summons, and please use it as needed.
     */
    public <Y> Join<T, Y> join(String associationPath) {
        return root.join(associationPath);
    }
    
    public <Y> Join<T, Y> join(String associationPath, String alias) {
        Join<T, Y> v = join(associationPath);
        v.alias(alias);
        return v;
    }
    
    /**
     * Return built CriteriaQuery..
     * <p>A complicated query and aggregate function are these methods,
     *  and please build returned query for the cause more.
     */
    public CriteriaQuery<T> result() {
        return result(q -> q);
    }
    @SuppressWarnings("unchecked")
    public CriteriaQuery<T> result(Function<CriteriaQuery<?>, CriteriaQuery<?>> extension) {
        CriteriaQuery<T> q = query.where(predicates.toArray(new Predicate[0]));
        q = (CriteriaQuery<T>)extension.apply(q);
        return orders.isEmpty() ? q : q.orderBy(orders.toArray(new Order[0]));
    }
    
    public CriteriaQuery<Long> resultCount() {
        return resultCount(q -> q);
    }
    @SuppressWarnings("unchecked")
    public CriteriaQuery<Long> resultCount(Function<CriteriaQuery<?>, CriteriaQuery<?>> extension) {
        CriteriaQuery<Long> q = builder.createQuery(Long.class);
        q.from(clazz).alias(alias);
        q.where(predicates.toArray(new Predicate[0]));
        if (q.isDistinct()) {
            q.select(builder.countDistinct(root));
        } else {
            q.select(builder.count(root));
        }
        return (CriteriaQuery<Long>)extension.apply(q);
    }

    /**
     * Add a condition phrase.
     * <p>Add Predicate formed in CriteriaBuilder to an argument.
     */
    public OrmCriteria<T> add(final Predicate predicate) {
        this.predicates.add(predicate);
        return this;
    }

    /** Add any OR condition phrase. */
    public OrmCriteria<T> or(final Predicate... predicates) {
        if (predicates.length != 0) {
            add(builder.or(predicates));
        }
        return this;
    }

    /** Add isNull condition. */
    public OrmCriteria<T> isNull(String field) {
        return add(builder.isNull(root.get(field)));
    }

    /** Add isNotNull condition. */
    public OrmCriteria<T> isNotNull(String field) {
        return add(builder.isNotNull(root.get(field)));
    }

    /** Add an "equal" condition. (a value is ignored at the time of null) */
    public OrmCriteria<T> equal(String field, final Object value) {
        return equal(root, field, value);
    }
    
    public OrmCriteria<T> equal(Path<?> path, String field, final Object value) {
        if (isValid(value)) {
            add(builder.equal(path.get(field), value));
        }
        return this;
    }

    private boolean isValid(final Object value) {
        if (value instanceof String) {
            return StringUtils.isNotBlank((String) value);
        } else if (value instanceof Optional) {
            return ((Optional<?>) value).isPresent();
        } else {
            return value != null;
        }
    }

    /** Add an "equalNot" condition. (a value is ignored at the time of null) */
    public OrmCriteria<T> equalNot(String field, final Object value) {
        if (isValid(value)) {
            add(builder.notEqual(root.get(field), value));
        }
        return this;
    }

    /** Add an "equalProp" condition. (a value is ignored at the time of null) */
    public OrmCriteria<T> equalProp(String field, final String fieldOther) {
        add(builder.equal(root.get(field), root.get(fieldOther)));
        return this;
    }

    /** Add an "like" condition. (a value is ignored at the time of null) */
    public OrmCriteria<T> like(String field, String value, MatchMode mode) {
        if (isValid(value)) {
            add(builder.like(root.get(field), mode.toMatchString(value)));
        }
        return this;
    }

    /** Add an "like" condition.[OR combination for plural fields] (a value is ignored at the time of null) */
    public OrmCriteria<T> like(String[] fields, String value, MatchMode mode) {
        if (isValid(value)) {
            Predicate[] predicates = new Predicate[fields.length];
            for (int i = 0; i < fields.length; i++) {
                predicates[i] = builder.like(root.get(fields[i]), mode.toMatchString(value));
            }
            add(builder.or(predicates));
        }
        return this;
    }

    /** Add an "in" condition. (a value is ignored at the time of null) */
    public OrmCriteria<T> in(String field, final Object[] values) {
        if (values != null && 0 < values.length) {
            add(root.get(field).in(values));
        }
        return this;
    }

    /** Add an "between" condition. (a value is ignored at the time of null) */
    public OrmCriteria<T> between(String field, final Date from, final Date to) {
        if (from != null && to != null) {
            predicates.add(builder.between(root.get(field), from, to));
        } else if (from != null) {
            gte(field, from);
        } else if (to != null) {
            lte(field, to);
        }
        return this;
    }
    
    /** Add an "between" condition. (a value is ignored at the time of null) */
    public OrmCriteria<T> between(String field, final LocalDate from, final LocalDate to) {
        if (from != null && to != null) {
            predicates.add(builder.between(root.get(field), from, to));
        } else if (from != null) {
            gte(field, from);
        } else if (to != null) {
            lte(field, to);
        }
        return this;
    }
    
    /** Add an "between" condition. (a value is ignored at the time of null) */
    public OrmCriteria<T> between(String field, final LocalDateTime from, final LocalDateTime to) {
        if (from != null && to != null) {
            predicates.add(builder.between(root.get(field), from, to));
        } else if (from != null) {
            gte(field, from);
        } else if (to != null) {
            lte(field, to);
        }
        return this;
    }

    /** Add an "between" condition. (a value is ignored at the time of null) */
    public OrmCriteria<T> between(String field, final String from, final String to) {
        if (isValid(from) && isValid(to)) {
            predicates.add(builder.between(root.get(field), from, to));
        } else if (isValid(from)) {
            gte(field, from);
        } else if (isValid(to)) {
            lte(field, to);
        }
        return this;
    }

    /** Add an "field&gt;=value" condition. (a value is ignored at the time of null) */
    public <Y extends Comparable<? super Y>> OrmCriteria<T> gte(String field, final Y value) {
        if (isValid(value)) {
            add(builder.greaterThanOrEqualTo(root.get(field), value));
        }
        return this;
    }

    /** Add an "field&gt;value" condition. (a value is ignored at the time of null) */
    public <Y extends Comparable<? super Y>> OrmCriteria<T> gt(String field, final Y value) {
        if (isValid(value)) {
            add(builder.greaterThan(root.get(field), value));
        }
        return this;
    }

    /** Add an "field&lt;=value" condition. (a value is ignored at the time of null) */
    public <Y extends Comparable<? super Y>> OrmCriteria<T> lte(String field, final Y value) {
        if (isValid(value)) {
            add(builder.lessThanOrEqualTo(root.get(field), value));
        }
        return this;
    }

    /** Add an "field&gt;value" condition. (a value is ignored at the time of null) */
    public <Y extends Comparable<? super Y>>  OrmCriteria<T> lt(String field, final Y value) {
        if (isValid(value)) {
            add(builder.lessThan(root.get(field), value));
        }
        return this;
    }

    /** Add a sort. */
    public OrmCriteria<T> sort(Sort sort) {
        sort.getOrders().forEach(this::sort);
        return this;
    }
    
    /** Add a sort. */
    public OrmCriteria<T> sort(SortOrder order) {
        if (order.isAscending()) {
            sort(order.getProperty());
        } else {
            sortDesc(order.getProperty());
        }
        return this;
    }
    
    /** Add a asc sort. */
    public OrmCriteria<T> sort(String field) {
        orders.add(builder.asc(root.get(field)));
        return this;
    }

    /** Add a desc sort. */
    public OrmCriteria<T> sortDesc(String field) {
        orders.add(builder.desc(root.get(field)));
        return this;
    }
    
    public boolean emptySort() {
        return !orders.isEmpty();
    }
    
    /** Create Criteria centering on the Entity class. */    
    public static <T> OrmCriteria<T> of(EntityManager em, Class<T> clazz) {
        return new OrmCriteria<>(em, clazz, DefaultAlias);
    }

    /** Create Criteria centering on the Entity class. */
    public static <T> OrmCriteria<T> of(EntityManager em, Class<T> clazz, String alias) {
        return new OrmCriteria<>(em, clazz, alias);
    }
    
}
