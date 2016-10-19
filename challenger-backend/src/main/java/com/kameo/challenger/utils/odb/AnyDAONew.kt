package com.kameo.challenger.utils.odb

import com.kameo.challenger.odb.api.IIdentity
import com.kameo.challenger.utils.odb.newapi.PathWrap
import com.kameo.challenger.utils.odb.newapi.RootWrap
import org.springframework.beans.factory.annotation.Autowired
import java.util.*
import javax.inject.Inject
import javax.persistence.EntityManager
import javax.persistence.PersistenceContext
import kotlin.reflect.KClass

class AnyDAONew(@Inject val em: EntityManager) {


    fun <E> getAll(clz: Class<E>, query: (RootWrap<E>) -> Unit): List<E> {
        val cb = em.criteriaBuilder
        val criteria = cb.createQuery(clz)
        val root = criteria.from(clz)
        criteria.select(root)

        val pw = RootWrap(cb, root, ArrayList());
        query.invoke(pw);
        criteria.where(pw.getPredicate());

        val jpaQuery = em.createQuery(criteria)
        return jpaQuery.resultList
    }

    fun <E : Any> getAll(clz: KClass<E>, query: (RootWrap<E>) -> Unit): List<E> {
        return getAll(clz.java, query);
    }

    fun <E> getFirst(clz: Class<E>, query: (RootWrap<E>) -> Unit): E? {
        val cb = em.criteriaBuilder
        val criteria = cb.createQuery(clz)
        val root = criteria.from(clz)
        criteria.select(root)

        val pw = RootWrap(cb, root, ArrayList());
        query.invoke(pw);
        criteria.where(pw.getPredicate());

        val jpaQuery = em.createQuery(criteria)
        jpaQuery.setMaxResults(1);
        var res = jpaQuery.resultList
        if (res.isEmpty() || res.size>1)
            return null;
        return res.first();
    }
    fun <E> getOne(clz: Class<E>, query: (RootWrap<E>) -> Unit): E {
       return getFirst(clz, query)?:throw IllegalArgumentException("Found 0 or ore than one results.");
    }

    fun <E : Any> getFirst(clz: KClass<E>, query: (RootWrap<E>) -> Unit): E? {
        return getFirst(clz.java, query);
    }
    fun <E : Any> getOne(clz: KClass<E>, query: (RootWrap<E>) -> Unit): E {
        return getOne(clz.java, query);
    }

    fun  <E: IIdentity> reload(e: E): E {
        return em.find(e.javaClass,e.id)
    }
}


