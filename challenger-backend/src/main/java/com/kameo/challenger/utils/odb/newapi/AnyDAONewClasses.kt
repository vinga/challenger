package com.kameo.challenger.utils.odb.newapi

import com.kameo.challenger.odb.api.IIdentity
import com.kameo.challenger.utils.odb.AnyDAO
import com.kameo.challenger.utils.odb.AnyDAONew.PathPairSelect
import com.kameo.challenger.utils.odb.AnyDAONew.PathTripleSelect
import com.kameo.challenger.utils.odb.newapi.pc.PathContext
import com.kameo.challenger.utils.odb.newapi.pc.UpdatePathContext
import com.kameo.challenger.utils.odb.newapi.wraps.*
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*
import javax.persistence.EntityManager
import javax.persistence.Query
import javax.persistence.TypedQuery
import javax.persistence.criteria.*
import javax.persistence.metamodel.SingularAttribute
import kotlin.reflect.*
import kotlin.reflect.KMutableProperty1.Setter
import kotlin.reflect.KProperty1.Getter


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
        value: Expression<F>) : ComparableExpressionWrap<F, G>(pc, value) where F : Number, F:Comparable<F> {

}

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