package com.kameo.challenger.utils.odb

import com.kameo.challenger.odb.TaskODB
import com.kameo.challenger.odb.TaskODB_
import com.kameo.challenger.odb.UserODB
import com.kameo.challenger.utils.odb.newapi.KPro
import com.kameo.challenger.utils.odb.newapi.PathWrap
import com.kameo.challenger.utils.odb.newapi.foo
import org.springframework.beans.factory.annotation.Autowired
import java.util.*
import javax.persistence.EntityManager
import javax.persistence.PersistenceContext
import javax.persistence.criteria.CriteriaBuilder
import javax.persistence.criteria.Path
import javax.persistence.criteria.Predicate
import javax.persistence.criteria.Root

public class AnyDAONew {
    // @Autowired
    // Logger logger;

    @Autowired
    @PersistenceContext
    lateinit var em: EntityManager;




    fun test() {




        val
                all = getAll(TaskODB::class.java,
                {  (
                        it eqId 10)
                        .eqId(11);

                    it.get(TaskODB_.dueDate).eq(Date())

                    it.inIds(1L,2L)

             /*       it.eq2(KPro(TaskODB::id),"baa");
                    it.eq2(KPro(TaskODB::id),2L);
                    it.eq2(TaskODB::id.foo(),2L);
                    it.eq2(TaskODB::id.foo(),"AA");*/
                   // it.eq2(PathWrap.KPro(TaskODB::id),2L);
                    //it.eq2(UserODB::email,"bbb");
                    //it.eq2(UserODB::email,2);

                    it.newOr()
                            .inIds(1L)
                            .eq(TaskODB_.dueDate,Date());



                    it.get(TaskODB_.challenge).eqId(1L); }
                );

    }

            fun <E> getAll(clz: Class<E>, restr: (PathWrap<E>) -> Unit):List<E> {
        val cb = em.criteriaBuilder
        val criteria = cb.createQuery(clz)
        val root = criteria.from(clz)
        criteria.select(root)

        val pw=PathWrap(cb,root, ArrayList());
        restr.invoke(pw);
        criteria.where(pw.getPredicate());
       // restr.apply(RootWrap(cb,root));
      //  criteria.where(rest.apply(cb, criteria, root))

        val query = em.createQuery(criteria)
        return query.getResultList()
    }


    fun <E> getOne(clz: Class<E>, restr: (PathWrap<E>) -> Unit):E {
        val cb = em.criteriaBuilder
        val criteria = cb.createQuery(clz)
        val root = criteria.from(clz)
        criteria.select(root)

        val pw=PathWrap(cb,root, ArrayList());
        restr.invoke(pw);
        criteria.where(pw.getPredicate());

        val query = em.createQuery(criteria)
        query.setMaxResults(1);
        var res= query.getResultList();

        return res.get(0);
    }

}