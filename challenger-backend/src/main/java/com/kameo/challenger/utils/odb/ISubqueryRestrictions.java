package com.kameo.challenger.utils.odb;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;

public interface ISubqueryRestrictions<E> {
	public Predicate apply(CriteriaBuilder cb, Subquery<?> cq, Root<E> root);  

}