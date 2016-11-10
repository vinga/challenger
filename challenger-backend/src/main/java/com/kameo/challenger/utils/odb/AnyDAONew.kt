package com.kameo.challenger.utils.odb

import com.kameo.challenger.domain.accounts.db.UserODB
import com.kameo.challenger.domain.tasks.db.TaskODB
import com.kameo.challenger.odb.api.IIdentity
import com.kameo.challenger.utils.odb.newapi.*
import javax.inject.Inject
import javax.persistence.EntityManager
import javax.persistence.Tuple
import javax.persistence.criteria.CriteriaBuilder
import javax.persistence.criteria.CriteriaQuery
import javax.persistence.criteria.Expression
import javax.persistence.criteria.Selection
import javax.swing.text.html.HTML
import kotlin.reflect.KClass


@Suppress("UNUSED_PARAMETER") // parameter resltClass is unused but needed for type safety
class AnyDAONew(@Inject val em: EntityManager) {

    class PathPairSelect<E, F>(val first: ISugarQuerySelect<E>, val second: ISugarQuerySelect<F>, val cb: CriteriaBuilder) : ISugarQuerySelect<Pair<E, F>> {
        override fun getSelection(): Selection<Tuple> {
            return cb.tuple(first.getSelection(), second.getSelection())
        }

        override fun isSingle(): Boolean = false
    }

    class PathTripleSelect<E, F, G>(val first: ISugarQuerySelect<E>, val second: ISugarQuerySelect<F>, val third: ISugarQuerySelect<G>, val cb: CriteriaBuilder) : ISugarQuerySelect<Triple<E, F, G>> {
        override fun getSelection(): Selection<Tuple> {
            return cb.tuple(first.getSelection(), second.getSelection(), third.getSelection())
        }

        override fun isSingle(): Boolean = false
    }

