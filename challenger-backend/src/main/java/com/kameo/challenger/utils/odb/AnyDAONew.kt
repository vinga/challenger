package com.kameo.challenger.utils.odb

import com.kameo.challenger.domain.accounts.db.UserODB
import com.kameo.challenger.domain.tasks.db.TaskODB
import com.kameo.challenger.odb.api.IIdentity
import com.kameo.challenger.utils.odb.newapi.*
import java.util.*
import javax.inject.Inject
import javax.persistence.EntityManager
import javax.persistence.TypedQuery
import javax.persistence.criteria.*
import kotlin.reflect.KClass

class AnyDAONew(@Inject val em: EntityManager) {

    class PathPairSelect<E, F>(val first: ISelectWrap<E>, val second: ISelectWrap<F>);
    class PathTripleSelect<E, F, G>(val first: ISelectWrap<E>, val second: ISelectWrap<F>, val third: ISelectWrap<G>);



    fun test() {


      var one=getOne(TaskODB::class.java, {

            it.select(it.max(TaskODB::id))
        });

        println("ONE HEHAAAAAAAAAAAAAA "+one);

       var two=getOne(TaskODB::class.java, {
           it limit 1
            it.select(it.get(TaskODB::user))
        });

        println("ONE HE................................ "+two);


        var three:UserODB=getOne(TaskODB::class.java, {

            it eqId 1

            it limit 1

            //it.select(it eqId  8)
           // it.select(it.get(TaskODB::user))
            it.select(it.get(TaskODB::user));
        });

        println("ONE HEH itqiii "+three);

if (true)
    return;
        var four:TaskODB=getOne(TaskODB::class.java, {
            it eqId 8
            it limit 1
            //var itt:RootWrap<TaskODB,Any> = it;

            //var ps: IPathSelect<TaskODB> = it;
            //it.select(it eqId  8)

        });

        var four2:TaskODB=getOne(TaskODB::class.java, {

            it eqId 8
            it limit 1
            //var itt:RootWrap<TaskODB,Any> = it;

            //var ps: IPathSelect<TaskODB> = it;
            //it.select(it eqId  8)

        });

        println("ONE HEH "+four);


        var users3: List<Long> = getAllSingles(TaskODB::class.java, {

            it.select(it.max(TaskODB::id))
        });

        println("users3 "+users3);
        users3.forEach { println( " "+it); }
        if (true)
            return;

        update(TaskODB::class, {
            it.set(TaskODB::label, "new label")
            it eqId 1
        });

        var users: List<Pair<TaskODB, String>> = getAllPairs(TaskODB::class.java, {
            it eqId 1

            it limit 100
            it.select(it, it get TaskODB::createdByUser get UserODB::email)
        });

        var users2: List<String> = getAllSingles(TaskODB::class.java, {
            it eqId 1

            it limit 100

            it.select(it get TaskODB::createdByUser get UserODB::email)
        });



        for (p in users) {
            println("FIRST " + p.first)
            println("SECOND " + p.second)
        }

        for (p in users2) {
            println("EMAIL " + p)
        }
    }

    inline fun <E : Any, reified F : Any> getAllSingles(clz: KClass<E>, query: (RootWrap<E,F>) -> (SelectWrap<F>)): List<F> {
        return getAllSingles(clz.java, query);
    }

    inline fun <E : Any, reified F : Any> getAllSingles(clz: Class<E>, query: (RootWrap<E,F>) -> (ISelectWrap<F>)): List<F> {
        val cb = em.criteriaBuilder
        val criteria = cb.createQuery(F::class.java)
        val root = criteria.from(clz)
        val pc = PathContext(cb, criteria);
        val pw = RootWrap(pc, SelectWrap(root) as SelectWrap<F>, root, ArrayList())
        val selector = query.invoke(pw)

        criteria.select(selector.select)

        criteria.where(pw.getPredicate())
        if (pc.orders.isNotEmpty())
            criteria.orderBy(pc.orders)
        val jpaQuery = em.createQuery(criteria)

        applyPage(jpaQuery, pc);


        return jpaQuery.resultList;
    }




    public fun applyPage(jpaQuery: TypedQuery<*>, pc: PathContext) {
        val skip = pc.skip;
        if (skip != null)
            jpaQuery.firstResult = skip;
        val take = pc.take;
        if (take != null)
            jpaQuery.maxResults = take;
    }


    inline fun <E : Any, reified F : Any, reified G : Any> getAllPairs(clz: KClass<E>, query: (RootWrap<E,*>) -> (PathPairSelect<F, G>)): List<Pair<F, G>> {
        return getAllPairs(clz.java, query);
    }

