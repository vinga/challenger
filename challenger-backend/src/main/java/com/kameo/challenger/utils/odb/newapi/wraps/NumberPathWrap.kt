package com.kameo.challenger.utils.odb.newapi.wraps

import com.kameo.challenger.utils.odb.newapi.NumberExpressionWrap
import com.kameo.challenger.utils.odb.newapi.pc.PathContext
import javax.persistence.criteria.Path

class NumberPathWrap<F , G>(pc: PathContext<G>,
                            root: Path<F>) : ComparablePathWrap<F, G>(pc, root) where F : Number, F:Comparable<F> {
    fun max(): NumberExpressionWrap<F, G> {
        return NumberExpressionWrap<F, G>(pc, pc.cb.max(root))
    }

    fun min(): NumberExpressionWrap<F, G> {
        return NumberExpressionWrap<F, G>(pc, pc.cb.min(root))
    }

    infix fun max(f: ()->Unit): NumberExpressionWrap<F, G> {
        return NumberExpressionWrap<F, G>(pc, pc.cb.max(root))
    }

    infix fun min(f: ()->Unit): NumberExpressionWrap<F, G> {
        return NumberExpressionWrap<F, G>(pc, pc.cb.min(root))
    }


}