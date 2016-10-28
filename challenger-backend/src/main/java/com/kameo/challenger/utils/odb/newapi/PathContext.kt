package com.kameo.challenger.utils.odb.newapi

import javax.persistence.criteria.CommonAbstractCriteria
import javax.persistence.criteria.CriteriaBuilder
import javax.persistence.criteria.Order
import javax.persistence.criteria.Predicate


class PathContext constructor(val cb: CriteriaBuilder,
                              val criteria: CommonAbstractCriteria,

                              val orders: MutableList<Order> = mutableListOf(),

                              var skip: Int? = null,
                              var take: Int? = null
) {

    var currentArray: MutableList<() -> Predicate?> = mutableListOf();

    val arraysStack:MutableList<MutableList<() -> Predicate?>> = mutableListOf();



    fun addOrder(o: Order) {
        orders.add(o);
    }

    fun  add(function: () -> Predicate?) {
        currentArray.add(function);
    }

    fun stackNewArray(newArr: MutableList<() -> Predicate?>) {


        arraysStack.add(currentArray);
        currentArray = newArr;
    }

    fun unstackArray() {
        currentArray=arraysStack.last();
        arraysStack.remove(currentArray);
    }


    public fun getPredicate(): Predicate {
        if (arraysStack.isNotEmpty())
            throw IllegalArgumentException("In or Or clause has not been closed");
        var predicates = mutableListOf<Predicate>()
        for (p in currentArray) {
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

}