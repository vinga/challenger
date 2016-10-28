package com.kameo.challenger.utils.odb

import com.kameo.challenger.domain.accounts.db.UserODB
import com.kameo.challenger.domain.challenges.db.ChallengeODB
import com.kameo.challenger.domain.tasks.db.TaskODB
import com.kameo.challenger.odb.api.IIdentity
import com.kameo.challenger.utils.odb.newapi.*
import javax.inject.Inject
import javax.persistence.EntityManager
import javax.persistence.Tuple
import javax.persistence.TypedQuery
import javax.persistence.criteria.Path
import kotlin.reflect.KClass


interface ISugarQuery<E, F> {
    fun query(it: RootWrap<E, E>): (ISugarQuerySelect<F>?)
}


class AnyDAONew(@Inject val em: EntityManager) {

    class PathPairSelect<E, F>(val first: ISugarQuerySelect<E>, val second: ISugarQuerySelect<F>)
    class PathTripleSelect<E, F, G>(val first: ISugarQuerySelect<E>, val second: ISugarQuerySelect<F>, val third: ISugarQuerySelect<G>)


    fun test() {
        val res = getAll(TaskODB::class, {
            it.eqId(12L)
            it.get(TaskODB::user).eqId(8)


            val or = it.newOr()
            or.get(TaskODB::user).eqId(10)
            or.ref(it.like(TaskODB::label, "A"))
            val user = or.get(TaskODB::user)


            val or2 = it.newAnd()
            or2.get(TaskODB::user).eqId(100)
            or2.get(TaskODB::user).eqId(200)
            or2.ref(it.like(TaskODB::label, "A"))
            or2.ref(it.like(TaskODB::label, "B"))
            val or3 = it.newOr()
                    .ref(user).get(UserODB::email).like("aa")
                    .ref(user).get(UserODB::login).like("hehe")
                    .ref(it.get(TaskODB::challenge).get(ChallengeODB::label).like("FF"))
                    .ref(it.get(TaskODB::challenge)).eqId(5)
                    .ref(it).eqId(5)

                    .ref(it.get(TaskODB::user), {
                        it.get(UserODB::login).like("login")
                        it.get(UserODB::email).like("email")
                    })
                    .finish()



            or3.get(TaskODB::challenge) eqId 4
            or3.get(TaskODB::challenge) eqId 5


            it.newAnd({
                it.eqId(99)
                user.eq(UserODB::email, "AA")
            })
            or2.finish()
            or.finish()


        })
        println("SUCCESS.. " + res)

        val one = getOne(TaskODB::class.java) {
            it.select(it.max(TaskODB::id))
        }

        println("Get max user id: " + one)

        val two = getOne(TaskODB::class.java) {
            it limit 1
            it.select(it.get(TaskODB::user))
        }

        println("TaskODB -> UserODB " + two)


        val three: List<UserODB> = getAllSingles(TaskODB::class, {
            it limit 3
            it.select(it.get(TaskODB::user))
        })

        println("TaskODBs -> UserODBs " + three)

        val threep: List<Pair<TaskODB,UserODB>> = getAllPairs(TaskODB::class, {
            it skip 4
            it limit 3
            it.select(it,it.get(TaskODB::user))
        })

        println("TaskODBs -> UserODBs " + threep)

        val four: TaskODB? = getFirst(TaskODB::class) {
            it eqId 8
            it limit 1
        }
        println("ONE HEH " + four)
        val four2: TaskODB = getOne(TaskODB::class.java) {
            it eqId 8
            it limit 1
        }

        println("ONE HEH " + four2)

        val users3: List<Long> = getAllSingles(TaskODB::class) {
            it.select(it.max(TaskODB::id))
        }

        println("users3 " + users3)
        users3.forEach { println(" " + it) }


        update(TaskODB::class) {
            it.set(TaskODB::label, "new label")
            it eqId 1
        }

        val users: List<Pair<TaskODB, String>> = getAllPairs(TaskODB::class.java, {
            it eqId 1
            it limit 100
            it.select(it, it get TaskODB::createdByUser get UserODB::email)
        })

        val users2: List<String> = getAllSingles(TaskODB::class) {
            it eqId 1
            it limit 100
            it.select(it get TaskODB::createdByUser get UserODB::email)
        }

        for (p in users) {
            println("FIRST " + p.first)
            println("SECOND " + p.second)
        }

        for (p in users2) {
            println("EMAIL " + p)
        }
    }