    inline fun <E : Any, reified F : Any, reified G : Any> getAllPairs(clz: Class<E>, query: (RootWrap<E,*>) -> (PathPairSelect<F, G>)): List<Pair<F, G>> {
        val cb = em.criteriaBuilder
        val criteria = cb.createTupleQuery();
        val root = criteria.from(clz)
        val pc = PathContext(cb, criteria);
        val pw = RootWrap(pc, SelectWrap(root), root, ArrayList())
        val selector = query.invoke(pw)

        criteria.select(cb.tuple(selector.first.select, selector.second.select))

        criteria.where(pw.getPredicate())
        if (pc.orders.isNotEmpty())
            criteria.orderBy(pc.orders)
        val jpaQuery = em.createQuery(criteria)
        applyPage(jpaQuery, pc);

        return jpaQuery.resultList.map { Pair(it.get(0) as F, it.get(1) as G) }
    }

    inline fun <E : Any, reified F : Any, reified G : Any, reified H : Any>
            getAllTriples(clz: KClass<E>, query: (RootWrap<E,*>) -> (PathTripleSelect<F, G, H>)): List<Triple<F, G, H>> {
        return getAllTriples(clz.java, query);

    }

    inline fun <E : Any, reified F : Any, reified G : Any, reified H : Any>

            getAllTriples(clz: Class<E>, query: (RootWrap<E,*>) -> (PathTripleSelect<F, G, H>)): List<Triple<F, G, H>> {
        val cb = em.criteriaBuilder
        val criteria = cb.createTupleQuery();
        val root = criteria.from(clz)
        val pc = PathContext(cb, criteria);
        val pw = RootWrap(pc,SelectWrap(root), root, ArrayList())
        val selector = query.invoke(pw)

        criteria.select(cb.tuple(selector.first.select, selector.second.select, selector.third.select))

        criteria.where(pw.getPredicate())
        if (pc.orders.isNotEmpty())
            criteria.orderBy(pc.orders)
        val jpaQuery = em.createQuery(criteria)
        applyPage(jpaQuery, pc);

        return jpaQuery.resultList.map { Triple(it.get(0) as F, it.get(1) as G, it.get(2) as H) }
    }

    fun <E : Any> getAll(clz: Class<E>, query: (RootWrap<E,E>) -> ISelectWrap<E>): List<E> {
        val cb = em.criteriaBuilder
        val criteria = cb.createQuery(clz)
        val root = criteria.from(clz)
        criteria.select(root)

        val jpaQuery = prepareQuery<E, E>(cb, criteria, query, root)

        return jpaQuery.resultList
    }


    private fun <E, F : Any> prepareQuery(cb: CriteriaBuilder, criteria: CriteriaQuery<F>, query: (RootWrap<E,F>) -> ISelectWrap<F>, root: Root<E>): TypedQuery<F> {
        val pc = PathContext(cb, criteria);
        val pw = RootWrap(pc,SelectWrap(root) as SelectWrap<F>, root, ArrayList())
        query.invoke(pw)


        criteria.where(pw.getPredicate())
        if (pc.orders.isNotEmpty())
            criteria.orderBy(pc.orders)
        val jpaQuery = em.createQuery(criteria)
        applyPage(jpaQuery, pc);
        return jpaQuery
    }

    fun <E : Any> remove(clz: KClass<E>, query: (RootWrap<E,E>) -> Unit): Int {
        return remove(clz.java, query);
    }

    /**
     * Works with updatable=false fields
     */
    fun <E : Any> update(clz: KClass<E>, query: (RootWrapUpdate<E,E>) -> Unit): Int {
        return update(clz.java, query);
    }

    fun <E : Any> update(clz: Class<E>, query: (RootWrapUpdate<E,E>) -> Unit): Int {
        val cb = em.criteriaBuilder
        val criteria = cb.createCriteriaUpdate(clz)
        val root = criteria.from(clz)
        //criteria.select(root)
        val result = prepareAndExecuteUpdateQuery<E, E>(cb, criteria, query, root)
        return result
    }

    private fun <E, F : Any> prepareAndExecuteUpdateQuery(cb: CriteriaBuilder, criteria: CriteriaUpdate<F>, query: (RootWrapUpdate<E,F>) -> Unit, root: Root<E>):
            Int {
        val pc = PathContext(cb, criteria);
        val pw = RootWrapUpdate(pc,SelectWrap(root) as SelectWrap<F>, root, ArrayList())
        query.invoke(pw)
        criteria.where(pw.getPredicate())
        val jpaQuery = em.createQuery(criteria)
        return jpaQuery.executeUpdate();
    }


    fun <E : Any> remove(clz: Class<E>, query: (RootWrap<E,E>) -> Unit): Int {
        val cb = em.criteriaBuilder
        val criteria = cb.createCriteriaDelete(clz)
        val root = criteria.from(clz)
        //criteria.select(root)
        val result = prepareAndExecuteDeleteQuery<E,E>(cb, criteria, query, root)
        return result
    }

