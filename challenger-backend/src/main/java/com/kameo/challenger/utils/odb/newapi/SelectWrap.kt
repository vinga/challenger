package com.kameo.challenger.utils.odb.newapi

import javax.persistence.criteria.Expression
import javax.persistence.criteria.Path

interface ISugarQuerySelect<E>  {
    val select: Expression<E>;
}
class SelectWrap<E> constructor ( override val select: Expression<E>): ISugarQuerySelect<E>
