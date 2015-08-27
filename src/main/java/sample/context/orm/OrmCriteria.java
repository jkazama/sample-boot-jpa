package sample.context.orm;

import java.util.Date;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.Criteria;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projection;
import org.hibernate.criterion.Restrictions;
import org.hibernate.sql.JoinType;
import org.hibernate.transform.ResultTransformer;

import lombok.Getter;
import sample.context.Entity;

/**
 * ORMのCriteriaBuilderラッパー。
 * <p>Criteriaの簡易的な取り扱いを可能にします。
 * <p>Criteriaで利用する条件句は必要に応じて追加してください。
 */
@Getter
public class OrmCriteria<T extends Entity> {

	private final DetachedCriteria criteria;

	public OrmCriteria(Class<T> clazz) {
		this.criteria = DetachedCriteria.forClass(clazz);
	}

	public OrmCriteria(Class<T> clazz, String alias) {
		this.criteria = DetachedCriteria.forClass(clazz, alias);
	}

	public DetachedCriteria result() {
		return criteria;
	}

	public DetachedCriteria resultJoinToMap() {
		return result(Criteria.ALIAS_TO_ENTITY_MAP);
	}

	public DetachedCriteria resultJoin() {
		return result(Criteria.PROJECTION);
	}

	public DetachedCriteria resultJoinDistinct() {
		return result(Criteria.DISTINCT_ROOT_ENTITY);
	}

	public DetachedCriteria result(final ResultTransformer transformer) {
		return criteria.setResultTransformer(transformer);
	}

	/**
	 * 関連条件を追加します。
	 * <p>引数にはJoin可能なフィールド(@ManyToOne 等)を指定してください。
	 */
	public OrmCriteria<T> join(String associationPath, String alias) {
		this.criteria.createAlias(associationPath, alias);
		return this;
	}

	public OrmCriteria<T> join(String associationPath, String alias,
			JoinType joinType) {
		this.criteria.createAlias(associationPath, alias, joinType);
		return this;
	}

	/**
	 * Projection(max/min 等)の条件句を設定します。
	 * <p>引数にはProjectionsで生成したProjectionを追加してください。
	 */
	public OrmCriteria<T> projection(final Projection projection) {
		this.criteria.setProjection(projection);
		return this;
	}

	/**
	 * 条件句(OR結合含む)を追加します。
	 * <p>引数にはRestrictionsで生成したCriterionを追加してください。
	 */
	public OrmCriteria<T> add(final Criterion criterion) {
		this.criteria.add(criterion);
		return this;
	}

	/** null一致条件を付与します。 */
	public OrmCriteria<T> isNull(String field) {
		return add(Restrictions.isNull(field));
	}

	/** null不一致条件を付与します。 */
	public OrmCriteria<T> isNotNull(String field) {
		return add(Restrictions.isNotNull(field));
	}

	/** 一致条件を付与します。 */
	public OrmCriteria<T> equal(String field, final Object value) {
		if (isValid(value)) {
			add(Restrictions.eq(field, value));
		}
		return this;
	}
	private boolean isValid(final Object value) {
		if (value instanceof String) {
			return StringUtils.isNotBlank((String)value);
		} else {
			return value != null;
		}
	}

	/** 不一致条件を付与します。 */
	public OrmCriteria<T> equalNot(String field, final Object value) {
		if (isValid(value)) {
			add(Restrictions.ne(field, value));
		}
		return this;
	}

	/** 一致条件を付与します。 */
	public OrmCriteria<T> equalProp(String field, final String fieldOther) {
		add(Restrictions.eqProperty(field, fieldOther));
		return this;
	}

	/** like条件を付与します。 */
	public OrmCriteria<T> like(String field, String value, MatchMode mode) {
		if (isValid(value)) {
			add(Restrictions.like(field, value, mode));
		}
		return this;
	}

	/** like条件を付与します。[複数フィールドに対するOR結合] */
	public OrmCriteria<T> like(String[] fields, String value, MatchMode mode) {
		if (isValid(value)) {
			Criterion[] predicates = new Criterion[fields.length];
			for (int i = 0; i < fields.length; i++) {
				predicates[i] = Restrictions.like(fields[i], value, mode);
			}
			add(Restrictions.or(predicates));
		}
		return this;
	}

	/**
	 * or条件を付与します。
	 */
	public OrmCriteria<T> or(Criterion[] predicates) {
		if (predicates.length != 0) {
			add(Restrictions.or(predicates));
		}
		return this;
	}

	/** in条件を付与します。 */
	public OrmCriteria<T> in(String field, final Object[] values) {
		if (values != null && 0 < values.length) {
			add(Restrictions.in(field, values));
		}
		return this;
	}

	/** between条件を付与します。 */
	public OrmCriteria<T> between(String field, final Date from, final Date to) {
		if (from != null && to != null) {
			add(Restrictions.between(field, from, to));
		}
		return this;
	}

	/** between条件を付与します。 */
	public OrmCriteria<T> between(String field, final String from, final String to) {
		if (isValid(from) && isValid(to)) {
			add(Restrictions.between(field, from, to));
		}
		return this;
	}

	/** "greater than or equal"【[フィールド]&gt;=[値]】条件を付与します。 */
	public OrmCriteria<T> gte(String field, final Object value) {
		if (isValid(value)) {
			add(Restrictions.ge(field, value));
		}
		return this;
	}

	/** "greater than"【[フィールド]&gt;[値]】条件を付与します。 */
	public OrmCriteria<T> gt(String field, final Object value) {
		if (isValid(value)) {
			add(Restrictions.gt(field, value));
		}
		return this;
	}

	/** "less than or equal"【[フィールド]&lt;=[値]】条件を付与します。 */
	public OrmCriteria<T> lte(String field, final Object value) {
		if (isValid(value)) {
			add(Restrictions.le(field, value));
		}
		return this;
	}

	/** "less than"【[フィールド]&lt;[値]】条件を付与します。 */
	public OrmCriteria<T> lt(String field, final Object value) {
		if (isValid(value)) {
			add(Restrictions.lt(field, value));
		}
		return this;
	}

	/** 昇順条件を加えます。 */
	public OrmCriteria<T> sort(String field) {
		criteria.addOrder(Order.asc(field));
		return this;
	}

	/** 降順条件を加えます。 */
	public OrmCriteria<T> sortDesc(String field) {
		criteria.addOrder(Order.desc(field));
		return this;
	}

}
