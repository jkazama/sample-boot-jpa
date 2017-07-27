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
 * ORM の可変条件を取り扱う CriteriaBuilder ラッパー。
 * <p>Criteria の簡易的な取り扱いを可能にします。
 * <p>Criteria で利用する条件句は必要に応じて追加してください。
 * <p>ビルド結果としての CriteriaQuery は result* メソッドで受け取って下さい。
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

    /** 指定したEntityクラスにエイリアスを紐付けたCriteriaを生成します。 */
    private OrmCriteria(EntityManager em, Class<T> clazz, String alias) {
        this.clazz = clazz;
        this.metamodel = em.getMetamodel();
        this.builder = em.getCriteriaBuilder();
        this.query = builder.createQuery(clazz);
        this.root = query.from(clazz);
        this.alias = alias;
        this.root.alias(alias);
    }

    /** 内部に保有するエンティティクラスを返します。 */
    public Class<T> entityClass() {
        return clazz;
    }
    
    /** エンティティのメタ情報を返します。 */
    public Metamodel metamodel() {
        return metamodel;
    }
    
    /** 内部に保有する　CriteriaBuilder を返します。 */
    public CriteriaBuilder builder() {
        return builder;
    }
    
    /** 内部に保有する Root を返します。 */
    public Root<T> root() {
        return root;
    }
    
    /**
     * 関連付けを行います。
     * <p>引数にはJoin可能なフィールド(@ManyToOne 等)を指定してください。
     * <p>Join した要素は呼び出し元で保持して必要に応じて利用してください。
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
     * 組み上げた CriteriaQuery を返します。
     * <p>複雑なクエリや集計関数は本メソッドで返却された query を元に追加構築してください。
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
     * 条件句 ( or 条件含む ) を追加します。
     * <p>引数には CriteriaBuilder で生成した Predicate を追加してください。
     */
    public OrmCriteria<T> add(final Predicate predicate) {
        this.predicates.add(predicate);
        return this;
    }

    /** or 条件を付与します。 */
    public OrmCriteria<T> or(final Predicate... predicates) {
        if (predicates.length != 0) {
            add(builder.or(predicates));
        }
        return this;
    }

    /** null 一致条件を付与します。 */
    public OrmCriteria<T> isNull(String field) {
        return add(builder.isNull(root.get(field)));
    }

    /** null 不一致条件を付与します。 */
    public OrmCriteria<T> isNotNull(String field) {
        return add(builder.isNotNull(root.get(field)));
    }

    /** 一致条件を付与します。( 値が null の時は無視されます ) */
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

    /** 不一致条件を付与します。(値がnullの時は無視されます) */
    public OrmCriteria<T> equalNot(String field, final Object value) {
        if (isValid(value)) {
            add(builder.notEqual(root.get(field), value));
        }
        return this;
    }

    /** 一致条件を付与します。(値がnullの時は無視されます) */
    public OrmCriteria<T> equalProp(String field, final String fieldOther) {
        add(builder.equal(root.get(field), root.get(fieldOther)));
        return this;
    }

    /** like条件を付与します。(値がnullの時は無視されます) */
    public OrmCriteria<T> like(String field, String value, MatchMode mode) {
        if (isValid(value)) {
            add(builder.like(root.get(field), mode.toMatchString(value)));
        }
        return this;
    }

    /** like条件を付与します。[複数フィールドに対するOR結合](値がnullの時は無視されます) */
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

    /** in条件を付与します。 */
    public OrmCriteria<T> in(String field, final Object[] values) {
        if (values != null && 0 < values.length) {
            add(root.get(field).in(values));
        }
        return this;
    }

    /** between条件を付与します。 */
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

    /** between条件を付与します。 */
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

    /** between条件を付与します。 */
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

    /** between条件を付与します。 */
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

    /** [フィールド]&gt;=[値] 条件を付与します。(値がnullの時は無視されます) */
    public <Y extends Comparable<? super Y>> OrmCriteria<T> gte(String field, final Y value) {
        if (isValid(value)) {
            add(builder.greaterThanOrEqualTo(root.get(field), value));
        }
        return this;
    }

    /** [フィールド]&gt;[値] 条件を付与します。(値がnullの時は無視されます) */
    public <Y extends Comparable<? super Y>> OrmCriteria<T> gt(String field, final Y value) {
        if (isValid(value)) {
            add(builder.greaterThan(root.get(field), value));
        }
        return this;
    }

    /** [フィールド]&lt;=[値] 条件を付与します。 */
    public <Y extends Comparable<? super Y>> OrmCriteria<T> lte(String field, final Y value) {
        if (isValid(value)) {
            add(builder.lessThanOrEqualTo(root.get(field), value));
        }
        return this;
    }

    /** [フィールド]&lt;[値] 条件を付与します。 */
    public <Y extends Comparable<? super Y>>  OrmCriteria<T> lt(String field, final Y value) {
        if (isValid(value)) {
            add(builder.lessThan(root.get(field), value));
        }
        return this;
    }

    /** ソート条件を加えます。 */
    public OrmCriteria<T> sort(Sort sort) {
        sort.getOrders().forEach(this::sort);
        return this;
    }
    
    /** ソート条件を加えます。 */
    public OrmCriteria<T> sort(SortOrder order) {
        if (order.isAscending()) {
            sort(order.getProperty());
        } else {
            sortDesc(order.getProperty());
        }
        return this;
    }
    
    /** 昇順条件を加えます。 */
    public OrmCriteria<T> sort(String field) {
        orders.add(builder.asc(root.get(field)));
        return this;
    }

    /** 降順条件を加えます。 */
    public OrmCriteria<T> sortDesc(String field) {
        orders.add(builder.desc(root.get(field)));
        return this;
    }
    
    public boolean emptySort() {
        return !orders.isEmpty();
    }
    
    /** 指定した Entity クラスを軸にしたCriteriaを生成します。 */    
    public static <T> OrmCriteria<T> of(EntityManager em, Class<T> clazz) {
        return new OrmCriteria<>(em, clazz, DefaultAlias);
    }

    /** 指定した Entity クラスにエイリアスを紐付けたCriteriaを生成します。 */
    public static <T> OrmCriteria<T> of(EntityManager em, Class<T> clazz, String alias) {
        return new OrmCriteria<>(em, clazz, alias);
    }
    
}
