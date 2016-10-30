package com.kameo.challenger.utils.odb.newapi

import javax.persistence.criteria.Join
import kotlin.reflect.KMutableProperty1


class JoinWrap<E, G> constructor(val pw: PathContext<G>,
                                 override val root: Join<Any, E>)
: PathWrap<E, G>(pw, root) {


    @Suppress("UNCHECKED_CAST")
    fun <F> join(sa: KMutableProperty1<E, F>): JoinWrap<F, G> {
        val join = root.join<E, F>(sa.name) as Join<Any, F>
        return JoinWrap(pw, join)
    }

    @Suppress("UNCHECKED_CAST")
    // perhaps we want to create here dedicated class
    fun <F> joinList(sa: KMutableProperty1<E, List<F>>): JoinWrap<F, G> {
        val join = root.join<E, F>(sa.name) as Join<Any, F>
        return JoinWrap(pw, join)
    }

}