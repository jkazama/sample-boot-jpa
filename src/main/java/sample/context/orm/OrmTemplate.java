package sample.context.orm;

import java.util.*;

import org.apache.commons.lang3.SerializationUtils;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.NonUniqueResultException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.springframework.orm.hibernate5.HibernateCallback;
import org.springframework.orm.hibernate5.HibernateTemplate;
import org.springframework.orm.hibernate5.SessionHolder;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import sample.ValidationException;
import sample.context.orm.Sort.SortOrder;

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

	/**
	 * 一件取得の処理をします。
	 */
	@SuppressWarnings("unchecked")
	public <T> Optional<T> get(final DetachedCriteria criteria) {
		return Optional.ofNullable(tmpl.executeWithNativeSession(new HibernateCallback<T>() {
			@Override
			public T doInHibernate(Session session) throws HibernateException {
				return (T) criteria.getExecutableCriteria(session).setCacheable(true).uniqueResult();
			}
		}));
	}

	/**
	 * 一件取得の処理をします。(存在しない時はValidationException)
	 */
	public <T> T load(final DetachedCriteria criteria) {
		try {
			Optional<T> v = get(criteria);
			return v.orElseThrow(() -> new ValidationException("error.EntityNotFoundException"));
		} catch (NonUniqueResultException e) {
			return null;
		}
	}

	/**
	 * Criteriaの検索をします。
	 */
	@SuppressWarnings("unchecked")
	public <T> List<T> find(final DetachedCriteria criteria) {
		return (List<T>) tmpl.findByCriteria(criteria);
	}

	/**
	 * Criteriaのページング検索をします。
	 */
	@SuppressWarnings("unchecked")
	public <T> PagingList<T> find(final DetachedCriteria criteria, final Pagination page) {
		return new PagingList<T>(tmpl.executeWithNativeSession(new HibernateCallback<List<T>>() {
			@Override
			public List<T> doInHibernate(Session session) throws HibernateException {
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
					for (SortOrder order : page.getSort().orders()) {
						executableCriteria.addOrder(
								order.isAscending() ? Order.asc(order.getProperty()) : Order.desc(order.getProperty()));
					}
				}
				return executableCriteria.list();
			}
		}), page);
	}

	/**
	 * 一件取得の処理をします。
	 */
	@SuppressWarnings("unchecked")
	public <T> Optional<T> get(final String hql, final Object... args) {
		return Optional.ofNullable(tmpl.executeWithNativeSession(new HibernateCallback<T>() {
			@Override
			public T doInHibernate(Session session) throws HibernateException {
				return (T) bindArgs(session.createQuery(hql).setCacheable(true), args).uniqueResult();
			}
		}));
	}

	/**
	 * 一件取得の処理をします。(存在しない時はValidationException)
	 */
	public <T> T load(final String hql, final Object... args) {
		try {
			Optional<T> v = get(hql, args);
			return v.orElseThrow(() -> new ValidationException("error.EntityNotFoundException"));
		} catch (NonUniqueResultException e) {
			return null;
		}
	}

	/**
	 * 対象Entityを全件取得します。
	 */
	public <T> List<T> loadAll(final Class<T> entityClass) {
		return tmpl.loadAll(entityClass);
	}

	/**
	 * HQLの検索を行います。
	 */
	@SuppressWarnings("unchecked")
	public <T> List<T> find(final String hql, final Object... args) {
		return tmpl.executeWithNativeSession(new HibernateCallback<List<T>>() {
			@Override
			public List<T> doInHibernate(Session session) throws HibernateException {
				return bindArgs(session.createQuery(hql), args).list();
			}
		});
	}

	/**
	 * HQLのページング検索を行います。
	 */
	@SuppressWarnings("unchecked")
	public <T> PagingList<T> find(final String hql, final Pagination page, final Object... args) {
		return new PagingList<T>(tmpl.executeWithNativeSession(new HibernateCallback<List<T>>() {
			@Override
			public List<T> doInHibernate(Session session) throws HibernateException {
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
				Query query = session.createQuery(hql);
				return bindArgs(query, page, args).list();
			}
		}), page);
	}

	/**
	 * 名前付きHQLで一件取得を行います。
	 */
	public <T> Optional<T> getNamed(final String queryName, final Object... args) {
		List<T> list = findNamed(queryName, args);
		if (list.isEmpty()) {
			return Optional.empty();
		}
		return Optional.of(list.get(0));
	}

	/**
	 * 名前付きHQLで一件取得を行います。
	 */
	public <T> T loadNamed(final String queryName, final Object... args) {
		Optional<T> v = getNamed(queryName, args);
		return v.orElseThrow(() -> new ValidationException("error.EntityNotFoundException"));
	}

	/**
	 * 名前付きHQLの検索を行います。
	 */
	@SuppressWarnings("unchecked")
	public <T> List<T> findNamed(final String queryName, final Object... args) {
		return (List<T>) tmpl.executeWithNativeSession(new HibernateCallback<List<?>>() {
			@Override
			public List<?> doInHibernate(Session session) throws HibernateException {
				Query query = session.getNamedQuery(queryName);
				return bindArgs(query, args).list();
			}
		});
	}

	/**
	 * 名前付きHQLのページング検索を行います。
	 */
	@SuppressWarnings("unchecked")
	public <T> PagingList<T> findNamed(final String queryName, final Pagination page, final Object... args) {
		return new PagingList<T>(tmpl.executeWithNativeSession(new HibernateCallback<List<T>>() {
			@Override
			public List<T> doInHibernate(Session session) throws HibernateException {
				Query query = session.getNamedQuery(queryName);
				String hql = query.getQueryString();
				String hqlCnt = "select count(*) " + hql.substring(hql.toLowerCase().indexOf("from"));
				long total = load(hqlCnt, args);
				page.setTotal(total);
				return bindArgs(query, page, args).list();
			}
		}), page);
	}

	/**
	 * SQL検索をします。
	 * @return 検索結果(selectの値配列一覧)
	 */
	@SuppressWarnings("unchecked")
	public <T> List<T> findBySql(final String sql, final Object... args) {
		return tmpl.executeWithNativeSession(new HibernateCallback<List<T>>() {
			@Override
			public List<T> doInHibernate(Session session) throws HibernateException {
				Query query = session.createSQLQuery(sql);
				return bindArgs(query, args).list();
			}
		});
	}

	/**
	 * SQLのページング検索をします。
	 * <p>常にPagination#ignoreTotalがtrueな状態となります。
	 * 件数が必要な時は別途カウント算出を行うようにしてください。
	 * @return 検索結果(selectの値配列一覧)
	 */
	@SuppressWarnings("unchecked")
	public <T> PagingList<T> findBySql(final String sql, final Pagination page, final Object... args) {
		return new PagingList<T>(tmpl.executeWithNativeSession(new HibernateCallback<List<T>>() {
			@Override
			public List<T> doInHibernate(Session session) throws HibernateException {
				return bindArgs(session.createSQLQuery(sql), page, args).list();
			}
		}), page);
	}

	/**
	 * HQL実行をします。
	 * @return 実行件数
	 */
	public int execute(final String hql, final Object... args) {
		return tmpl.executeWithNativeSession(new HibernateCallback<Integer>() {
			@Override
			public Integer doInHibernate(Session session) throws HibernateException {
				Query query = session.createQuery(hql);
				return bindArgs(query, args).executeUpdate();
			}
		});
	}

	/**
	 * SQL実行をします。
	 * @return 実行件数
	 */
	public int executeSql(final String sql, final Object... args) {
		return tmpl.executeWithNativeSession(new HibernateCallback<Integer>() {
			@Override
			public Integer doInHibernate(Session session) throws HibernateException {
				Query query = session.createSQLQuery(sql);
				return bindArgs(query, args).executeUpdate();
			}
		});
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