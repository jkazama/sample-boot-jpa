package sample.context.orm;

import java.util.*;
import java.util.function.Function;

import org.apache.commons.lang3.SerializationUtils;
import org.hibernate.*;
import org.hibernate.criterion.*;
import org.springframework.orm.hibernate5.*;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import sample.ValidationException;
import sample.ValidationException.ErrorKeys;

/**
 * HibernateのRepositoryに対する簡易アクセサ。
 * セッション毎に生成して利用してください。
 * <p>HibernateTemplateのメソッドで利用したい処理があれば
 * 必要に応じてラップメソッドを追加してください。
 */
public class OrmTemplate {

	private HibernateTemplate tmpl;

	public OrmTemplate(SessionFactory sessionFactory) {
		tmpl = new HibernateTemplate(sessionFactory);
	}

	public HibernateTemplate origin() {
		return tmpl;
	}

	/** Criteriaで一件取得します。 */
	@SuppressWarnings("unchecked")
	public <T> Optional<T> get(final DetachedCriteria criteria) {
		return Optional.ofNullable(tmpl.executeWithNativeSession((session) ->
				(T) criteria.getExecutableCriteria(session).setCacheable(true).uniqueResult()));
	}

	/** Criteriaで一件取得します。(存在しない時はValidationException) */
	public <T> T load(final DetachedCriteria criteria) {
		try {
			Optional<T> v = get(criteria);
			return v.orElseThrow(() -> new ValidationException(ErrorKeys.EntityNotFound));
		} catch (NonUniqueResultException e) {
			return null;
		}
	}

	/**
	 * Criteriaで検索します。
	 * ※ランダムな条件検索等、可変条件検索が必要となる時に利用して下さい
	 */
	@SuppressWarnings("unchecked")
	public <T> List<T> find(final DetachedCriteria criteria) {
		return (List<T>) tmpl.findByCriteria(criteria);
	}

	/** Criteriaでページング検索します。 */
	@SuppressWarnings("unchecked")
	public <T> PagingList<T> find(final DetachedCriteria criteria, final Pagination page) {
		return new PagingList<T>(tmpl.executeWithNativeSession((session) -> {
			long total = -1L;
			if (!page.isIgnoreTotal()) {
				total = load(SerializationUtils.clone(criteria).setProjection(Projections.rowCount()));
			}
			page.setTotal(total);
			Criteria executableCriteria = criteria.getExecutableCriteria(session);
			prepareCriteria(executableCriteria);
			if (page != null) {
				if (page.getPage() > 0) {
					executableCriteria.setFirstResult(page.getFirstResult());
				}
				if (page.getSize() > 0) {
					executableCriteria.setMaxResults(page.getSize());
				}
				page.getSort().orders().forEach((order) ->
					executableCriteria.addOrder(
						order.isAscending() ? Order.asc(order.getProperty()) : Order.desc(order.getProperty())));
			}
			return executableCriteria.list();
		}), page);
	}

	/**
	 * Criteriaで一件取得します。
	 * <p>クロージャ戻り値は引数に取るOrmCriteriaのresult*の実行結果を返すようにしてください。
	 */
	public <T> Optional<T> get(Class<T> entityClass, Function<OrmCriteria<T>, DetachedCriteria> func) {
		return get(func.apply(new OrmCriteria<>(entityClass)));
	}
	
	public <T> Optional<T> get(Class<T> entityClass, String alias, Function<OrmCriteria<T>, DetachedCriteria> func) {
		return get(func.apply(new OrmCriteria<>(entityClass, alias)));
	}

	/**
	 * Criteriaで一件取得します。(存在しない時はValidationException)
	 * <p>クロージャ戻り値は引数に取るOrmCriteriaのresult*の実行結果を返すようにしてください。
	 */
	public <T> Optional<T> load(Class<T> entityClass, Function<OrmCriteria<T>, DetachedCriteria> func) {
		return load(func.apply(new OrmCriteria<>(entityClass)));
	}
	
	public <T> Optional<T> load(Class<T> entityClass, String alias, Function<OrmCriteria<T>, DetachedCriteria> func) {
		return load(func.apply(new OrmCriteria<>(entityClass, alias)));
	}
	
	/**
	 * Criteriaで検索します。
	 * <p>クロージャ戻り値は引数に取るOrmCriteriaのresult*の実行結果を返すようにしてください。
	 */
	public <T> List<T> find(Class<T> entityClass, Function<OrmCriteria<T>, DetachedCriteria> func) {
		return find(func.apply(new OrmCriteria<>(entityClass)));
	}
	
