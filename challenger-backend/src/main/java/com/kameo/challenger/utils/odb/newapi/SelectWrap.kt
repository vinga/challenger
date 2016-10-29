package com.kameo.challenger.utils.odb.newapi

import javax.persistence.criteria.Expression
import javax.persistence.criteria.Path
import javax.persistence.criteria.Selection

interface ISugarQuerySelect<E>  {
    fun getSelection(): Selection<*>;
    fun isSingle(): Boolean;
}
class SelectWrap<E> constructor (  val select: Selection<E>): ISugarQuerySelect<E> {
    override fun getSelection(): Selection<E> {
        return select;
    }

    override fun isSingle(): Boolean {
        return true;
    }

}
