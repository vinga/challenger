package com.kameo.challenger.utils.odb.newapi

import javax.persistence.criteria.Expression
import javax.persistence.criteria.Path

/**
 * Created by Kamila on 2016-10-27.
 */
open class ExpressionWrap<E> constructor (val root: Expression<E>): ISelectWrap<E> {
    override val select: Expression<E>
        get() = root;
}