    fun <E : Any> getAll(clz: Class<E>, query: (RootWrap<E, E>) -> ISugarQuerySelect<E>): List<E> {
        var pc = PathContext.createSelectQuery(clz, clz, em);
        return pc.invokeSingle(query).resultList
    }

    fun <E : Any, RESULT : Any> getAllSingles(clz: Class<E>, resultClass: Class<RESULT>, query: (RootWrap<E, E>) -> (ISugarQuerySelect<RESULT>)): List<RESULT> {
        var pc = PathContext.createSelectQuery(clz, resultClass, em);
        return pc.invokeSingle(query).resultList
    }


    fun <E : Any, F : Any, G : Any> getAllPairs(clz: Class<E>, query: (RootWrap<E, *>) -> (PathPairSelect<F, G>)): List<Pair<F, G>> {
        val criteria = em.criteriaBuilder.createTupleQuery()
        val pc = PathContext(clz, em, criteria);
        val selector = query.invoke(pc.rootWrap as RootWrap<E, *>)

        criteria.select(em.criteriaBuilder.tuple(selector.first.select, selector.second.select))
        val jpaQuery = pc.calculateWhere(em) as TypedQuery<Tuple>
        return jpaQuery.resultList.map { Pair(it.get(0) as F, it.get(1) as G) }
    }


    fun <E : Any, F : Any, G : Any, H : Any>
            getAllTriples(clz: Class<E>, query: (RootWrap<E, *>) -> (PathTripleSelect<F, G, H>)): List<Triple<F, G, H>> {
        val criteria = em.criteriaBuilder.createTupleQuery()
        val pc = PathContext(clz, em, criteria)
        val selector = query.invoke(pc.rootWrap as RootWrap<E, *>)
        criteria.select(em.criteriaBuilder.tuple(selector.first.select, selector.second.select, selector.third.select))

        val jpaQuery = pc.calculateWhere(em) as TypedQuery<Tuple>

        return jpaQuery.resultList.map { Triple(it.get(0) as F, it.get(1) as G, it.get(2) as H) }
    }




    fun <E : Any> update(clz: Class<E>, query: (RootWrapUpdate<E, E>) -> Unit): Int {
        val criteria = em.criteriaBuilder.createCriteriaUpdate(clz)
        val pc = PathContext(clz, em, criteria);
        pc.calculateUpdate(query);
        pc.calculateWhere(criteria);
        return em.createQuery(criteria).executeUpdate();

    }


    fun <E : Any> remove(clz: Class<E>, query: (RootWrap<E, E>) -> Unit): Int {
        val criteria = em.criteriaBuilder.createCriteriaDelete(clz)
        val pc = PathContext(clz, em, criteria);
        pc.calculateDelete(query);
        pc.calculateWhere(criteria);
        return em.createQuery(criteria).executeUpdate();
    }

    fun <E> exists(clz: Class<E>, query: (RootWrap<E, Long>) -> ISugarQuerySelect<Long>): Boolean {
        val criteria = em.criteriaBuilder.createQuery(Long::class.java)
        val pc = PathContext(clz, em, criteria)
        pc.calculateSelect(query as (RootWrap<E, *>) -> ISugarQuerySelect<*>)
        criteria.select(em.criteriaBuilder.count(pc.root))
        val jpaQuery = pc.calculateWhere(em) as TypedQuery<Long>
        jpaQuery.maxResults = 1
        return jpaQuery.resultList.size == 1
    }





    fun <E : Any, F : Any> getOne(clz: Class<E>, query: ISugarQuery<E, F>, outClz: Class<F>): F {
        val criteria = em.criteriaBuilder.createQuery(outClz)

        val pc = PathContext(clz, em, criteria);
        val root = pc.root;
        val pw = RootWrap(pc, SelectWrap(root), root)
        val selector = query.query(pw as RootWrap<E, E>)

        if (selector != null)
            criteria.select(selector.select)
        else
            criteria.select(pw.root as Path<F>)

        val jpaQuery = pc.calculateWhere(em) as TypedQuery<F>


        return ensureIsOnlyOne(jpaQuery.resultList as List<F>);
    }

