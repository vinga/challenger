package com.kameo.challenger.utils.odb.newapi


import javax.persistence.EntityManager
import javax.persistence.Query
import javax.persistence.TypedQuery
import javax.persistence.criteria.*


abstract class PathContext<G>
    constructor(
        val em: EntityManager,
        open val criteria: CommonAbstractCriteria) {

    val cb: CriteriaBuilder = em.criteriaBuilder
    val orders: MutableList<Order> = mutableListOf()
    val arraysStack: MutableList<MutableList<() -> Predicate?>> = mutableListOf();

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


    fun addOrder(o: Order) {
        orders.add(o);
    }

    fun add(function: () -> Predicate?) {
        currentArray.add(function);
    }

    fun stackNewArray(newArr: MutableList<() -> Predicate?>) {
        arraysStack.add(currentArray);
        currentArray = newArr;
    }

    fun unstackArray() {
        currentArray = arraysStack.last();
        arraysStack.remove(currentArray);
    }


    fun getPredicate(): Predicate {
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

class QueryPathContext<G>(clz: Class<*>,
                          em: EntityManager,
                          override val criteria: CriteriaQuery<G> = em.criteriaBuilder.createQuery(clz) as CriteriaQuery<G>)
: PathContext<G>(em, criteria) {

    var selector: ISugarQuerySelect<*>? = null // set after execution


    init {
        root = criteria.from(clz as Class<Any>)
        defaultSelection = SelectWrap(root)
        rootWrap = RootWrap<Any, G>(this, root)
    }


    fun <RESULT, E> invokeQuery(query: (RootWrap<E, E>) -> ISugarQuerySelect<RESULT>): TypedQuery<RESULT> {
        selector = query.invoke(rootWrap as RootWrap<E, E>)
        var sell = selector!!.getSelection()
        criteria.select(sell as Selection<out G>)
        return calculateWhere(em) as TypedQuery<RESULT>
    }

    fun calculateWhere(em: EntityManager): TypedQuery<*> {
        criteria.where(getPredicate())
        if (orders.isNotEmpty())
            criteria.orderBy(orders)
        val jpaQuery = em.createQuery(criteria)
        applyPage(jpaQuery)
        return jpaQuery;
    }

    private fun applyPage(jpaQuery: TypedQuery<*>) {
        val skip = skip;
        if (skip != null)
            jpaQuery.firstResult = skip;
        val take = take;
        if (take != null)
            jpaQuery.maxResults = take;
    }

    fun <RESULT : Any> mapToPluralsIfNeeded(res: List<RESULT>): List<RESULT> {
        if (res.isNotEmpty()) {
            if (!selector!!.isSingle()) {
                if (res.first() is Array<*>) {

                    var rows = res as List<Array<Any>>
                    var row = rows.first()
                    if (row.size == 2) {
                        return rows.map({
                            Pair(it[0], it[1]) as RESULT
                        });
                    } else if (row.size == 3) {
                        return rows.map({
                            Triple(it[0], it[1], it[2]) as RESULT
                        });
                    }
                }
            }
        }
        return res
    }

}

class UpdatePathContext<G>(clz: Class<*>,
                           em: EntityManager,
                           override val criteria: CriteriaUpdate<G> = em.criteriaBuilder.createCriteriaUpdate(clz) as CriteriaUpdate<G>)
: PathContext<G>(em, criteria) {

    init {
        root = criteria.from(clz as Class<G>) as Root<Any>
        rootWrap = RootWrapUpdate<Any, G>(this, root)
    }

    @Suppress("UNCHECKED_CAST")
    fun <E> invokeUpdate(query: (RootWrapUpdate<E, E>) -> Unit): Query {
        query.invoke(rootWrap as RootWrapUpdate<E, E>)
        calculateWhere(criteria)
        return em.createQuery(criteria)
    }

    private fun calculateWhere(cq: CriteriaUpdate<*>) {
        cq.where(getPredicate())
    }
}

class DeletePathContext<G>(clz: Class<*>,
                           em: EntityManager,
                           override val criteria: CriteriaDelete<G> = em.criteriaBuilder.createCriteriaDelete(clz) as CriteriaDelete<G>)
: PathContext<G>(em, criteria) {

    init {
        root = (criteria as CriteriaDelete<Any>).from(clz as Class<Any>);
        rootWrap = RootWrap<Any, G>(this, root)
    }

    @Suppress("UNCHECKED_CAST")
    fun <E> invokeDelete(query: (RootWrap<E, E>) -> Unit): Query {
        query.invoke(rootWrap as RootWrap<E, E>)
        calculateWhere(criteria)
        return em.createQuery(criteria)

    }

    private fun calculateWhere(cq: CriteriaDelete<*>) {
        cq.where(getPredicate())
    }
}