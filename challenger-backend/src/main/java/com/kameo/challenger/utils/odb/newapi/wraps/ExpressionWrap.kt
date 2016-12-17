package com.kameo.challenger.utils.odb.newapi.wraps

import com.kameo.challenger.utils.odb.newapi.IExpression
import com.kameo.challenger.utils.odb.newapi.ISelectExpressionProvider
import com.kameo.challenger.utils.odb.newapi.ISugarQuerySelect
import com.kameo.challenger.utils.odb.newapi.SelectWrap
import com.kameo.challenger.utils.odb.newapi.pc.PathContext
import javax.persistence.criteria.Expression
import javax.persistence.criteria.Selection

open class ExpressionWrap<E, G> constructor(
        val pc: PathContext<G>,
        val value: Expression<E>
) :
        ISelectExpressionProvider<E>,
        ISugarQuerySelect<G>, //by pathSelect,
        IExpression<E, G> {
    open val it: ExpressionWrap<E, G> by lazy {
        this
    }
    override fun getSelection(): Selection<*> {
        return pc.defaultSelection!!.getSelection()
    }

    override fun isDistinct(): Boolean {
        return pc.defaultSelection!!.isDistinct()
    }

    override fun isSingle(): Boolean {
        return pc.defaultSelection!!.isSingle()
    }

    override fun eq(expr: E): ExpressionWrap<E, G> {
        pc.add({ pc.cb.equal(this.value, expr) })
        return this
    }

    override fun eq(expr: IExpression<E, *>): ExpressionWrap<E, G> {
        pc.add({ pc.cb.equal(this.value, expr.getExpression()) })
        return this
    }

    open infix fun notEq(expr: IExpression<E, *>): ExpressionWrap<E, G> {
        pc.add({ pc.cb.notEqual(this.value, expr.getExpression()) })
        return this
    }

    open infix fun isIn(list: List<E>): ExpressionWrap<E, G> {
        pc.add({ value.`in`(list) })
        return this
    }
    open infix fun isIn(expr: ExpressionWrap<E,*>): ExpressionWrap<E, G> {
        pc.add({ value.`in`(expr.value) })
        return this
    }
    open infix fun isIn(expr: SubqueryWrap<E,*>): ExpressionWrap<E, G> {
        pc.add({ value.`in`(expr.subquery) })
        return this
    }
    open infix fun exists(expr: SubqueryWrap<*,*>): ExpressionWrap<E, G> {
        pc.add({pc.cb.exists(expr.subquery)  })
        return this
    }

    open infix fun notExists(expr: SubqueryWrap<*,*>): ExpressionWrap<E, G> {
        pc.add({pc.cb.not(pc.cb.exists(expr.subquery))  })
        return this
    }

    val cb = pc.cb

    override fun getDirectSelection(): ISugarQuerySelect<E> {
        return SelectWrap(value)
    }

    override fun getExpression(): Expression<E> {
        return value
    }

    fun groupBy(vararg exprs: IExpression<*, *>): ExpressionWrap<E, G> {
        pc.groupBy(exprs);
        return this
    }

    infix fun groupBy(expr: IExpression<*, *>): ExpressionWrap<E, G> {
        pc.groupBy(arrayOf(expr) as Array<out IExpression<Any, Any>>)
        return this
    }

}