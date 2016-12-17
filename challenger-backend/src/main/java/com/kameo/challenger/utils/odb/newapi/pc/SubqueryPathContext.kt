package com.kameo.challenger.utils.odb.newapi.pc

import com.kameo.challenger.utils.odb.newapi.ISugarQuerySelect
import com.kameo.challenger.utils.odb.newapi.pc.PathContext
import com.kameo.challenger.utils.odb.newapi.wraps.RootWrap
import com.kameo.challenger.utils.odb.newapi.SelectWrap
import com.kameo.challenger.utils.odb.newapi.wraps.ExpressionWrap
import com.kameo.challenger.utils.odb.newapi.wraps.SubqueryWrap
import javax.persistence.EntityManager
import javax.persistence.TypedQuery
import javax.persistence.criteria.CriteriaQuery
import javax.persistence.criteria.Expression
import javax.persistence.criteria.Selection
import javax.persistence.criteria.Subquery

class SubqeryPathContext<G>(clz: Class<*>,
                            em: EntityManager,
                            val parentContext: QueryPathContext<G>,
                            val subquery: Subquery<G>)
    : PathContext<G>(em, parentContext.criteria) {

    var selector: ISugarQuerySelect<*>? = null // set after execution


    init {
        root = subquery.from(clz as Class<Any>)

        defaultSelection = SelectWrap(root)
        rootWrap = RootWrap(this, root)
    }

    //TODO execute from QueryPathContext
    fun <RESULT, E> invokeQuery(query: (RootWrap<E, E>) -> ISugarQuerySelect<RESULT>): SubqueryWrap<RESULT,E> {
        selector = query.invoke(rootWrap as RootWrap<E, E>)
        val sell = selector!!.getSelection()
        val ss = subquery.select(sell as Expression<G>).distinct(selector!!.isDistinct())




        subquery.where(getPredicate())
        val groupBy = getGroupBy();
        if (groupBy.isNotEmpty()) {
            subquery.groupBy(groupBy.map { it.getExpression() })
        }

       // criteria.subquery()


       // return SubqueryWrap<RESULT,E>(parentContext as QueryPathContext<E>,ss, subquery as Subquery<RESULT>) as SubqueryWrap<RESULT, E>;
        return SubqueryWrap<RESULT,E>(parentContext as QueryPathContext<E> ,subquery as Expression<RESULT>, subquery as Subquery<RESULT>) as SubqueryWrap<RESULT, E>;
    }






}

