package com.kameo.challenger.utils.odb

import com.kameo.challenger.domain.accounts.db.UserODB
import com.kameo.challenger.odb.api.IIdentity
import com.kameo.challenger.utils.odb.newapi.PathContext
import com.kameo.challenger.utils.odb.newapi.PathWrap
import com.kameo.challenger.utils.odb.newapi.RootWrap
import org.springframework.beans.factory.annotation.Autowired
import java.util.*
import javax.inject.Inject
import javax.persistence.EntityManager
import javax.persistence.PersistenceContext
import javax.persistence.TypedQuery
import javax.persistence.criteria.CriteriaBuilder
import javax.persistence.criteria.CriteriaQuery
import javax.persistence.criteria.Root
import kotlin.reflect.KClass

class AnyDAONew(@Inject val em: EntityManager) {


    fun <E:Any> getAll(clz: Class<E>, query: (RootWrap<E>) -> Unit, maxResults: Int? =null): List<E> {
        val cb = em.criteriaBuilder
        val criteria = cb.createQuery(clz)
        val root = criteria.from(clz)
        criteria.select(root)

        val jpaQuery = prepareQuery<E,E>(cb, criteria, maxResults, query, root)

        return jpaQuery.resultList
    }
    fun  <E> exists(clz: Class<E>, query: (RootWrap<E>) -> Unit):Boolean {

        val cb = em.criteriaBuilder
        val criteria = cb.createQuery(Long::class.java)
        val root = criteria.from(clz)
        criteria.select(cb.count(root))

        val jpaQuery = prepareQuery<E,Long>(cb, criteria, 1, query, root)



       // val jpaQuery = em.createQuery(criteria)
       // jpaQuery.setMaxResults(1)
        return jpaQuery.resultList.size==1;
    }

    private fun <E , F: Any> prepareQuery(cb: CriteriaBuilder, criteria: CriteriaQuery<F>, maxResults: Int?, query: (RootWrap<E>) -> Unit, root: Root<E>): TypedQuery<F> {
        val pc = PathContext(cb, criteria);
        val pw = RootWrap(pc, root, ArrayList())
        query.invoke(pw)
        criteria.where(pw.getPredicate())
        if (pc.orders.isNotEmpty())
            criteria.orderBy(pc.orders)
        val jpaQuery = em.createQuery(criteria)
        if (maxResults != null)
            jpaQuery.maxResults = maxResults;
        return jpaQuery
    }

    fun <E : Any> getAll(clz: KClass<E>, query: (RootWrap<E>) -> Unit, maxResults: Int? =null): List<E> {
        return getAll(clz.java, query, maxResults)
    }

    fun <E : Any> getFirst(clz: Class<E>, query: (RootWrap<E>) -> Unit): E? {
        var res = getAll(clz, query, 1);
        if (res.isEmpty() || res.size>1)
            return null
        return res.first()
    }
    fun <E : Any> getOne(clz: Class<E>, query: (RootWrap<E>) -> Unit): E {
       return getFirst(clz, query)?:throw IllegalArgumentException("Found 0 or ore than one results.")
    }

    fun <E : Any> getFirst(clz: KClass<E>, query: (RootWrap<E>) -> Unit): E? {
        return getFirst(clz.java, query)
    }
    fun <E : Any> getOne(clz: KClass<E>, query: (RootWrap<E>) -> Unit): E {
        return getOne(clz.java, query)
    }

    fun  <E: IIdentity> reload(e: E): E {
        return em.find(e.javaClass,e.id)
    }



    fun  <E :Any> exists(clz: KClass<E>, query: (RootWrap<E>) -> Unit):Boolean {
        return exists(clz.java, query);
    }
}


