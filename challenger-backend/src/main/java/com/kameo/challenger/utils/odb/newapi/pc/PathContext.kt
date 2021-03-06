package com.kameo.challenger.utils.odb.newapi.pc

import com.kameo.challenger.utils.odb.newapi.IExpression
import com.kameo.challenger.utils.odb.newapi.ISugarQuerySelect
import com.kameo.challenger.utils.odb.newapi.wraps.PathWrap
import javax.persistence.EntityManager
import javax.persistence.criteria.*

abstract class PathContext<G>
constructor(
        val em: EntityManager,
        open val criteria: CommonAbstractCriteria) {

    val cb: CriteriaBuilder = em.criteriaBuilder
    val orders: MutableList<Order> = mutableListOf()
    val arraysStack: MutableList<MutableList<() -> Predicate?>> = mutableListOf()

    private var currentArray: MutableList<() -> Predicate?> = mutableListOf()
        private set
    var skip: Int? = null
    var take: Int? = null

    lateinit var root: Root<Any>
        protected set
    lateinit var rootWrap: PathWrap<*, G>
        protected set
    var defaultSelection: ISugarQuerySelect<Any>? = null
        protected set
    private var groupByList: MutableList<IExpression<*, *>>? = null

    fun addOrder(o: Order) {
        orders.add(o)
    }

    fun add(function: () -> Predicate?) {
        currentArray.add(function)
    }

    private fun calculateOr(list: MutableList<() -> Predicate?>): Predicate? {
        val predicates = toPredicates(list)
        return if (predicates.isNotEmpty())
            if (predicates.size == 1)
                predicates[0]
            else
                cb.or(*predicates.toTypedArray())
        else
            null
    }

    private fun calculateAnd(list: MutableList<() -> Predicate?>): Predicate? {
        val predicates = toPredicates(list)
        return if (predicates.isNotEmpty())
            if (predicates.size == 1)
                predicates[0]
            else
                cb.and(*predicates.toTypedArray())
        else
            null
    }

    private fun toPredicates(list: MutableList<() -> Predicate?>): MutableList<Predicate> {
        val predicates = list
                .asSequence()
                .mapNotNull { it.invoke() }
                .toMutableList()
        return predicates
    }

    fun mergeLevelUpAsOr() {
        val temp = currentArray
        unstackArray()
        val temp2 = currentArray

        val newCurr = mutableListOf<() -> Predicate?>()
        newCurr.add({ calculateAnd(temp2) })
        newCurr.add({ calculateAnd(temp) })

        val newCurrentArray = mutableListOf({ calculateOr(newCurr) })
        currentArray = newCurrentArray
    }

    fun mergeLevelUpAsAnd() {
        val temp = currentArray
        unstackArray()

        val newCurr = mutableListOf<() -> Predicate?>()
        newCurr.addAll(currentArray)
        newCurr.addAll(temp)


        val newCurrentArray = mutableListOf({ calculateAnd(newCurr) })
        currentArray = newCurrentArray
    }

    fun stackNewArray(newArr: MutableList<() -> Predicate?>) {
        arraysStack.add(currentArray)
        currentArray = newArr
    }

    fun unstackArray() {
        currentArray = arraysStack.last()
        arraysStack.remove(currentArray)
    }


    fun getPredicate(): Predicate {
        if (arraysStack.isNotEmpty())
            throw IllegalArgumentException("In or Or clause has not been closed")
        val predicates = currentArray.mapNotNull { it.invoke() }
        if (predicates.size == 1) {
            return predicates[0]
        } else {
            return cb.and(*predicates.toTypedArray())
        }
    }

    fun groupBy(expressions: Array<out IExpression<*, *>>) {
        groupByList = expressions.toMutableList()
    }

    protected fun getGroupBy(): List<IExpression<*, *>> = groupByList ?: emptyList()


}