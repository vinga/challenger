package com.kameo.challenger.utils.odb.newapi

import javax.persistence.criteria.CommonAbstractCriteria
import javax.persistence.criteria.CriteriaBuilder
import javax.persistence.criteria.Order


class PathContext constructor(val cb: CriteriaBuilder,
                              val criteria: CommonAbstractCriteria,

                              val orders: MutableList<Order> = mutableListOf(),

                              var skip: Int? = null,
                              var take: Int? = null
) {
    fun addOrder(o: Order) {
        orders.add(o);
    }


}