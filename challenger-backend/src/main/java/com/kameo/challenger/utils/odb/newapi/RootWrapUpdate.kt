package com.kameo.challenger.utils.odb.newapi

import javax.persistence.criteria.*
import kotlin.reflect.KMutableProperty1

class RootWrapUpdate<E,G> constructor(val pw: PathContext<E>, root: Root<E>): PathWrap<E,G>(pw,  root) {

    fun <F> set(sa: KMutableProperty1<E, F>, f: F): RootWrapUpdate<E,G> {
        val criteriaUpdate=pw.criteria as? CriteriaUpdate<F> ?: throw IllegalArgumentException();
        criteriaUpdate.set(get(sa).root,f)
        return this
    }

    fun <F> set(sa: KMutableProperty1<E, F>, f: Expression<F>): RootWrapUpdate<E,G> {
        val criteriaUpdate=pw.criteria as? CriteriaUpdate<F> ?: throw IllegalArgumentException();
        criteriaUpdate.set(get(sa).root,f)
        return this
    }

    fun <F> set(sa: KMutableProperty1<E, F>, f: PathWrap<F,G>): RootWrapUpdate<E,G> {
        val criteriaUpdate=pw.criteria as? CriteriaUpdate<F> ?: throw IllegalArgumentException();
        criteriaUpdate.set(get(sa).root,f.root)
        return this
    }



}