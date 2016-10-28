package com.kameo.challenger.utils.odb.newapi


import org.hibernate.sql.Update
import javax.persistence.criteria.*
import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty1


open class RootWrap<E,G> constructor(val pw: PathContext<E>,
                                pathSelect: ISugarQuerySelect<G>,
                                root: Root<E>)


: PathWrap<E,G>(pw, pathSelect, root) {




    @Suppress("UNCHECKED_CAST")
    fun <F> join(sa: KMutableProperty1<E, F>): JoinWrap<F,G> {
        val join = (root as From<Any, E>).join<E, F>(sa.name) as Join<Any, F>
        return JoinWrap(pw as PathContext<F>,pathSelect, join)

    }



    fun <F:Any> from(sa: KClass<F>): RootWrap<F,G> {
        val criteriaQuery=pw.criteria as? CriteriaQuery<F>;
        if (criteriaQuery!=null) {
            val from=criteriaQuery.from(sa.java);
            return RootWrap(pw as PathContext<F>, pathSelect, from)
        } else {
            val criteriaUpdateQuery = pw.criteria as? CriteriaUpdate<F>;
            if (criteriaUpdateQuery != null) {
                val from = criteriaUpdateQuery.from(sa.java);
                return RootWrap(pw as PathContext<F>, pathSelect, from)
            }
        }
       throw IllegalArgumentException("Clause 'from' is supported only for CriteriaQuery and CriteriaUpdate");

    }

}