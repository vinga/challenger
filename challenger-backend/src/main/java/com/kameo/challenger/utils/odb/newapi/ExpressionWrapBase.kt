package com.kameo.challenger.utils.odb.newapi

import javax.persistence.criteria.Expression
import javax.persistence.criteria.Path
import javax.persistence.criteria.Selection


open class ExpressionWrapBase<E> constructor (val root: Expression<E>): ISugarQuerySelect<E>,  /*to usunac jak chce wylaczyc*/ISelectExpressionProvider<E> {


    override fun getSelection(): Selection<*> {
        return root;
    }

    override fun getDirectSelection(): ISugarQuerySelect<E> {
       return SelectWrap(root);
    }

    override fun isSingle(): Boolean {
        return true;
    }
}

