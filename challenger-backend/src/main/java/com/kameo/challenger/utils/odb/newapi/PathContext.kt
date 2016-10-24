package com.kameo.challenger.utils.odb.newapi

import javax.persistence.criteria.CriteriaBuilder
import javax.persistence.criteria.CriteriaQuery
import javax.persistence.criteria.Order

class PathContext constructor(val cb: CriteriaBuilder,
                              val criteria: CriteriaQuery<*>,
                              val orders: MutableList<Order>  = mutableListOf()) {
    internal fun addOrder(o: Order) {
        orders.add(o);
    }
}