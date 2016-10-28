package com.kameo.challenger.utils.odb.newapi

import com.kameo.challenger.domain.challenges.db.ChallengeParticipantODB
import com.kameo.challenger.domain.challenges.db.ChallengeStatus

import javax.persistence.criteria.*
import kotlin.reflect.KMutableProperty1


class JoinWrap<E,G> constructor(val pw: PathContext,
                                pathSelect: ISugarQuerySelect<G>,
                                root: Join<Any, E>

                                )

                              : PathWrap<E,G>(pw, pathSelect, root) {


    @Suppress("UNCHECKED_CAST")
    fun <F> join(sa: KMutableProperty1<E, F>): JoinWrap<F,G> {
        val join=(root as Join<Any,E>).join<E,F>(sa.name) as Join<Any,F>
        return JoinWrap(pw, pathSelect, join)
    }


    // perhaps we want to create here dedicated class
    fun <F> joinList(sa: KMutableProperty1<E, List<F>>): JoinWrap<F,G> {
        val join=(root as Join<Any,E>).join<E,F>(sa.name) as Join<Any,F>
        return JoinWrap(pw, pathSelect, join)
    }



    /* fun on(): PathWrap<E> {
         var list = mutableListOf<() -> Predicate?>();


         var pw = PathWrap(cb, root, list, this);


         pc.add({ calculateOr(list) });
         return pw;
     }

     private fun calculateOr(list: MutableList<() -> Predicate?>): Predicate? {
         var predicates = mutableListOf<Predicate>();
         for (p in list) {
             var pp: Predicate? = p.invoke();
             if (pp != null) {
                 predicates.add(pp);
             }
         };
         if (predicates.isNotEmpty()) {
             return (root as Join<Any,E>).on(*predicates.toTypedArray());
         }
         return null;
     }*/
}