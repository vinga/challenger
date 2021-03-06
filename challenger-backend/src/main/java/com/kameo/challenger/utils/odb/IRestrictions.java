package com.kameo.challenger.utils.odb;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

public interface IRestrictions<E> {
	Predicate apply(CriteriaBuilder cb, CriteriaQuery<?> cq, Root<E> root);

}