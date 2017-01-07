package com.kameo.challenger.utils.odb.newapi.wraps

import com.kameo.challenger.utils.odb.newapi.pc.QueryPathContext
import javax.persistence.criteria.Expression
import javax.persistence.criteria.Subquery

open class SubqueryWrap<E, G>(
        val pw: QueryPathContext<G>,
        root: Expression<E>, val subquery: Subquery<E>) : ExpressionWrap<E, G>(pw, root)
