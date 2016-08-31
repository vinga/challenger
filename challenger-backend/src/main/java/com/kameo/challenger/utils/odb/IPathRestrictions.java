package com.kameo.challenger.utils.odb;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;

public interface IPathRestrictions<E> {
	public Predicate apply(CriteriaBuilder cb, CriteriaQuery<?> cq, Path<E> root);

}