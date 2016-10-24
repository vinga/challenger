package com.kameo.challenger.utils.odb.newapi

import com.kameo.challenger.utils.odb.AnyDAO
import javax.persistence.criteria.*
import javax.persistence.metamodel.SingularAttribute
import kotlin.reflect.KMutableProperty1

open class PathWrap<E> constructor(val pc: PathContext, val root: Path<E>, var arr: MutableList<() -> Predicate?>, val parent: PathWrap<E>? = null) {

    val cb=pc.cb;

    infix fun eqId(id: Long): PathWrap<E> {
        arr.add({ pc.cb.equal(root.get<Path<Long>>(AnyDAO.id_column), id) })
        return this
    }

    infix fun inIds(ids: List<Long>): PathWrap<E> {
        arr.add({ root.get<Path<Long>>(AnyDAO.id_column).`in`(ids) })
        return this
    }

    fun newOr(): PathWrap<E> {
        var list = mutableListOf<() -> Predicate?>()


        var pw = PathWrap(pc, root, list, this)


        arr.add({ calculateOr(list) })
        return pw
    }

    fun newAnd(): PathWrap<E> {
        var list = mutableListOf<() -> Predicate?>()


        var pw = PathWrap(pc, root, list, this)


        arr.add({ calculateAnd(list) })
        return pw
    }

    fun finish(): PathWrap<E> {
        if (parent != null)
            return parent
        throw IllegalArgumentException()
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

    public fun getPredicate(): Predicate {
        var predicates = mutableListOf<Predicate>()
        for (p in arr) {
            var pp: Predicate? = p.invoke()
            if (pp != null) {
                predicates.add(pp)
            }
        }
        if (predicates.size == 1) {
            return predicates[0]
        } else {
            return cb.and(*predicates.toTypedArray())
        }
    }

    fun inIds(vararg ids: Long): PathWrap<E> {
        arr.add({ root.get<Path<Long>>(AnyDAO.id_column).`in`(ids) })
        return this
    }

    infix fun eq(id: E): PathWrap<E> {
        arr.add({ cb.equal(root, id) })
        return this
    }

    fun <F> eq(sa: SingularAttribute<E, F>, f: F): PathWrap<E> {
        arr.add({ cb.equal(root.get(sa), f) })
        return this
    }



    fun <F> eq(sa: KMutableProperty1<E, F>, f: F): PathWrap<E> {
        arr.add({ cb.equal(root.get<Path<F>>(sa.name), f) })
        return this
    }


    fun <F> notEq(sa: KMutableProperty1<E, F>, f: F): PathWrap<E> {
        arr.add({ cb.notEqual(root.get<F>(sa.name), f) })
        return this
    }

    fun <F> notEq(sa: SingularAttribute<E, F>, f: F): PathWrap<E> {
        arr.add({ cb.notEqual(root.get(sa), f) })
        return this
    }

    fun <F : Comparable<F>> after(sa: SingularAttribute<E, F>, f: F): PathWrap<E> {
        arr.add({ cb.greaterThan(root.get(sa), f) })
        return this
    }
    fun <F : Comparable<F>> after(sa: KMutableProperty1<E, F?>, f: F): PathWrap<E> {
        arr.add({ cb.greaterThan(root.get(sa.name), f) })
        return this
    }


    operator fun  <F> rangeTo(sa: KMutableProperty1<E, F>): PathWrap<F> {
        return get(sa)
    }

    fun eqIdToPred(id: Long): Predicate {
        return cb.equal(root.get<Path<Long>>(AnyDAO.id_column), id)
    }

    fun isIn(list: List<E>): PathWrap<E> {
        arr.add({ root.`in`(list) })
        return this
    }

    fun <F> get(sa: SingularAttribute<E, F>): PathWrap<F> {
        return PathWrap(pc, root.get(sa), arr)
    }

    infix fun <F> get(sa: KMutableProperty1<E, F>): PathWrap<F> {
        return PathWrap(pc, root.get(sa.name), arr)
    }

    fun <F> get(sa: KMutableProperty1<E, List<F>>): UseGetListOnJoinInstead {
        val join=(root as Join<Any, E>).join<E,F>(sa.name) as Join<Any, F>
        return UseGetListOnJoinInstead();
    }
    infix fun orderByAsc(sa: KMutableProperty1<E,*>):PathWrap<E> {
        pc.addOrder(cb.asc(root.get<Any>(sa.name)));
        return this;
    }
    infix fun orderByDesc(sa: KMutableProperty1<E,*>): PathWrap<E> {
        pc.addOrder(cb.desc(root.get<Any>(sa.name)));
        return this;
    }
    infix fun orderBy(pw: Pair<PathWrap<*>,Boolean>) {
        var (pathWrap, asc)=pw;
        pc.addOrder(if (asc) cb.asc(pathWrap.root) else cb.desc(pathWrap.root));
    }
    fun orderBy(vararg pw: Pair<PathWrap<*>,Boolean>) {
        for ((pathWrap, asc) in pw) {
            pc.addOrder(if (asc) cb.asc(pathWrap.root) else cb.desc(pathWrap.root));
        }
    }

    // type safety class to not use get with lists paremters
    class UseGetListOnJoinInstead {

    }


}