    inline fun <E : Any, reified F : Any> getOne(clz: Class<E>, query: (RootWrap<E, E>) -> (ISugarQuerySelect<F>?)): F {
        val criteria = em.criteriaBuilder.createQuery(F::class.java)
        val pc = PathContext(clz, em, criteria)
        val selector = query.invoke(pc.rootWrap as RootWrap<E, E>)
        if (selector != null)
            criteria.select(selector.select)
        else
            criteria.select(pc.rootWrap.root as Path<F>)
        val jpaQuery = pc.calculateWhere(em) as TypedQuery<F>


        return ensureIsOnlyOne(jpaQuery.resultList as List<F>)
    }



     fun < E : Any, F : Any> getFirst(clz: Class<E>, resultClz: Class<F>, query: (RootWrap<E, E>) -> (ISugarQuerySelect<F>?)): F? {
        val criteria = em.criteriaBuilder.createQuery(resultClz)

        val pc = PathContext(clz, em, criteria);

        val selector = query.invoke(pc.rootWrap as RootWrap<E, E>)
        if (selector != null)
            criteria.select(selector.select)
        else
            criteria.select(pc.rootWrap.root as Path<F>)

        val jpaQuery = pc.calculateWhere(em) as TypedQuery<F>

        jpaQuery.maxResults = 1
        var res = jpaQuery.resultList
        if (res.isEmpty() || res.size > 1)
            return null
        return res.first()

    }

    fun <E : Any> ensureIsOnlyOne(res: List<E>): E {
        if (res.isEmpty() || res.size > 1)
            throw IllegalArgumentException("Expected exactly 1 result but query returned ${res.size} results.")
        return res.first()
    }


    fun <E : IIdentity> reload(e: E): E {
        return em.find(e.javaClass, e.id)
    }
    fun <E : Any> exists(clz: KClass<E>, query: (RootWrap<E, Long>) -> ISugarQuerySelect<Long>): Boolean {
        return exists(clz.java, query)
    }

    fun <E : Any> getAll(clz: KClass<E>, query: (RootWrap<E, E>) -> ISugarQuerySelect<E>): List<E> {
        return getAll(clz.java, query)
    }
    inline fun <E : Any, reified F : Any> getOne(clz: KClass<E>, query: (RootWrap<E, E>) -> (ISugarQuerySelect<F>?)): F {
        return getOne(clz.java, query)
    }
    inline fun <reified E : Any, reified F : Any> getFirst(clz: KClass<E>, noinline query: (RootWrap<E, E>) -> (ISugarQuerySelect<F>?)): F? {
        return getFirst(clz.java, F::class.java, query)
    }

    inline fun <E : Any, reified RESULT : Any> getAllSingles(clz: KClass<E>, noinline query: (RootWrap<E, E>) -> (ISugarQuerySelect<RESULT>)): List<RESULT> {
        return getAllSingles(clz.java, RESULT::class.java, query)
    }

    inline fun <E : Any, reified F : Any, reified G : Any> getAllPairs(clz: KClass<E>, noinline query: (RootWrap<E, *>) -> (PathPairSelect<F, G>)): List<Pair<F, G>> {
        return getAllPairs(clz.java, query)
    }

    inline fun <E : Any, reified F : Any, reified G : Any, reified H : Any>
            getAllTriples(clz: KClass<E>, noinline query: (RootWrap<E, *>) -> (PathTripleSelect<F, G, H>)): List<Triple<F, G, H>> {
        return getAllTriples(clz.java, query)
    }
    fun <E : Any> remove(clz: KClass<E>, query: (RootWrap<E, E>) -> Unit): Int {
        return remove(clz.java, query);
    }
    /**
     * Works with updatable=false fields
     */
    fun <E : Any> update(clz: KClass<E>, query: (RootWrapUpdate<E, E>) -> Unit): Int {
        return update(clz.java, query);
    }

}


