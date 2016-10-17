package com.kameo.challenger.utils.odb.newapi

import com.kameo.challenger.utils.odb.AnyDAO
import javax.persistence.criteria.CriteriaBuilder
import javax.persistence.criteria.Path
import javax.persistence.criteria.Predicate
import javax.persistence.criteria.Root
import javax.persistence.metamodel.SingularAttribute
import kotlin.reflect.KClass
import kotlin.reflect.KProperty


class PathWrap<E> constructor (val cb: CriteriaBuilder, val root: Path<E>, var arr:MutableList<()->Predicate?>, val parent: PathWrap<E>? =null) {



    infix fun eqId(id: Long): PathWrap<E> {



        arr.add({cb.equal(root.get<Path<Long>>(AnyDAO.id_column),id)});
        return this;
    }

    infix fun inIds(ids: List<Long>): PathWrap<E> {
        arr.add({root.get<Path<Long>>(AnyDAO.id_column).`in`(ids)});
        return this;
    }

    fun newOr():PathWrap<E> {
        var list=mutableListOf<()->Predicate?>();


        var pw=PathWrap(cb,root, list, this);


        arr.add({calculateOr(list)});
        return pw;
    }
    fun newAnd():PathWrap<E> {
        var list=mutableListOf<()->Predicate?>();


        var pw=PathWrap(cb,root, list, this);


        arr.add({calculateAnd(list)});
        return pw;
    }
    fun finish():PathWrap<E> {
        if (parent!=null)
             return parent;
        throw IllegalArgumentException();
    }

    private fun calculateOr(list: MutableList<() -> Predicate?>):Predicate? {
        var predicates = mutableListOf<Predicate>();
        for (p in list) {
            var pp: Predicate? = p.invoke();
            if (pp != null) {
                predicates.add(pp);
            }
        };
        if (predicates.isNotEmpty()) {
            return cb.or(*predicates.toTypedArray());
        }
        return null;
    }
    private fun calculateAnd(list: MutableList<() -> Predicate?>):Predicate? {
        var predicates = mutableListOf<Predicate>();
        for (p in list) {
            var pp: Predicate? = p.invoke();
            if (pp != null) {
                predicates.add(pp);
            }
        };
        if (predicates.isNotEmpty()) {
            return cb.and(*predicates.toTypedArray());
        }
        return null;
    }

    public fun getPredicate():Predicate {
        var predicates = mutableListOf<Predicate>();
        for (p in arr) {
            var pp: Predicate? = p.invoke();
            if (pp != null) {
                predicates.add(pp);
            }
        };
        if (predicates.size==1) {
            return predicates[0];
        } else {
           return cb.and(*predicates.toTypedArray());
        }
    }

    fun inIds(vararg  ids: Long): PathWrap<E> {
        arr.add({root.get<Path<Long>>(AnyDAO.id_column).`in`(ids)});
        return this;
    }

    infix fun eq(id: E): PathWrap<E> {
        arr.add({cb.equal(root,id)});
        return this;
    }

    fun <F> eq(sa: SingularAttribute<E,F>, f: F): PathWrap<E> {
        arr.add({cb.equal(root.get(sa),f)});
        return this;
    }

/*
    fun <F> eq(sa: KProperty<F>, f: F): PathWrap<E> {
        arr.add({cb.equal(root.get<Path<F>>(sa.name),f)});
        return this;
    }
*/

    fun <F> notEq(sa: SingularAttribute<E,F>, f: F): PathWrap<E> {
        arr.add({cb.notEqual(root.get(sa),f)});
        return this;
    }
    fun <F:Comparable<F>> after(sa: SingularAttribute<E,F>, f: F): PathWrap<E> {
        arr.add({cb.greaterThan(root.get(sa),f)});
        return this;
    }
    fun <F> get(sa: SingularAttribute<E,F>): PathWrap<F> {
        return PathWrap(cb,root.get(sa),arr);
    }

    fun eqIdToPred(id: Long): Predicate {
        return cb.equal(root.get<Path<Long>>(AnyDAO.id_column),id)
    }


}