	public <T> List<T> find(Class<T> entityClass, String alias, Function<OrmCriteria<T>, DetachedCriteria> func) {
		return find(func.apply(new OrmCriteria<>(entityClass, alias)));
	}

	/**
	 * Criteriaでページング検索します。
	 * <p>クロージャ戻り値は引数に取るOrmCriteriaのresult*の実行結果を返すようにしてください。
	 */
	public <T> PagingList<T> find(Class<T> entityClass, final Pagination page, Function<OrmCriteria<T>, DetachedCriteria> func) {
		return find(func.apply(new OrmCriteria<>(entityClass)), page);
	}
	
	public <T> PagingList<T> find(Class<T> entityClass, String alias, final Pagination page, Function<OrmCriteria<T>, DetachedCriteria> func) {
		return find(func.apply(new OrmCriteria<>(entityClass, alias)), page);
	}
	
	/** HQLで一件取得します。*/
	@SuppressWarnings("unchecked")
	public <T> Optional<T> get(final String hql, final Object... args) {
		return Optional.ofNullable(tmpl.executeWithNativeSession((session) ->
				(T) bindArgs(session.createQuery(hql).setCacheable(true), args).uniqueResult()));
	}

	/** HQLで一件取得します。(存在しない時はValidationException) */
	public <T> T load(final String hql, final Object... args) {
		try {
			Optional<T> v = get(hql, args);
			return v.orElseThrow(() -> new ValidationException(ErrorKeys.EntityNotFound));
		} catch (NonUniqueResultException e) {
			return null;
		}
	}

	/** 対象Entityを全件取得します。*/
	public <T> List<T> loadAll(final Class<T> entityClass) {
		return tmpl.loadAll(entityClass);
	}

	/** HQLで検索します。 */
	@SuppressWarnings("unchecked")
	public <T> List<T> find(final String hql, final Object... args) {
		return tmpl.executeWithNativeSession((session) ->
				bindArgs(session.createQuery(hql), args).list());
	}

	/**
	 * HQLでページング検索します。
	 * <p>カウント句がうまく構築されない時はPagination#ignoreTotalをtrueにして、
	 * 別途通常の検索でトータル件数を算出するようにして下さい。
	 */
	@SuppressWarnings("unchecked")
	public <T> PagingList<T> find(final String hql, final Pagination page, final Object... args) {
		return new PagingList<T>(tmpl.executeWithNativeSession((session) -> {
			if (page.isIgnoreTotal()) {
				page.setTotal(-1L);
			} else {
				String hqlCnt = "select count(*) " + hql.substring(hql.toLowerCase().indexOf("from"));
				int orderPos = hqlCnt.indexOf("order");
				if (0 <= orderPos) {
					hqlCnt = hqlCnt.substring(0, orderPos);
				}
				long total = load(hqlCnt, args);
				page.setTotal(total);
			}
			return bindArgs(session.createQuery(hql), page, args).list();
		}), page);
	}

	/** 名前付きHQLで一件取得します。 */
	public <T> Optional<T> getNamed(final String queryName, final Object... args) {
		List<T> list = findNamed(queryName, args);
		if (list.isEmpty()) {
			return Optional.empty();
		}
		return Optional.of(list.get(0));
	}

	/** 名前付きHQLで一件取得をします。(存在しない時はValidationException) */
	public <T> T loadNamed(final String queryName, final Object... args) {
		Optional<T> v = getNamed(queryName, args);
		return v.orElseThrow(() -> new ValidationException(ErrorKeys.EntityNotFound));
	}

	/** 名前付きHQLで検索します。 */
	@SuppressWarnings("unchecked")
	public <T> List<T> findNamed(final String queryName, final Object... args) {
		return (List<T>) tmpl.executeWithNativeSession((session) ->
				bindArgs(session.getNamedQuery(queryName), args).list());
	}

	/** 
	 * 名前付きHQLでページング検索します。
	 * <p>カウント句がうまく構築されない時はPagination#ignoreTotalをtrueにして、
	 * 別途通常の検索でトータル件数を算出するようにして下さい。
	 */
	@SuppressWarnings("unchecked")
	public <T> PagingList<T> findNamed(final String queryName, final Pagination page, final Object... args) {
		return new PagingList<T>(tmpl.executeWithNativeSession((session) -> {
			Query query = session.getNamedQuery(queryName);
			String hql = query.getQueryString();
			String hqlCnt = "select count(*) " + hql.substring(hql.toLowerCase().indexOf("from"));
			long total = load(hqlCnt, args);
			page.setTotal(total);
			return bindArgs(query, page, args).list();
		}), page);
	}

