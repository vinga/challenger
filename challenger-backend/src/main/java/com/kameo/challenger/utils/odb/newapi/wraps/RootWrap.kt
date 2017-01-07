package com.kameo.challenger.utils.odb.newapi.wraps

import com.kameo.challenger.utils.odb.newapi.pc.PathContext
import javax.persistence.criteria.*
import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty1

open class RootWrap<E, G> constructor(
        val pw: PathContext<G>,
        root: Root<E>) : PathWrap<E, G>(pw, root) {

    override val it: RootWrap<E, G> by lazy {
        this
    }

    @Suppress("UNCHECKED_CAST")
    fun <F> join(sa: KMutableProperty1<E, F>): JoinWrap<F, G> {
        val join = (root as From<Any, E>).join<E, F>(sa.name) as Join<Any, F>
        return JoinWrap(pw, join)
    }
    @Suppress("UNCHECKED_CAST")
    fun <F : Any> from(sa: KClass<F>): RootWrap<F, G> {
        val criteriaQuery = pw.criteria as? CriteriaQuery<F>
        if (criteriaQuery != null) {
            val from = criteriaQuery.from(sa.java)
            return RootWrap(pw, from)
        } else {
            val criteriaUpdateQuery = pw.criteria as? CriteriaUpdate<F>
            if (criteriaUpdateQuery != null) {
                val from = criteriaUpdateQuery.from(sa.java)
                return RootWrap(pw, from)
            }
        }
        throw IllegalArgumentException("Clause 'from' is supported only for CriteriaQuery and CriteriaUpdate")

    }

    

}