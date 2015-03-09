package com.appdirect.isv.backend.core.dao;

import java.io.Serializable;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.CriteriaSpecification;
import org.hibernate.criterion.DetachedCriteria;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

public class GenericHibernateDAOImpl<T, PK extends Serializable> implements GenericDAO<T, PK> {
	@Autowired
	private SessionFactory sessionFactory;

	private final Class<T> type;

	public GenericHibernateDAOImpl(Class<T> type) {
		this.type = type;
	}

	@Override
	@Transactional(propagation = Propagation.MANDATORY)
	public void saveOrUpdate(T entity) {
		sessionFactory.getCurrentSession().saveOrUpdate(entity);
	}

	@Override
	@Transactional(propagation = Propagation.MANDATORY)
	public void delete(T entity) {
		sessionFactory.getCurrentSession().delete(entity);
	}

	@Override
	@Transactional(propagation = Propagation.MANDATORY)
	public T findById(PK id) {
		@SuppressWarnings("unchecked")
		T result = (T) sessionFactory.getCurrentSession().load(type, id);
		return result;
	}

	@Override
	@Transactional(propagation = Propagation.MANDATORY)
	public List<T> findAll() {
		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(type);
		criteria.setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY);
		@SuppressWarnings("unchecked")
		List<T> result = criteria.list();
		return result;
	}

	@Override
	@Transactional(propagation = Propagation.MANDATORY)
	public <E> List<E> findByCriteria(Class<E> type, DetachedCriteria criteria) {
		return findByCriteria(type, criteria, 0, -1);
	}

	@Override
	@Transactional(propagation = Propagation.MANDATORY)
	public <E> List<E> findByCriteria(Class<E> type, DetachedCriteria criteria, int firstResult, int maxResults) {
		Criteria executableCriteria = criteria.getExecutableCriteria(sessionFactory.getCurrentSession());
		if (firstResult >= 0) {
			executableCriteria.setFirstResult(firstResult);
		}
		if (maxResults >= 0) {
			executableCriteria.setMaxResults(maxResults);
		}
		@SuppressWarnings("unchecked")
		List<E> result = executableCriteria.list();
		return result;
	}

	@Override
	@Transactional(propagation = Propagation.MANDATORY)
	public <E> E findUniqueByCriteria(Class<E> type, DetachedCriteria criteria) {
		List<E> results = findByCriteria(type, criteria);
		if (results.size() > 1) {
			throw new IllegalStateException("Criteria should only return one result.");
		}
		if (results.size() == 1) {
			return results.get(0);
		}
		return null;
	}
}