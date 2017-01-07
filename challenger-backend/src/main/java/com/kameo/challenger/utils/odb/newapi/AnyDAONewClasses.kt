package com.kameo.challenger.utils.odb.newapi

import com.kameo.challenger.utils.odb.newapi.pc.PathContext
import com.kameo.challenger.utils.odb.newapi.wraps.ComparableExpressionWrap
import com.kameo.challenger.utils.odb.newapi.wraps.ExpressionWrap
import com.kameo.challenger.utils.odb.newapi.wraps.StringExpressionWrap
import javax.persistence.criteria.Expression
import javax.persistence.criteria.Predicate
import javax.persistence.criteria.Selection
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KMutableProperty1.Setter
import kotlin.reflect.KParameter
import kotlin.reflect.KProperty1
import kotlin.reflect.KProperty1.Getter
import kotlin.reflect.KType


interface IExpression<F, G> {
    fun getExpression(): Expression<F>
    infix fun eq(expr: IExpression<F, *>): IExpression<F, G>
    infix fun eq(expr: F): IExpression<F, G>
}

interface IStringExpressionWrap<G> : IExpression<String, G> {
    infix fun like(f: String): IExpression<String, G>
    infix fun like(f: Expression<String>): IExpression<String, G>
    infix fun like(f: ExpressionWrap<String, *>): IExpression<String, G>
    fun lower(): StringExpressionWrap<G>
}





class NumberExpressionWrap<F,G> constructor(
        pc: PathContext<G>,
        value: Expression<F>) : ComparableExpressionWrap<F, G>(pc, value) where F : Number, F:Comparable<F>

@Suppress("UNCHECKED_CAST")
operator fun <T, R> KProperty1<T, R?>.unaryPlus(): KMutableProperty1<T, R> {
    val foo = this
    if (foo is KMutableProperty1)
        return foo as KMutableProperty1<T, R>
    return object : KMutableProperty1<T, R> {
        override val name: String
            get() = foo.name

        override fun invoke(p1: T): R {
            throw UnsupportedOperationException("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun get(receiver: T): R {
            throw UnsupportedOperationException("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun callBy(args: Map<KParameter, Any?>): R {
            throw UnsupportedOperationException("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun call(vararg args: Any?): R {
            throw UnsupportedOperationException("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun set(receiver: T, value: R) {
            throw UnsupportedOperationException("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override val annotations: List<Annotation>
            get() = throw UnsupportedOperationException()
        override val returnType: KType
            get() = throw UnsupportedOperationException()
        override val parameters: List<KParameter>
            get() = throw UnsupportedOperationException()

        override val getter: Getter<T, R>
            get() = throw UnsupportedOperationException()
        override val setter: Setter<T, R>
            get() = throw UnsupportedOperationException()

    }


}

interface ISugarQuerySelect<E> {
    fun getSelection(): Selection<*>
    fun isSingle(): Boolean
    fun isDistinct(): Boolean
}

class SelectWrap<E> constructor(val select: Selection<E>, val distinct: Boolean = false) : ISugarQuerySelect<E> {
    override fun getSelection(): Selection<E> {
        return select
    }

    override fun isSingle(): Boolean {
        return distinct
    }

    override fun isDistinct(): Boolean {
        return false
    }
}

interface ISelectExpressionProvider<E> {
    fun getDirectSelection(): ISugarQuerySelect<E>
}

class SugarPredicate(val predicate: Predicate)