package com.kameo.challenger.utils.odb.newapi.pc

import com.kameo.challenger.utils.odb.newapi.ISugarQuerySelect
import com.kameo.challenger.utils.odb.newapi.SelectWrap
import com.kameo.challenger.utils.odb.newapi.wraps.RootWrap
import javax.persistence.EntityManager
import javax.persistence.TypedQuery
import javax.persistence.criteria.CriteriaQuery
import javax.persistence.criteria.Selection

@Suppress("UNCHECKED_CAST")
class QueryPathContext<G>(clz: Class<*>,
                          em: EntityManager,
                          override val criteria: CriteriaQuery<G> = em.criteriaBuilder.createQuery(clz) as CriteriaQuery<G>)
    : PathContext<G>(em, criteria) {

    var selector: ISugarQuerySelect<*>? = null // set after execution


    init {
        root = criteria.from(clz as Class<Any>)
        defaultSelection = SelectWrap(root)
        rootWrap = RootWrap(this, root)
    }


    fun <RESULT, E> invokeQuery(query: (RootWrap<E, E>) -> ISugarQuerySelect<RESULT>): TypedQuery<RESULT> {
        selector = query.invoke(rootWrap as RootWrap<E, E>)
        val sell = selector!!.getSelection()
        val ss = criteria.select(sell as Selection<out G>).distinct(selector!!.isDistinct())

        val groupBy = getGroupBy()
        if (groupBy.isNotEmpty()) {
            criteria.groupBy(groupBy.map { it.getExpression() })
        }


        return calculateWhere(em) as TypedQuery<RESULT>
    }

    fun calculateWhere(em: EntityManager): TypedQuery<*> {
        criteria.where(getPredicate())
        if (orders.isNotEmpty())
            criteria.orderBy(orders)
        val jpaQuery = em.createQuery(criteria)
        applyPage(jpaQuery)
        return jpaQuery
    }

    private fun applyPage(jpaQuery: TypedQuery<*>) {
        val skip = skip
        if (skip != null)
            jpaQuery.firstResult = skip
        val take = take
        if (take != null)
            jpaQuery.maxResults = take
    }

    fun <RESULT : Any> mapToPluralsIfNeeded(res: List<RESULT>): List<RESULT> {
        if (res.isNotEmpty()) {
            if (!selector!!.isSingle()) {
                if (res.first() is Array<*>) {

                    val rows = res as List<Array<Any>>
                    val row = rows.first()
                    if (row.size == 2) {
                        return rows.map({
                            Pair(it[0], it[1]) as RESULT
                        })
                    } else if (row.size == 3) {
                        return rows.map({
                            Triple(it[0], it[1], it[2]) as RESULT
                        })
                    }
                }
            }
        }
        return res
    }

}