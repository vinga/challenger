package com.kameo.challenger.utils.odb.newapi.wraps

import com.kameo.challenger.utils.odb.newapi.pc.PathContext
import com.kameo.challenger.utils.odb.newapi.pc.QueryPathContext
import javax.persistence.criteria.Expression
import javax.persistence.criteria.Root
import javax.persistence.criteria.Subquery
import kotlin.reflect.KClass

open class SubqueryWrap<E, G>(
        val pw: QueryPathContext<G>,
        root: Expression<E>, val subquery: Subquery<E>) : ExpressionWrap<E, G>(pw, root) {


}
