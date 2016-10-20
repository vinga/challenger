package com.kameo.challenger.utils.odb.newapi

import javax.persistence.criteria.CriteriaBuilder
import javax.persistence.criteria.From
import javax.persistence.criteria.Join
import javax.persistence.criteria.Predicate
import kotlin.reflect.KMutableProperty1


class JoinWrap<E> constructor(cb: CriteriaBuilder,
                              root: Join<Any, E>,
                              arr: MutableList<() -> Predicate?>,
                              parent: PathWrap<E>?=null)

                              : PathWrap<E>(cb, root, arr, parent) {


    @Suppress("UNCHECKED_CAST")
    fun <F> join(sa: KMutableProperty1<E, F>): JoinWrap<F> {
        val join=(root as Join<Any,E>).join<E,F>(sa.name) as Join<Any,F>
        return JoinWrap(cb, join, arr)
    }


   /* fun on(): PathWrap<E> {
        var list = mutableListOf<() -> Predicate?>();


        var pw = PathWrap(cb, root, list, this);


        arr.add({ calculateOr(list) });
        return pw;
    }

    private fun calculateOr(list: MutableList<() -> Predicate?>): Predicate? {
        var predicates = mutableListOf<Predicate>();
        for (p in list) {
            var pp: Predicate? = p.invoke();
            if (pp != null) {
                predicates.add(pp);
            }
        };
        if (predicates.isNotEmpty()) {
            return (root as Join<Any,E>).on(*predicates.toTypedArray());
        }
        return null;
    }*/
}