    fun test() {
        val ep = exists(UserODB::class.java, { it eqId 3 })
        val ep2 = exists(UserODB::class.java, { it eqId 300 })
        println(" $ep $ep2")




        val oneq: List<Pair<Long, UserODB>> = getAll(TaskODB::class) {
            it.get(TaskODB::user).get(+UserODB::login).lower().eq("12")
            it.get(TaskODB::user).get(+UserODB::login).lower().eq(it.get(TaskODB::label).lower())
            it.get(TaskODB::user).get(+UserODB::login).lower().eq(it.get(TaskODB::label))
            it.get(TaskODB::user).get(+UserODB::login).eq(it.get(TaskODB::label))



            it.get(TaskODB::label).lower() like "%ide%"

            it.newOr {
                it.newAnd { it.get(TaskODB::label).eq("hah") }
            }
            it.select(it.get(TaskODB::id), it.get(TaskODB::user))
        }

        oneq.map {
            println("FI " + it.first)
            println("SE " + it.second)
        }

        val one = getOne(TaskODB::class) {
            it.select(it.max(TaskODB::id))
        }
        val one2 = getFirst(TaskODB::class) {
            it eqId 10
        }
        println("Get max user id: " + one)

        val two = getOne(TaskODB::class) {
            it limit 1
            it.select(it.get(TaskODB::user))
        }

        println("TaskODB -> UserODB " + two)


        val three: List<UserODB> = getAll(TaskODB::class, {
            it limit 3
            it.select(it.get(TaskODB::user))
        })


        println("TaskODBs -> UserODBs " + three)

        val threep: List<Pair<TaskODB, UserODB>> = getAll(TaskODB::class, {
            it skip 4
            it limit 3
            it.select(it, it.get(TaskODB::user))


        })

        println("TaskODBs -> UserODBs " + threep)

        val four: TaskODB? = getFirst(TaskODB::class) {
            it eqId 8
            it limit 1
        }
        println("ONE HEH " + four)

        val users3: List<Long> = getAll(TaskODB::class) {
            it.select(it.max(TaskODB::id))
        }

        println("users3 " + users3)
        users3.forEach { println(" " + it) }


        update(TaskODB::class) {
            it.set(TaskODB::label, "new label")
            it eqId 1
        }

        val users: List<Pair<TaskODB, String>> = getAll(TaskODB::class, {
            it eqId 1
            it limit 100
            it.select(it, it get TaskODB::createdByUser get UserODB::email)
        })

        val users2: List<String> = getAll(TaskODB::class) {
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


    fun <T> merge(entity: T):T {
       return em.merge(entity)
    }
    fun <T> remove(entity: T):T {
        em.remove(entity)
        return entity
    }
    fun persist(entity: Any) {
        em.persist(entity)
    }
    fun <E : Any> find(clz: KClass<E>, primaryKey: Any): E {
        return em.find(clz.java, primaryKey);
    }


    fun <E : Any, RESULT : Any> getAll(clz: Class<E>, resultClass: Class<RESULT>, query: (RootWrap<E, E>) -> (ISugarQuerySelect<RESULT>)): List<RESULT> {
        val pc = QueryPathContext<RESULT>(clz, em)
        val res = pc.invokeQuery(query).resultList
        return pc.mapToPluralsIfNeeded<RESULT>(res)
    }

    fun <E : Any, RESULT : Any> getOne(clz: Class<E>, resultClass: Class<RESULT>, query: (RootWrap<E, E>) -> (ISugarQuerySelect<RESULT>)): RESULT {
        val pc = QueryPathContext<RESULT>(clz, em)
        val jpaQuery = pc.invokeQuery(query)
        jpaQuery.maxResults = 1
        return ensureIsOnlyOne(pc.mapToPluralsIfNeeded<RESULT>(jpaQuery.resultList))
    }



    fun <E : Any, RESULT : Any> getFirst(clz: Class<E>, resultClass: Class<RESULT>, query: (RootWrap<E, E>) -> (ISugarQuerySelect<RESULT>)): RESULT? {
        val pc = QueryPathContext<RESULT>(clz, em)
        val jpaQuery = pc.invokeQuery(query)
        jpaQuery.maxResults = 1
        return pc.mapToPluralsIfNeeded<RESULT>(jpaQuery.resultList).firstOrNull()
    }


    fun <E : Any> update(clz: Class<E>, query: (RootWrapUpdate<E, E>) -> Unit): Int {
        val pc = UpdatePathContext<E>(clz, em)
        return pc.invokeUpdate(query).executeUpdate()
    }

    fun <E : Any> remove(clz: Class<E>, query: (RootWrap<E, E>) -> Unit): Int {
        val pc = DeletePathContext<E>(clz, em)
        return pc.invokeDelete(query).executeUpdate()
    }

    fun <E : Any> exists(clz: Class<E>, query: (RootWrap<E, E>) -> ISugarQuerySelect<*>): Boolean {
        val queryExists: (RootWrap<E, E>) -> ISugarQuerySelect<Long> = {
            val invoke: ISugarQuerySelect<*> = query.invoke(it)
            it.select(ExpressionWrap(it.pc,em.criteriaBuilder.count(invoke.getSelection() as Expression<*>)))
        }
        return getOne(clz, Long::class.java, queryExists) > 0
    }


    fun <E : Any> ensureIsOnlyOne(res: List<E>): E {
        if (res.isEmpty() || res.size > 1)
            throw IllegalArgumentException("Expected exactly 1 result but query returned ${res.size} results.")
        return res.first()
    }


    fun <E : IIdentity> reload(e: E): E {
        return em.find(e.javaClass, e.id)
    }

    fun <E : Any> exists(clz: KClass<E>, query: (RootWrap<E, E>) -> ISugarQuerySelect<*>): Boolean {
        return exists(clz.java, query)
    }


    inline fun <E : Any, reified RESULT : Any> getOne(clz: KClass<E>, noinline query: (RootWrap<E, E>) -> (ISugarQuerySelect<RESULT>)): RESULT {
        return getOne(clz.java, RESULT::class.java, query)
    }

    inline fun <E : Any, reified RESULT : Any> getFirst(clz: KClass<E>, noinline query: (RootWrap<E, E>) -> (ISugarQuerySelect<RESULT>)): RESULT? {
        return getFirst(clz.java, RESULT::class.java, query)
    }

    inline fun <E : Any, reified RESULT : Any> getAll(clz: KClass<E>, noinline query: (RootWrap<E, E>) -> (ISugarQuerySelect<RESULT>)): List<RESULT> {
        return getAll(clz.java, RESULT::class.java, query)
    }
    inline fun <E : Any, reified RESULT : Any> getAllMutable(clz: KClass<E>, noinline query: (RootWrap<E, E>) -> (ISugarQuerySelect<RESULT>)): MutableList<RESULT> {
        return getAll(clz.java, RESULT::class.java, query) as MutableList<RESULT>
    }
  /*
    Example usage: /Function Literals with Receiver/
    fun boo() {
        getAll2(TaskODB::class) {
            eqId(10)
            get(TaskODB::label) like "ha ha"
            select(+TaskODB::label)
        }
    }*/
    inline fun <E : Any, reified RESULT : Any> getAll2(clz: KClass<E>, noinline query: RootWrap<E, E>.() -> (ISugarQuerySelect<RESULT>)): List<RESULT> {
        return getAll(clz.java, RESULT::class.java, query)
    }


    fun <E : Any> remove(clz: KClass<E>, query: (RootWrap<E, E>) -> Unit): Int {
        return remove(clz.java, query)
    }

    /**
     * Works with updatable=false fields
     */
    fun <E : Any> update(clz: KClass<E>, query: (RootWrapUpdate<E, E>) -> Unit): Int {
        return update(clz.java, query)
    }

}