	/**
	 * SQLで検索します。
	 * <p>検索結果としてselectの値配列一覧が返されます。
	 */
	@SuppressWarnings("unchecked")
	public <T> List<T> findBySql(final String sql, final Object... args) {
		return tmpl.executeWithNativeSession((session) ->
				bindArgs(session.createSQLQuery(sql), args).list());
	}

	/**
	 * SQLでページング検索します。
	 * <p>常にPagination#ignoreTotalがtrueな状態となります。
	 * 件数が必要な時は別途カウント算出を行うようにしてください。
	 * <p>検索結果としてselectの値配列一覧が返されます。
	 */
	@SuppressWarnings("unchecked")
	public <T> PagingList<T> findBySql(final String sql, final Pagination page, final Object... args) {
		return new PagingList<T>(tmpl.executeWithNativeSession((session) ->
				bindArgs(session.createSQLQuery(sql), page, args).list()), page);
	}

	/** HQLを実行します。 */
	public int execute(final String hql, final Object... args) {
		return tmpl.executeWithNativeSession((session) ->
				bindArgs(session.createQuery(hql), args).executeUpdate());
	}

	/** SQLを実行をします。*/
	public int executeSql(final String sql, final Object... args) {
		return tmpl.executeWithNativeSession((session) ->
				bindArgs(session.createSQLQuery(sql), args).executeUpdate());
	}

	protected Query bindArgs(final Query query, final Object... args) {
		return bindArgs(query, null, args);
	}

	protected Query bindArgs(final Query query, final Pagination page, final Object... args) {
		prepareQuery(query);
		if (page != null && page.getPage() > 0) {
			query.setFirstResult(page.getFirstResult());
		}
		if (page != null && page.getSize() > 0) {
			query.setMaxResults(page.getSize());
		}
		if (args != null) {
			for (int i = 0; i < args.length; i++) {
				bindParameter(query, String.valueOf(i + 1), args[i]);
			}
		}
		SessionHolder sessionHolder = (SessionHolder) TransactionSynchronizationManager
				.getResource(tmpl.getSessionFactory());
		if (sessionHolder != null && sessionHolder.hasTimeout()) {
			query.setTimeout(sessionHolder.getTimeToLiveInSeconds());
		}
		return query;
	}
	
	protected void bindParameter(final Query query, String name, final Object value) {
		if (value instanceof Collection) {
			query.setParameterList(name, (Collection<?>) value);
		} else if (value instanceof Object[]) {
			query.setParameterList(name, (Object[]) value);
		} else {
			query.setParameter(name, value);
		}		
	}

	// from HibernateTemplate

	protected void prepareCriteria(Criteria criteria) {
		if (tmpl.isCacheQueries()) {
			criteria.setCacheable(true);
			if (tmpl.getQueryCacheRegion() != null) {
				criteria.setCacheRegion(tmpl.getQueryCacheRegion());
			}
		}
		if (tmpl.getFetchSize() > 0) {
			criteria.setFetchSize(tmpl.getFetchSize());
		}
		if (tmpl.getMaxResults() > 0) {
			criteria.setMaxResults(tmpl.getMaxResults());
		}

		SessionHolder sessionHolder = (SessionHolder) TransactionSynchronizationManager
				.getResource(tmpl.getSessionFactory());
		if (sessionHolder != null && sessionHolder.hasTimeout()) {
			criteria.setTimeout(sessionHolder.getTimeToLiveInSeconds());
		}
	}

	protected void prepareQuery(Query queryObject) {
		if (tmpl.isCacheQueries()) {
			queryObject.setCacheable(true);
			if (tmpl.getQueryCacheRegion() != null) {
				queryObject.setCacheRegion(tmpl.getQueryCacheRegion());
			}
		}
		if (tmpl.getFetchSize() > 0) {
			queryObject.setFetchSize(tmpl.getFetchSize());
		}
		if (tmpl.getMaxResults() > 0) {
			queryObject.setMaxResults(tmpl.getMaxResults());
		}

		SessionHolder sessionHolder = (SessionHolder) TransactionSynchronizationManager
				.getResource(tmpl.getSessionFactory());
		if (sessionHolder != null && sessionHolder.hasTimeout()) {
			queryObject.setTimeout(sessionHolder.getTimeToLiveInSeconds());
		}
	}

}