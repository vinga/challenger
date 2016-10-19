package com.kameo.challenger.utils.odb.newapi

import javax.persistence.criteria.*
import kotlin.reflect.KMutableProperty1


class RootWrap<E> constructor(cb: CriteriaBuilder,
                              root: Root<E>,
                              arr: MutableList<() -> Predicate?>,
                              parent: PathWrap<E>? = null)

: PathWrap<E>(cb, root, arr, parent) {


    @Suppress("UNCHECKED_CAST")
    fun <F> join(sa: KMutableProperty1<E, F>): JoinWrap<F> {
        val join = (root as From<Any, E>).join<E, F>(sa.name) as Join<Any, F>;
        return JoinWrap(cb, join, arr);

    }


}