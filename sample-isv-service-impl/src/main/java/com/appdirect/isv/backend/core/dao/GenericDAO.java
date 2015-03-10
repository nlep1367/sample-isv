package com.appdirect.isv.backend.core.dao;

import java.io.Serializable;
import java.util.List;

import org.hibernate.criterion.DetachedCriteria;

public interface GenericDAO<T, PK extends Serializable> {
	public void saveOrUpdate(T entity);

	public void delete(T entity);

	public T findById(PK id);

	public List<T> findAll();

	public <E> List<E> findByCriteria(Class<E> type, DetachedCriteria criteria);

	public <E> List<E> findByCriteria(Class<E> type, DetachedCriteria criteria, int firstResult, int maxResults);

	public <E> E findUniqueByCriteria(Class<E> type, DetachedCriteria criteria);
}
