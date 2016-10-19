package com.kameo.challenger.utils.odb

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

    fun <E> getOne(clz: Class<E>, query: (RootWrap<E>) -> Unit): E {
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

        return res.get(0);
    }

    fun <E : Any> getOne(clz: KClass<E>, query: (RootWrap<E>) -> Unit): E {
        return getOne(clz.java, query);
    }
}