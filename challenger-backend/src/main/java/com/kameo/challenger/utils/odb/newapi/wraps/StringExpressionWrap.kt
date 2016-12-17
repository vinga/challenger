package com.kameo.challenger.utils.odb.newapi.wraps

import com.kameo.challenger.utils.odb.newapi.wraps.ExpressionWrap
import com.kameo.challenger.utils.odb.newapi.IExpression
import com.kameo.challenger.utils.odb.newapi.IStringExpressionWrap
import com.kameo.challenger.utils.odb.newapi.pc.PathContext
import javax.persistence.criteria.Expression

class StringExpressionWrap<G> constructor(
        pc: PathContext<G>,
        value: Expression<String>) : ExpressionWrap<String, G>(pc, value), IStringExpressionWrap<G> {

    override infix fun like(f: String): IExpression<String, G> {
        pc.add({ pc.cb.like(value, f) })
        return this
    }

    override infix fun like(f: Expression<String>): IExpression<String, G> {
        pc.add({ pc.cb.like(value, f) })
        return this
    }
    override infix fun like(f: ExpressionWrap<String, *>): IExpression<String, G> {
        pc.add({ pc.cb.like(value, f.value) })
        return this
    }
    override fun lower(): StringExpressionWrap<G> {
        return StringExpressionWrap(pc, pc.cb.lower(value))
    }
}