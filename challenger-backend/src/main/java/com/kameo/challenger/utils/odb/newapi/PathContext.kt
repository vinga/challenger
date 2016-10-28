package com.kameo.challenger.utils.odb.newapi

import javax.persistence.EntityManager
import javax.persistence.TypedQuery
import javax.persistence.criteria.*


class PathContext<E> constructor(val clz: Class<E>,
                                 val em: EntityManager,

                                 val criteria: CommonAbstractCriteria

) {
    val cb: CriteriaBuilder=em.criteriaBuilder;

    companion object {
        fun <E,G> createSelectQuery(clz: Class<E>, resultClass: Class<G>, em: EntityManager): PathContext<E> {
            val cb=em.criteriaBuilder;
            val pc = PathContext(clz, em, cb.createQuery(resultClass));
            return pc;
        }
    }
    var currentArray: MutableList<() -> Predicate?> = mutableListOf();
        private set

    val orders: MutableList<Order> = mutableListOf()
    val arraysStack: MutableList<MutableList<() -> Predicate?>> = mutableListOf();

    var forceNoResultsInQuery: Boolean = false;
        private set
    var skip: Int? = null;

    var take: Int? = null;


    lateinit var root: Root<E>;
        private set
    lateinit var rootWrap: PathWrap<E,E>
        private set

    init {
        if (criteria is CriteriaQuery<*>) {
            root = criteria.from(clz) as Root<E>;
            rootWrap=RootWrap(this, SelectWrap(root), root)
        } else if (criteria is CriteriaUpdate<*>) {
            root = (criteria as CriteriaUpdate<E>).from(clz);
            rootWrap=RootWrapUpdate(this, SelectWrap(root), root)
        } else if (criteria is CriteriaDelete<*>) {
            root = (criteria as CriteriaDelete<E>).from(clz);
            rootWrap=RootWrap(this, SelectWrap(root), root)
        } else throw IllegalArgumentException();

    }

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



    public fun calculateSelect(query: (RootWrap<E, *>) -> ISugarQuerySelect<*>) {
        val selector = query.invoke(rootWrap as RootWrap<E, *>)
        if (criteria is CriteriaQuery<*>)
            (criteria as CriteriaQuery<Any>).select(selector.select)
        else if (criteria is CriteriaUpdate<*>) {
            ;//do nothing
        } else if (criteria is CriteriaDelete<*>) {
            ;//do nothing
        } else throw IllegalArgumentException();



    }
    fun calculateDelete(query: (RootWrap<E, E>) -> Unit) {
        val selector = query.invoke(rootWrap as RootWrap<E, E>)
        if (criteria is CriteriaQuery<*>)
            throw IllegalArgumentException();
        else if (criteria is CriteriaUpdate<*>) {
            throw IllegalArgumentException();
        } else if (criteria is CriteriaDelete<*>) {
            ;//do nothing
        } else throw IllegalArgumentException();
    }

    fun calculateUpdate(query: (RootWrapUpdate<E, E>) -> Unit) {
        val selector = query.invoke(rootWrap as RootWrapUpdate<E, E>)
        if (criteria is CriteriaQuery<*>)
            throw IllegalArgumentException();
        else if (criteria is CriteriaUpdate<*>) {
            //do nothing
        } else if (criteria is CriteriaDelete<*>) {
            throw IllegalArgumentException();
        } else throw IllegalArgumentException();
    }

    public fun calculateWhere(em: EntityManager): TypedQuery<*> {
        (criteria as CriteriaQuery<*>).where(getPredicate())
        if (orders.isNotEmpty())
            (criteria as CriteriaQuery<*>).orderBy(orders)
        val jpaQuery = em.createQuery(criteria)
        applyPage(jpaQuery);
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


    public fun calculateWhere(cq: CriteriaUpdate<*>) {
        cq.where(getPredicate())

    }

    public fun calculateWhere(cq: CriteriaDelete<*>) {
        cq.where(getPredicate())
    }

    fun forceNoResultsInQuery() {
        this.forceNoResultsInQuery = true;
    }

    fun  <RESULT> invokeSingle(query: (RootWrap<E, E>) -> ISugarQuerySelect<RESULT>): TypedQuery<RESULT> {
        val selector = query.invoke(rootWrap as RootWrap<E,E>)
        (criteria as CriteriaQuery<RESULT>).select(selector.select)
        return calculateWhere(em) as TypedQuery<RESULT>;
    }

}