    fun <E> exists(clz: Class<E>, query: (RootWrap<E,Long>) -> ISelectWrap<Long>): Boolean {

        val cb = em.criteriaBuilder
        val criteria = cb.createQuery(Long::class.java)
        val root = criteria.from(clz)
        criteria.select(cb.count(root))

        val jpaQuery = prepareQuery<E, Long>(cb, criteria, query, root);
        jpaQuery.maxResults = 1;

        return jpaQuery.resultList.size == 1;
    }


    private fun <E,F> prepareAndExecuteDeleteQuery(cb: CriteriaBuilder, criteria: CriteriaDelete<F>, query: (RootWrap<E,E>) -> Unit, root: Root<E>):
            Int {
        val pc = PathContext(cb, criteria);
        val pw = RootWrap(pc,SelectWrap(root), root, ArrayList())
        query.invoke(pw)
        criteria.where(pw.getPredicate())

        val jpaQuery = em.createQuery(criteria)
        return jpaQuery.executeUpdate();
    }

    fun <E : Any> getAll(clz: KClass<E>, query: (RootWrap<E,E>) -> ISelectWrap<E>): List<E> {
        return getAll(clz.java, query)
    }



    inline fun <E : Any, reified F : Any> getOne(clz: KClass<E>, query: (RootWrap<E,E>) -> (ISelectWrap<F>?)): F {
        return getOne(clz.java,query);
    }

    inline fun <E : Any, reified F : Any> getOne(clz: Class<E>, query: (RootWrap<E,E>) -> (ISelectWrap<F>?)): F {
        val cb = em.criteriaBuilder
        val criteria = cb.createQuery(F::class.java)
        var root=criteria.from(clz);
        val pc = PathContext(cb, criteria);
        val pw = RootWrap(pc,SelectWrap(root), root, ArrayList())
        val selector = query.invoke(pw as RootWrap<E, E>)

        if (selector!=null )
            criteria.select(selector.select)
        else
            criteria.select(pw.root as Path<F>)

            criteria.where(pw.getPredicate())
        if (pc.orders.isNotEmpty())
            criteria.orderBy(pc.orders)
        val jpaQuery = em.createQuery(criteria)
        applyPage(jpaQuery, pc);


        return ensureIsOnlyOne(jpaQuery.resultList as List<F>);
    }
    inline fun <E : Any, reified F : Any> getFirst(clz: KClass<E>, query: (RootWrap<E,E>) -> (ISelectWrap<F>?)): F? {
        return getFirst(clz.java,query);
    }
    inline fun <E : Any, reified F : Any> getFirst(clz: Class<E>, query: (RootWrap<E,E>) -> (ISelectWrap<F>?)): F? {
        val cb = em.criteriaBuilder
        val criteria = cb.createQuery(F::class.java)
        var root=criteria.from(clz);
        val pc = PathContext(cb, criteria);
        val pw = RootWrap(pc,SelectWrap(root), root, ArrayList())
        val selector = query.invoke(pw as RootWrap<E, E>)

        if (selector!=null )
            criteria.select(selector.select)
        else
            criteria.select(pw.root as Path<F>)

        criteria.where(pw.getPredicate())
        if (pc.orders.isNotEmpty())
            criteria.orderBy(pc.orders)
        val jpaQuery = em.createQuery(criteria)
        applyPage(jpaQuery, pc);

        jpaQuery.maxResults=1;
        var res=jpaQuery.resultList;
        if (res.isEmpty() || res.size > 1)
            return null
        return res.first()

    }
/*    fun <E : Any> getFirst(clz: Class<E>, query: (RootWrap<E,E>) -> ISelectWrap<E>): E? {
        val queryLimitWrapper: (RootWrap<E,E>) -> ISelectWrap<E> = {
            query.invoke(it)
            it limit 1
        }
        var res = getAll(clz, queryLimitWrapper);
        if (res.isEmpty() || res.size > 1)
            return null
        return res.first()
    }*/
/*
    fun <E : Any> getFirst(clz: KClass<E>, query: (RootWrap<E,E>) -> ISelectWrap<E>): E? {
        return getFirst(clz.java, query)
    }*/

    fun <E : Any> ensureIsOnlyOne(res: List<E>): E {
        if (res.isEmpty() || res.size > 1)
            throw IllegalArgumentException("Expected exactly 1 result but query returned ${res.size} results.");
        return res.first()
    }


    fun <E : IIdentity> reload(e: E): E {
        return em.find(e.javaClass, e.id)
    }


    fun <E : Any> exists(clz: KClass<E>, query: (RootWrap<E,Long>) -> ISelectWrap<Long>): Boolean {
        return exists(clz.java, query);
    }
}


