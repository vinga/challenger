package com.kameo.challenger.utils.odb.newapi

import com.kameo.challenger.odb.api.IIdentity
import com.kameo.challenger.utils.odb.AnyDAO
import com.kameo.challenger.utils.odb.AnyDAONew.PathPairSelect
import com.kameo.challenger.utils.odb.AnyDAONew.PathTripleSelect
import java.time.LocalDateTime
import java.util.*
import javax.persistence.criteria.Expression
import javax.persistence.criteria.Join
import javax.persistence.criteria.Path
import javax.persistence.criteria.Predicate
import javax.persistence.metamodel.SingularAttribute
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KMutableProperty1.Setter
import kotlin.reflect.KParameter
import kotlin.reflect.KProperty1
import kotlin.reflect.KProperty1.Getter
import kotlin.reflect.KType

open class PathWrap<E, G> constructor(
        pc: PathContext<G>,
        open val root: Path<E>
) :
        ExpressionWrap <E, G>(pc,  root) {

    override fun getDirectSelection(): ISugarQuerySelect<E> {
        return SelectWrap(root);
    }

    infix fun skip(skip: Int): PathWrap<E, G> {
        pc.skip = skip
        return this
    }

    infix fun limit(take: Int): PathWrap<E, G> {
        pc.take = take
        return this
    }


    infix fun <F> select(pw: ExpressionWrap<F,G>): ISugarQuerySelect<F> {
        return pw.getDirectSelection();
    }


    fun <F, G> select(pw1: ISelectExpressionProvider<F>, pw2: ISelectExpressionProvider<G>): PathPairSelect<F, G> {
        return PathPairSelect(pw1.getDirectSelection(), pw2.getDirectSelection(), pc.cb);
    }


    fun <F, G, H> select(pw1: ISelectExpressionProvider<F>, pw2: ISelectExpressionProvider<G>, pw3: ISelectExpressionProvider<H>): PathTripleSelect<F, G, H> {
        return PathTripleSelect(pw1.getDirectSelection(), pw2.getDirectSelection(), pw3.getDirectSelection(), pc.cb)
    }

    infix fun eqId(id: Long): PathWrap<E, G> {
        pc.add({ pc.cb.equal(root.get<Path<Long>>(AnyDAO.id_column), id) })
        return this
    }

    infix fun inIds(ids: List<Long>): PathWrap<E, G> {
        pc.add({ root.get<Path<Long>>(AnyDAO.id_column).`in`(ids) })
        return this
    }


    class ClousureWrap<E, G>(//var innerList:MutableList<() -> Predicate?> = mutableListOf<() -> Predicate?>(),
            pc: PathContext<G>,
            root: Path<E>
    ) : PathWrap<E, G>(pc,  root) {


    }

    fun <I, J> ref(ref: PathWrap<I, J>, clause: (PathWrap<I, J>) -> Unit): PathWrap<E, G> {
        clause.invoke(ref);
        return this;
    }

    fun ref(clause: (PathWrap<E, G>) -> Unit): PathWrap<E, G> {
        clause.invoke(this)
        return this
    }


    fun <I, J> ref(ref: PathWrap<I, J>): PathWrap<I, J> {
        return ref
    }

    fun finish(): PathWrap<E, G> {
        pc.unstackArray()
        return this;
    }

    fun newOr(): ClousureWrap<E, G> {
        var list = mutableListOf<() -> Predicate?>()


        var pw = ClousureWrap(pc,  root)


        pc.add({ calculateOr(list) })
        pc.stackNewArray(list);
        return pw
    }


    fun newOr(orClause: (PathWrap<E, G>) -> Unit): PathWrap<E, G> {
        var list = mutableListOf<() -> Predicate?>()
        var pw = ClousureWrap(pc,  root)
        pc.add({ calculateOr(list) })
        pc.stackNewArray(list)
        orClause.invoke(pw)
        pc.unstackArray();
        return this
    }

    fun newAnd(orClause: (PathWrap<E, G>) -> Unit): PathWrap<E, G> {
        var list = mutableListOf<() -> Predicate?>()
        var pw = ClousureWrap(pc,  root)
        pc.add({ calculateAnd(list) })
        pc.stackNewArray(list)
        orClause.invoke(pw)
        pc.unstackArray();
        return this
    }


    fun newAnd(): ClousureWrap<E, G> {
        var list = mutableListOf<() -> Predicate?>()
        var pw = ClousureWrap(pc,  root)
        pc.add({ calculateAnd(list) })
        pc.stackNewArray(list);
        return pw
    }


    private fun calculateOr(list: MutableList<() -> Predicate?>): Predicate? {
        var predicates = toPredicates(list)
        return if (predicates.isNotEmpty())
            cb.or(*predicates.toTypedArray())
        else
            null
    }

    private fun calculateAnd(list: MutableList<() -> Predicate?>): Predicate? {
        var predicates = toPredicates(list)
        return if (predicates.isNotEmpty())
            cb.and(*predicates.toTypedArray())
        else
            null
    }

    private fun toPredicates(list: MutableList<() -> Predicate?>): MutableList<Predicate> {
        var predicates = mutableListOf<Predicate>()
        for (p in list) {
            var pp: Predicate? = p.invoke()
            if (pp != null) {
                predicates.add(pp)
            }
        }
        return predicates
    }


    fun inIds(vararg ids: Long): PathWrap<E, G> {
        pc.add({ root.get<Path<Long>>(AnyDAO.id_column).`in`(ids) })
        return this
    }

    override infix fun eq(expr: E): PathWrap<E, G> {
        super.eq(expr);
        return this
    }

    fun <F> eq(sa: SingularAttribute<E, F>, f: F): PathWrap<E, G> {
        pc.add { cb.equal(root.get(sa), f) }
        return this
    }

    fun like(sa: KMutableProperty1<E, String>, f: String): PathWrap<E, G> {
        pc.add { cb.like(root.get<Path<String>>(sa.name) as (Expression<String>), f) }
        return this
    }

    fun <F> eq(sa: KMutableProperty1<E, F>, f: F): PathWrap<E, G> {
        pc.add({ cb.equal(root.get<Path<F>>(sa.name), f) })
        return this
    }


    fun <F : IIdentity> eqId(sa: KMutableProperty1<E, F>, id: Long): PathWrap<E, G> {
        pc.add({ cb.equal(root.get<Path<F>>(sa.name).get<Long>(AnyDAO.id_column), id) })
        return this
    }


    fun <F> eq(exp1: ExpressionWrap<F,G>, f: F): PathWrap<E, G> {
        pc.add({ cb.equal(exp1.getExpression(), f) })
        return this
    }

    fun <F> notEq(sa: KMutableProperty1<E, F>, f: F): PathWrap<E, G> {
        pc.add({ cb.notEqual(root.get<F>(sa.name), f) })
        return this
    }

    fun <F> notEq(sa: SingularAttribute<E, F>, f: F): PathWrap<E, G> {
        pc.add({ cb.notEqual(root.get(sa), f) })
        return this
    }

    fun <F : Comparable<F>> after(sa: SingularAttribute<E, F>, f: F): PathWrap<E, G> {
        pc.add({ cb.greaterThan(root.get(sa), f) })
        return this
    }

    fun <F : Comparable<F>> after(sa: KMutableProperty1<E, F>, f: F): PathWrap<E, G> {
        pc.add({ cb.greaterThan(root.get(sa.name), f) })
        return this
    }


    fun <F : Comparable<F>> before(sa: KMutableProperty1<E, F>, f: F): PathWrap<E, G> {
        pc.add({ cb.lessThan(root.get(sa.name), f) })
        return this
    }
    @JvmName("afterDate")
    fun after(sa: KMutableProperty1<E, Date?>, f: Date): PathWrap<E, G> {
        pc.add({ cb.greaterThan(root.get(sa.name), f) })
        return this
    }

    @JvmName("after")
    fun after(sa: KMutableProperty1<E, LocalDateTime?>, f: LocalDateTime): PathWrap<E, G> {
        pc.add({ cb.greaterThan(root.get(sa.name), f) })
        return this
    }
    @JvmName("before")
    fun before(sa: KMutableProperty1<E, LocalDateTime?>, f: LocalDateTime): PathWrap<E, G> {
        pc.add({ cb.lessThan(root.get(sa.name), f) })
        return this
    }
    fun before(sa: KMutableProperty1<E, Long?>, f: Long): PathWrap<E, G> {
        pc.add({ cb.lessThan(root.get(sa.name), f) })
        return this
    }
    @JvmName("beforeDate")
    fun before(sa: KMutableProperty1<E, Date?>, f: Date): PathWrap<E, G> {
        pc.add({ cb.lessThan(root.get(sa.name), f) })
        return this
    }

    operator fun <F> rangeTo(sa: KMutableProperty1<E, F>): PathWrap<F, G> {
        return get(sa)
    }

    fun eqIdToPred(id: Long): Predicate {
        return cb.equal(root.get<Path<Long>>(AnyDAO.id_column), id)
    }

    fun isIn(list: List<E>): PathWrap<E, G> {
        pc.add({ root.`in`(list) })
        return this
    }

    fun <F : Number> max(sa: KMutableProperty1<E, F>): ExpressionWrap<F,G> {
        return ExpressionWrap<F,G>(pc,pc.cb.max(root.get(sa.name)))
    }

    fun <F : Number> min(sa: KMutableProperty1<E, F>): ExpressionWrap<F,G> {
        return ExpressionWrap<F,G>(pc,pc.cb.min(root.get(sa.name)))
    }


    infix fun orderByAsc(sa: KMutableProperty1<E, *>): PathWrap<E, G> {
        pc.addOrder(cb.asc(root.get<Any>(sa.name)))
        return this
    }

    infix fun orderByDesc(sa: KMutableProperty1<E, *>): PathWrap<E, G> {
        pc.addOrder(cb.desc(root.get<Any>(sa.name)))
        return this
    }

    infix fun orderBy(pw: Pair<PathWrap<*, *>, Boolean>) {
        var (pathWrap, asc) = pw
        pc.addOrder(if (asc) cb.asc(pathWrap.root) else cb.desc(pathWrap.root))
    }

    fun orderBy(vararg pw: Pair<PathWrap<*, *>, Boolean>) {
        for ((pathWrap, asc) in pw) {
            pc.addOrder(if (asc) cb.asc(pathWrap.root) else cb.desc(pathWrap.root));
        }
    }


    // type safety class to not use get with lists paremters
    class UseGetListOnJoinInstead {

    }


    infix fun notEq(f: E): PathWrap<E, G> {
        pc.add { cb.notEqual(root, f) }
        return this
    }


    fun <F> get(sa: SingularAttribute<E, F>): PathWrap<F, G> {
        return PathWrap<F, G>(pc,  root.get(sa))
    }

    infix fun <F> get(sa: KMutableProperty1<E, F>): PathWrap<F, G> {
        return PathWrap(pc,  root.get(sa.name))
    }

    fun <F> get(sa: KMutableProperty1<E, List<F>>): UseGetListOnJoinInstead {
        sa.name
        //val join = (root as Join<Any, E>).join<E, F>(sa.name) as Join<Any, F>
        return UseGetListOnJoinInstead()
    }


    @JvmName("getAsString")
    infix fun get(sa: KMutableProperty1<E, String>): StringPathWrap<G> {
        return StringPathWrap<G>(pc,  root.get(sa.name))
    }

}

class StringPathWrap<G>(pc: PathContext<G>,

                        root: Path<String>) : PathWrap<String, G>(pc, root), IStringExpressionWrap<G> {

    override fun lower(): StringExpressionWrap<G> {
        return StringExpressionWrap(pc,  pc.cb.lower(root));
    }

    override infix fun like(f: String): PathWrap<String, G> {
        pc.add({ pc.cb.like(root as (Expression<String>), f) })
        return this
    }

    override infix fun like(f: Expression<String>): PathWrap<String, G> {

        pc.add({ pc.cb.like(root as (Expression<String>), f) })
        return this;
    }


}


operator fun <T, R> KProperty1<T, R?>.unaryPlus(): KMutableProperty1<T, R> {
    val foo = this;
    if (foo is KMutableProperty1)
        return foo as KMutableProperty1<T, R>;
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

    };


}

interface ISelectExpressionProvider<E> {
    fun getDirectSelection(): ISugarQuerySelect<E>;
}

class SugarPredicate(val predicate: Predicate) {

}