package com.kameo.challenger.utils.odb.newapi

import com.kameo.challenger.utils.odb.AnyDAO
import com.kameo.challenger.utils.odb.AnyDAONew.*

import org.hibernate.sql.Select
import java.beans.Introspector
import java.lang.invoke.LambdaMetafactory
import java.lang.invoke.MethodHandles
import java.lang.reflect.Method
import java.util.*
import java.util.function.DoubleUnaryOperator
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
        val pc: PathContext,
        val pathSelect: ISugarQuerySelect<G>,
        val root: Path<E>
        ) :  ISelectExpressionProvider <E>,  ISugarQuerySelect<G>  by pathSelect   {

    override fun getDirectSelection(): ISugarQuerySelect<E> {
       return ExpressionWrap(root);
    }


    val cb = pc.cb;

    infix fun skip(skip: Int): PathWrap<E,G> {
        pc.skip = skip
        return this
    }

    infix fun limit(take: Int): PathWrap<E,G> {
        pc.take = take
        return this
    }

    infix fun <F,G> select(pw: PathWrap<F,G>): ISugarQuerySelect<F> {
        return SelectWrap(pw.root);


       // var pww:PathWrap<F,F> = PathWrap<F,F>(arr=pw.arr,pc=pw.pc,pathSelect = SelectWrap(pw.root), root=pw.root, parent=pw.parent as? PathWrap<F, F>);
        //return pww;
    }
    infix fun <F> select(pw: ExpressionWrap<F>): ISugarQuerySelect<F> {
        return pw;
    }


    fun <F, G> select(pw1: ISelectExpressionProvider<F>, pw2: ISelectExpressionProvider<G>): PathPairSelect<F, G> {
        return PathPairSelect(pw1.getDirectSelection(), pw2.getDirectSelection())
    }

    fun <F, G, H> select(pw1: ISelectExpressionProvider<F>, pw2: ISelectExpressionProvider<G>, pw3: ISelectExpressionProvider<H>): PathTripleSelect<F, G, H> {
        return PathTripleSelect(pw1.getDirectSelection(), pw2.getDirectSelection(), pw3.getDirectSelection())
    }

    infix fun eqId(id: Long): PathWrap<E,G> {
        pc.add({ pc.cb.equal(root.get<Path<Long>>(AnyDAO.id_column), id) })
        return this
    }

    infix fun inIds(ids: List<Long>): PathWrap<E,G> {
        pc.add({ root.get<Path<Long>>(AnyDAO.id_column).`in`(ids) })
        return this
    }


    class ClousureWrap<E,G>(//var innerList:MutableList<() -> Predicate?> = mutableListOf<() -> Predicate?>(),
                             pc: PathContext,
                             pathSelect: ISugarQuerySelect<G>,
                             root: Path<E>,

                             val parent: PathWrap<E,G>? = null,

                             arr: MutableList<() -> Predicate?> = mutableListOf<() -> Predicate?>()

    ): PathWrap<E,G>(pc,  pathSelect, root) {


        fun <I,J> ref(ref : PathWrap<I,J>): PathWrap<I,J> {

            var pw = PathWrap(ref.pc, ref.pathSelect, ref.root)
            return pw;
        }

     /*fun   fun finish(): PathWrap<E,G> {
            pc.unstackArray();
            if (parent != null)
                return parent
            throw IllegalArgumentException()
        }*/
    }


    fun finish(): PathWrap<E,G> {
        pc.unstackArray();
        return this;
    }

    fun newOr(): ClousureWrap<E,G> {
        var list = mutableListOf<() -> Predicate?>()


        var pw = ClousureWrap(pc, pathSelect, root, this)


        pc.add({ calculateOr(list) })
        pc.stackNewArray(list);
        return pw
    }


    fun newOr(orClause: (PathWrap<E,G>)-> Unit): PathWrap<E,G> {
        var list = mutableListOf<() -> Predicate?>()
        var pw = ClousureWrap(pc, pathSelect, root, this)
        pc.add({ calculateOr(list) })
        pc.stackNewArray(list);
        orClause.invoke(pw);
        pc.unstackArray();
        return this
    }
    fun newAnd(orClause: (PathWrap<E,G>)->Unit): PathWrap<E,G> {
        var list = mutableListOf<() -> Predicate?>()
        var pw = ClousureWrap(pc, pathSelect, root, this)
        pc.add({ calculateAnd(list) })
        pc.stackNewArray(list);
        orClause.invoke(pw);
        pc.unstackArray();
        return this
    }


    fun newAnd(): ClousureWrap<E,G> {
        var list = mutableListOf<() -> Predicate?>()

        var pw = ClousureWrap(pc, pathSelect, root,  this)



        pc.add({ calculateAnd(list) })
        pc.stackNewArray(list);
        return pw
    }




    private fun calculateOr(list: MutableList<() -> Predicate?>): Predicate? {
        var predicates = mutableListOf<Predicate>()
        for (p in list) {
            var pp: Predicate? = p.invoke()
            if (pp != null) {
                predicates.add(pp)
            }
        }
        if (predicates.isNotEmpty()) {
            return cb.or(*predicates.toTypedArray())
        }
        return null
    }

    private fun calculateAnd(list: MutableList<() -> Predicate?>): Predicate? {
        var predicates = mutableListOf<Predicate>()
        for (p in list) {
            var pp: Predicate? = p.invoke()
            if (pp != null) {
                predicates.add(pp)
            }
        }
        if (predicates.isNotEmpty()) {
            return cb.and(*predicates.toTypedArray())
        }
        return null
    }



    fun inIds(vararg ids: Long): PathWrap<E,G> {
        pc.add({ root.get<Path<Long>>(AnyDAO.id_column).`in`(ids) })
        return this
    }

    infix fun eq(id: E): PathWrap<E,G> {
        pc.add({ cb.equal(root, id) })
        return this
    }




    fun <F> eq(sa: SingularAttribute<E, F>, f: F): PathWrap<E,G> {
        pc.add({ cb.equal(root.get(sa), f) })
        return this
    }

    fun like(sa: KMutableProperty1<E, String>, f: String): PathWrap<E,G> {
        pc.add({ cb.like(root.get<Path<String>>(sa.name) as (Expression<String>), f) })
        return this
    }

    fun <F> eq(sa: KMutableProperty1<E, F>, f: F): PathWrap<E,G> {
        pc.add({ cb.equal(root.get<Path<F>>(sa.name), f) })
        return this
    }


    fun <F> notEq(sa: KMutableProperty1<E, F>, f: F): PathWrap<E,G> {
        pc.add({ cb.notEqual(root.get<F>(sa.name), f) })
        return this
    }

    fun <F> notEq(sa: SingularAttribute<E, F>, f: F): PathWrap<E,G> {
        pc.add({ cb.notEqual(root.get(sa), f) })
        return this
    }

    fun <F : Comparable<F>> after(sa: SingularAttribute<E, F>, f: F): PathWrap<E,G> {
        pc.add({ cb.greaterThan(root.get(sa), f) })
        return this
    }

    fun <F : Comparable<F>> after(sa: KMutableProperty1<E, F>, f: F): PathWrap<E,G> {
        pc.add({ cb.greaterThan(root.get(sa.name), f) })
        return this
    }


    fun <F : Comparable<F>> before(sa: KMutableProperty1<E, F>, f: F): PathWrap<E,G> {
        pc.add({ cb.lessThan(root.get(sa.name), f) })
        return this
    }

    fun after(sa: KMutableProperty1<E, Date?>, f: Date): PathWrap<E,G> {
        pc.add({ cb.greaterThan(root.get(sa.name), f) })
        return this
    }

    fun before(sa: KMutableProperty1<E, Long?>, f: Long): PathWrap<E,G> {
        pc.add({ cb.lessThan(root.get(sa.name), f) })
        return this
    }

    fun before(sa: KMutableProperty1<E, Date?>, f: Date): PathWrap<E,G> {
        pc.add({ cb.lessThan(root.get(sa.name), f) })
        return this
    }

    operator fun <F> rangeTo(sa: KMutableProperty1<E, F>): PathWrap<F,G> {
        return get(sa)
    }

    fun eqIdToPred(id: Long): Predicate {
        return cb.equal(root.get<Path<Long>>(AnyDAO.id_column), id)
    }

    fun isIn(list: List<E>): PathWrap<E,G> {
        pc.add({ root.`in`(list) })
        return this
    }

    fun <F> get(sa: SingularAttribute<E, F>): PathWrap<F,G> {
        return PathWrap(pc, pathSelect, root.get(sa))
    }

    infix fun <F> get(sa: KMutableProperty1<E, F>): PathWrap<F,G> {
        return PathWrap(pc, pathSelect, root.get(sa.name))
    }


    fun <F:Number> max(sa: KMutableProperty1<E, F>): ExpressionWrap<F> {
          return ExpressionWrap(pc.cb.max(root.get(sa.name)) as Expression<F>);
     }
    fun <F:Number> min(sa: KMutableProperty1<E, F>): ExpressionWrap<F> {
        return ExpressionWrap(pc.cb.min(root.get(sa.name)) as Expression<F>);
    }

    fun <F> get(sa: KMutableProperty1<E, List<F>>): UseGetListOnJoinInstead {
        val join = (root as Join<Any, E>).join<E, F>(sa.name) as Join<Any, F>
        return UseGetListOnJoinInstead();
    }

    infix fun orderByAsc(sa: KMutableProperty1<E, *>): PathWrap<E,G> {
        pc.addOrder(cb.asc(root.get<Any>(sa.name)));
        return this;
    }

    infix fun orderByDesc(sa: KMutableProperty1<E, *>): PathWrap<E,G> {
        pc.addOrder(cb.desc(root.get<Any>(sa.name)));
        return this;
    }

    infix fun orderBy(pw: Pair<PathWrap<*,*>, Boolean>) {
        var (pathWrap, asc) = pw;
        pc.addOrder(if (asc) cb.asc(pathWrap.root) else cb.desc(pathWrap.root));
    }

    fun orderBy(vararg pw: Pair<PathWrap<*,*>, Boolean>) {
        for ((pathWrap, asc) in pw) {
            pc.addOrder(if (asc) cb.asc(pathWrap.root) else cb.desc(pathWrap.root));
        }
    }

    // type safety class to not use get with lists paremters
    class UseGetListOnJoinInstead {

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

class SugarPredicate(val predicate:Predicate) {

}