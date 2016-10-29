package com.kameo.challenger.utils.odb.newapi

import javax.persistence.EntityManager
import javax.persistence.Query
import javax.persistence.TypedQuery
import javax.persistence.criteria.*


class PathContext<E> constructor(clz: Class<*>,
                                 val em: EntityManager,

                                 val criteria: CommonAbstractCriteria

) {
    val cb: CriteriaBuilder = em.criteriaBuilder;

    companion object {
        fun <E, G> createSelectQuery(clz: Class<E>, resultClass: Class<G>, em: EntityManager): PathContext<E> {
            val cb = em.criteriaBuilder;
            val pc = PathContext<E>(clz, em, cb.createQuery(resultClass));
            return pc;
        }
    }

    var currentArray: MutableList<() -> Predicate?> = mutableListOf();
        private set

    val orders: MutableList<Order> = mutableListOf()
    val arraysStack: MutableList<MutableList<() -> Predicate?>> = mutableListOf();

    var forceNoResultsInQuery: Boolean = false;

    var skip: Int? = null;

    var take: Int? = null;


    lateinit var root: Root<Any>;
        private set
    lateinit var rootWrap: PathWrap<Any, Any>
        private set
    var defaultSelection: ISugarQuerySelect<Any>?=null
        private set

    init {
        if (criteria is CriteriaQuery<*>) {
            root = criteria.from(clz as Class<Any>);

            defaultSelection=SelectWrap(root)

            rootWrap = RootWrap<Any,Any>(this as PathContext<Any>, root)
        } else if (criteria is CriteriaUpdate<*>) {
            root = (criteria as CriteriaUpdate<Any>).from(clz as Class<Any>);
            rootWrap = RootWrapUpdate<Any,Any>(this as PathContext<Any>, root)
        } else if (criteria is CriteriaDelete<*>) {
            root = (criteria as CriteriaDelete<Any>).from(clz as Class<Any>);
            rootWrap = RootWrap<Any,Any>(this as PathContext<Any>, root)
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
            (criteria as CriteriaQuery<Any>).select(selector.getSelection())
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

    var selector: ISugarQuerySelect<*>? = null;


    fun invokeUpdate(query: (RootWrapUpdate<E, E>) -> Unit): Query {
        if (criteria is CriteriaUpdate<*>) {
            query.invoke(rootWrap as RootWrapUpdate<E, E>)
            calculateWhere(criteria)
            return em.createQuery(criteria as CriteriaUpdate<*>)
        } else throw IllegalArgumentException();
    }

    fun invokeDelete(query: (RootWrap<E, E>) -> Unit): Query {
        query.invoke(rootWrap as RootWrap<E, E>)
        if (criteria is CriteriaDelete<*>) {
            calculateWhere(criteria)
            return em.createQuery(criteria as CriteriaDelete<*>)
        } else throw IllegalArgumentException();


    }


    fun <RESULT> invokeQuery(query: (RootWrap<E, E>) -> ISugarQuerySelect<RESULT>): TypedQuery<RESULT> {
        if (criteria is CriteriaQuery<*>) {
            selector = query.invoke(rootWrap as RootWrap<E, E>)
            var sell = selector!!.getSelection();
            (criteria as CriteriaQuery<RESULT>).select(sell as Selection<out RESULT>)
            return calculateWhere(em) as TypedQuery<RESULT>
        } else throw IllegalArgumentException();
    }

    fun <RESULT : Any> mapToPluralsIfNeeded(res: List<RESULT>): List<RESULT> {
        if (res.isNotEmpty()) {
            if (!selector!!.isSingle()) {
                if (res.first() is Array<*>) {

                    var rows = res as List<Array<Object>>;
                    var row = rows.first();
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
        return res;
    }


}