package com.kameo.challenger.utils.odb.newapi

import com.kameo.challenger.odb.api.IIdentity
import com.kameo.challenger.utils.odb.AnyDAO
import com.kameo.challenger.utils.odb.AnyDAONew.PathPairSelect
import com.kameo.challenger.utils.odb.AnyDAONew.PathTripleSelect
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*
import javax.persistence.EntityManager
import javax.persistence.Query
import javax.persistence.TypedQuery
import javax.persistence.criteria.*
import javax.persistence.metamodel.SingularAttribute
import kotlin.reflect.*
import kotlin.reflect.KMutableProperty1.Setter
import kotlin.reflect.KProperty1.Getter


abstract class PathContext<G>
constructor(
        val em: EntityManager,
        open val criteria: CommonAbstractCriteria) {

    val cb: CriteriaBuilder = em.criteriaBuilder
    val orders: MutableList<Order> = mutableListOf()
    val arraysStack: MutableList<MutableList<() -> Predicate?>> = mutableListOf()

    private var currentArray: MutableList<() -> Predicate?> = mutableListOf()
        private set
    var skip: Int? = null
    var take: Int? = null

    lateinit var root: Root<Any>
        protected set
    lateinit var rootWrap: PathWrap<*, G>
        protected set
    var defaultSelection: ISugarQuerySelect<Any>? = null
        protected set


    fun addOrder(o: Order) {
        orders.add(o)
    }

    fun add(function: () -> Predicate?) {
        currentArray.add(function)
    }

    fun stackNewArray(newArr: MutableList<() -> Predicate?>) {
        arraysStack.add(currentArray)
        currentArray = newArr
    }

    fun unstackArray() {
        currentArray = arraysStack.last()
        arraysStack.remove(currentArray)
    }


    fun getPredicate(): Predicate {
        if (arraysStack.isNotEmpty())
            throw IllegalArgumentException("In or Or clause has not been closed")
        val predicates = currentArray.mapNotNull { it.invoke() }
        if (predicates.size == 1) {
            return predicates[0]
        } else {
            return cb.and(*predicates.toTypedArray())
        }
    }

}

open class ExpressionWrap<E, G> constructor(
        val pc: PathContext<G>,
        val value: Expression<E>
) :
        ISelectExpressionProvider <E>,
        ISugarQuerySelect<G>, //by pathSelect,
        IExpression<E, G> {

    override fun getSelection(): Selection<*> {
        return pc.defaultSelection!!.getSelection()
    }

    override fun isSingle(): Boolean {
        return pc.defaultSelection!!.isSingle()
    }

    override fun eq(expr: E): ExpressionWrap<E, G> {
        pc.add({ pc.cb.equal(this.value, expr) })
        return this
    }

    override fun eq(expr: IExpression<E, *>): ExpressionWrap<E, G> {
        pc.add({ pc.cb.equal(this.value, expr.getExpression()) })
        return this
    }


    val cb = pc.cb

    override fun getDirectSelection(): ISugarQuerySelect<E> {
        return SelectWrap(value)
    }

    override fun getExpression(): Expression<E> {
        return value
    }


}


interface IExpression<F, G> {
    fun getExpression(): Expression<F>
    infix fun eq(expr: IExpression<F, *>): IExpression<F, G>
    infix fun eq(expr: F): IExpression<F, G>
}

interface IStringExpressionWrap<G> : IExpression<String, G> {
    infix fun like(f: String): IExpression<String, G>
    infix fun like(f: Expression<String>): IExpression<String, G>
    fun lower(): StringExpressionWrap<G>
}


class StringExpressionWrap<G> constructor(
        pc: PathContext<G>,
        value: Expression<String>) : ExpressionWrap<String, G>(pc, value), IStringExpressionWrap<G> {

    override infix fun like(f: String): IExpression<String, G> {
        pc.add({ pc.cb.like(value, f) })
        return this
    }

    override infix fun like(f: Expression<String>): IExpression<String, G> {
        pc.add({ pc.cb.like(value, f) })
        return this
    }

    override fun lower(): StringExpressionWrap<G> {
        return StringExpressionWrap(pc, pc.cb.lower(value))
    }
}

class JoinWrap<E, G> constructor(val pw: PathContext<G>,
                                 override val root: Join<Any, E>)
: PathWrap<E, G>(pw, root) {


    @Suppress("UNCHECKED_CAST")
    fun <F> join(sa: KMutableProperty1<E, F>): JoinWrap<F, G> {
        val join = root.join<E, F>(sa.name) as Join<Any, F>
        return JoinWrap(pw, join)
    }

    @Suppress("UNCHECKED_CAST")
            // perhaps we want to create here dedicated class
    fun <F> joinList(sa: KMutableProperty1<E, List<F>>): JoinWrap<F, G> {
        val join = root.join<E, F>(sa.name) as Join<Any, F>
        return JoinWrap(pw, join)
    }

}



class QueryPathContext<G>(clz: Class<*>,
                          em: EntityManager,
                          override val criteria: CriteriaQuery<G> = em.criteriaBuilder.createQuery(clz) as CriteriaQuery<G>)
: PathContext<G>(em, criteria) {

    var selector: ISugarQuerySelect<*>? = null // set after execution


    init {
        root = criteria.from(clz as Class<Any>)
        defaultSelection = SelectWrap(root)
        rootWrap = RootWrap(this, root)
    }


    fun <RESULT, E> invokeQuery(query: (RootWrap<E, E>) -> ISugarQuerySelect<RESULT>): TypedQuery<RESULT> {
        selector = query.invoke(rootWrap as RootWrap<E, E>)
        val sell = selector!!.getSelection()
        criteria.select(sell as Selection<out G>)
        return calculateWhere(em) as TypedQuery<RESULT>
    }

    fun calculateWhere(em: EntityManager): TypedQuery<*> {
        criteria.where(getPredicate())
        if (orders.isNotEmpty())
            criteria.orderBy(orders)
        val jpaQuery = em.createQuery(criteria)
        applyPage(jpaQuery)
        return jpaQuery
    }

    private fun applyPage(jpaQuery: TypedQuery<*>) {
        val skip = skip
        if (skip != null)
            jpaQuery.firstResult = skip
        val take = take
        if (take != null)
            jpaQuery.maxResults = take
    }

    fun <RESULT : Any> mapToPluralsIfNeeded(res: List<RESULT>): List<RESULT> {
        if (res.isNotEmpty()) {
            if (!selector!!.isSingle()) {
                if (res.first() is Array<*>) {

                    val rows = res as List<Array<Any>>
                    val row = rows.first()
                    if (row.size == 2) {
                        return rows.map({
                            Pair(it[0], it[1]) as RESULT
                        })
                    } else if (row.size == 3) {
                        return rows.map({
                            Triple(it[0], it[1], it[2]) as RESULT
                        })
                    }
                }
            }
        }
        return res
    }

}

class UpdatePathContext<G>(clz: Class<*>,
                           em: EntityManager,
                           override val criteria: CriteriaUpdate<G> = em.criteriaBuilder.createCriteriaUpdate(clz) as CriteriaUpdate<G>)
: PathContext<G>(em, criteria) {

    init {
        root = criteria.from(clz as Class<G>) as Root<Any>
        rootWrap = RootWrapUpdate(this, root)
    }

    @Suppress("UNCHECKED_CAST")
    fun <E> invokeUpdate(query: (RootWrapUpdate<E, E>) -> Unit): Query {
        query.invoke(rootWrap as RootWrapUpdate<E, E>)
        calculateWhere(criteria)
        return em.createQuery(criteria)
    }

    private fun calculateWhere(cq: CriteriaUpdate<*>) {
        cq.where(getPredicate())
    }
}

class DeletePathContext<G>(clz: Class<*>,
                           em: EntityManager,
                           override val criteria: CriteriaDelete<G> = em.criteriaBuilder.createCriteriaDelete(clz) as CriteriaDelete<G>)
: PathContext<G>(em, criteria) {

    init {
        root = (criteria as CriteriaDelete<Any>).from(clz as Class<Any>)
        rootWrap = RootWrap(this, root)
    }

    @Suppress("UNCHECKED_CAST")
    fun <E> invokeDelete(query: (RootWrap<E, E>) -> Unit): Query {
        query.invoke(rootWrap as RootWrap<E, E>)
        calculateWhere(criteria)
        return em.createQuery(criteria)

    }

    private fun calculateWhere(cq: CriteriaDelete<*>) {
        cq.where(getPredicate())
    }
}

open class RootWrap<E, G> constructor(
        val pw: com.kameo.challenger.utils.odb.newapi.PathContext<G>,
        root: Root<E>) : PathWrap<E, G>(pw, root) {


    @Suppress("UNCHECKED_CAST")
    fun <F> join(sa: KMutableProperty1<E, F>): JoinWrap<F, G> {
        val join = (root as From<Any, E>).join<E, F>(sa.name) as Join<Any, F>
        return JoinWrap(pw, join)
    }

    fun <F : Any> from(sa: KClass<F>): RootWrap<F, G> {
        val criteriaQuery = pw.criteria as? CriteriaQuery<F>
        if (criteriaQuery != null) {
            val from = criteriaQuery.from(sa.java)
            return RootWrap(pw, from)
        } else {
            val criteriaUpdateQuery = pw.criteria as? CriteriaUpdate<F>
            if (criteriaUpdateQuery != null) {
                val from = criteriaUpdateQuery.from(sa.java)
                return RootWrap(pw, from)
            }
        }
        throw IllegalArgumentException("Clause 'from' is supported only for CriteriaQuery and CriteriaUpdate")

    }

}

class RootWrapUpdate<E, G> constructor(val pw: UpdatePathContext<G>, root: Root<E>) : PathWrap<E, G>(pw, root) {

    fun <F> set(sa: KMutableProperty1<E, F>, f: F): RootWrapUpdate<E, G> {
        pw.criteria.set(get(sa).root, f)
        return this
    }

    fun <F> set(sa: KMutableProperty1<E, F>, f: Expression<F>): RootWrapUpdate<E, G> {
        pw.criteria.set(get(sa).root, f)
        return this
    }

    fun <F> set(sa: KMutableProperty1<E, F>, f: PathWrap<F, G>): RootWrapUpdate<E, G> {
        pw.criteria.set(get(sa).root, f.root)
        return this
    }


}


open class PathWrap<E, G> constructor(
        pc: com.kameo.challenger.utils.odb.newapi.PathContext<G>,
        open val root: Path<E>
) :
        ExpressionWrap <E, G>(pc, root) {

    override fun getDirectSelection(): ISugarQuerySelect<E> {
        return SelectWrap(root)
    }

    infix fun skip(skip: Int): PathWrap<E, G> {
        pc.skip = skip
        return this
    }

    infix fun limit(take: Int): PathWrap<E, G> {
        pc.take = take
        return this
    }


    infix fun <F> select(pw: KMutableProperty1<E, F>): ISugarQuerySelect<F> {
        return select(get(pw))
    }

    infix fun <F> select(pw: ExpressionWrap<F, G>): ISugarQuerySelect<F> {
        return pw.getDirectSelection()
    }


    fun <F, G> select(pw1: ISelectExpressionProvider<F>, pw2: ISelectExpressionProvider<G>): PathPairSelect<F, G> {
        return PathPairSelect(pw1.getDirectSelection(), pw2.getDirectSelection(), pc.cb)
    }


    fun <F, G, H> select(pw1: ISelectExpressionProvider<F>, pw2: ISelectExpressionProvider<G>, pw3: ISelectExpressionProvider<H>): PathTripleSelect<F, G, H> {
        return PathTripleSelect(pw1.getDirectSelection(), pw2.getDirectSelection(), pw3.getDirectSelection(), pc.cb)
    }

    infix fun eqId(id: Long): PathWrap<E, G> {
        pc.add({ pc.cb.equal(root.get<Path<Long>>(AnyDAO.id_column), id) })
        return this
    }

    // should be forbidden on root....
    fun isNull(): PathWrap<E, G> {
        pc.add({ pc.cb.isNull(root) })
        return this
    }


    infix fun inIds(ids: Collection<Long>): PathWrap<E, G> {
        pc.add({ root.get<Path<Long>>(AnyDAO.id_column).`in`(ids) })
        return this
    }


    class ClousureWrap<E, G>(//var innerList:MutableList<() -> Predicate?> = mutableListOf<() -> Predicate?>(),
            pc: com.kameo.challenger.utils.odb.newapi.PathContext<G>,
            root: Path<E>
    ) : PathWrap<E, G>(pc, root)

    fun <I, J> ref(ref: PathWrap<I, J>, clause: (PathWrap<I, J>) -> Unit): PathWrap<E, G> {
        clause.invoke(ref)
        return this
    }

    fun ref(clause: (PathWrap<E, G>) -> Unit): PathWrap<E, G> {
        clause.invoke(this)
        return this
    }


    fun <I, J> ref(ref: PathWrap<I, J>): PathWrap<I, J> {
        return ref
    }

    fun finish(): PathWrap<E, G> {
        pc.unstackArray()
        return this
    }

    fun newOr(): ClousureWrap<E, G> {
        val list = mutableListOf<() -> Predicate?>()


        val pw = ClousureWrap(pc, root)


        pc.add({ calculateOr(list) })
        pc.stackNewArray(list)
        return pw
    }


    fun newOr(orClause: (PathWrap<E, G>) -> Unit): PathWrap<E, G> {
        val list = mutableListOf<() -> Predicate?>()
        val pw = ClousureWrap(pc, root)
        pc.add({ calculateOr(list) })
        pc.stackNewArray(list)
        orClause.invoke(pw)
        pc.unstackArray()
        return this
    }

    fun newAnd(orClause: (PathWrap<E, G>) -> Unit): PathWrap<E, G> {
        val list = mutableListOf<() -> Predicate?>()
        val pw = ClousureWrap(pc, root)
        pc.add({ calculateAnd(list) })
        pc.stackNewArray(list)
        orClause.invoke(pw)
        pc.unstackArray()
        return this
    }


    fun newAnd(): ClousureWrap<E, G> {
        val list = mutableListOf<() -> Predicate?>()
        val pw = ClousureWrap(pc, root)
        pc.add({ calculateAnd(list) })
        pc.stackNewArray(list)
        return pw
    }


    private fun calculateOr(list: MutableList<() -> Predicate?>): Predicate? {
        val predicates = toPredicates(list)
        return if (predicates.isNotEmpty())
            cb.or(*predicates.toTypedArray())
        else
            null
    }

    private fun calculateAnd(list: MutableList<() -> Predicate?>): Predicate? {
        val predicates = toPredicates(list)
        return if (predicates.isNotEmpty())
            cb.and(*predicates.toTypedArray())
        else
            null
    }

    private fun toPredicates(list: MutableList<() -> Predicate?>): MutableList<Predicate> {
        val predicates = list
                .asSequence()
                .mapNotNull { it.invoke() }
                .toMutableList()
        return predicates
    }


    fun inIds(vararg ids: Long): PathWrap<E, G> {
        pc.add({ root.get<Path<Long>>(AnyDAO.id_column).`in`(ids) })
        return this
    }

    override infix fun eq(expr: E): PathWrap<E, G> {
        super.eq(expr)
        return this
    }

    fun <F> eq(sa: SingularAttribute<E, F>, f: F): PathWrap<E, G> {
        pc.add { cb.equal(root.get(sa), f) }
        return this
    }

    fun like(sa: KMutableProperty1<E, String>, f: String): PathWrap<E, G> {
        pc.add { cb.like(root.get<Path<String>>(sa.name) as (Expression<String>), f) }
        return this
    }

    fun <F> eq(sa: KMutableProperty1<E, F>, f: F): PathWrap<E, G> {
        pc.add({ cb.equal(root.get<Path<F>>(sa.name), f) })
        return this
    }


    fun <F : IIdentity> eqId(sa: KMutableProperty1<E, F>, id: Long): PathWrap<E, G> {
        pc.add({ cb.equal(root.get<Path<F>>(sa.name).get<Long>(AnyDAO.id_column), id) })
        return this
    }


    fun <F> eq(exp1: ExpressionWrap<F, G>, f: F): PathWrap<E, G> {
        pc.add({ cb.equal(exp1.getExpression(), f) })
        return this
    }

    fun <F> notEq(sa: KMutableProperty1<E, F>, f: F): PathWrap<E, G> {
        pc.add({ cb.notEqual(root.get<F>(sa.name), f) })
        return this
    }

    fun <F> notEq(sa: SingularAttribute<E, F>, f: F): PathWrap<E, G> {
        pc.add({ cb.notEqual(root.get(sa), f) })
        return this
    }

    fun <F : Comparable<F>> after(sa: SingularAttribute<E, F>, f: F): PathWrap<E, G> {
        pc.add({ cb.greaterThan(root.get(sa), f) })
        return this
    }

    fun <F : Comparable<F>> after(sa: KMutableProperty1<E, F>, f: F): PathWrap<E, G> {
        pc.add({ cb.greaterThan(root.get(sa.name), f) })
        return this
    }

    fun <F : Comparable<F>> greaterThan(sa: KMutableProperty1<E, F>, f: F): PathWrap<E, G> {
        pc.add({ cb.greaterThan(root.get(sa.name), f) })
        return this
    }

    fun <F : Comparable<F>> greaterThanOrEqualTo(sa: KMutableProperty1<E, F>, f: F): PathWrap<E, G> {
        pc.add({ cb.greaterThanOrEqualTo(root.get(sa.name), f) })
        return this
    }

    fun <F : Comparable<F>> lessThan(sa: KMutableProperty1<E, F>, f: F): PathWrap<E, G> {
        pc.add({ cb.lessThan(root.get(sa.name), f) })
        return this
    }

    fun <F : Comparable<F>> lessThanOrEqualTo(sa: KMutableProperty1<E, F>, f: F): PathWrap<E, G> {
        pc.add({ cb.lessThanOrEqualTo(root.get(sa.name), f) })
        return this
    }

    fun <F : Comparable<F>> before(sa: KMutableProperty1<E, F>, f: F): PathWrap<E, G> {
        pc.add({ cb.lessThan(root.get(sa.name), f) })
        return this
    }

    @JvmName("afterDate")
    fun after(sa: KMutableProperty1<E, Date?>, f: Date): PathWrap<E, G> {
        pc.add({ cb.greaterThan(root.get(sa.name), f) })
        return this
    }

    @JvmName("after")
    fun after(sa: KMutableProperty1<E, LocalDateTime?>, f: LocalDateTime): PathWrap<E, G> {
        pc.add({ cb.greaterThan(root.get(sa.name), f) })
        return this
    }

    @JvmName("before")
    fun before(sa: KMutableProperty1<E, LocalDateTime?>, f: LocalDateTime): PathWrap<E, G> {
        pc.add({ cb.lessThan(root.get(sa.name), f) })
        return this
    }

    fun before(sa: KMutableProperty1<E, Long?>, f: Long): PathWrap<E, G> {
        pc.add({ cb.lessThan(root.get(sa.name), f) })
        return this
    }

    @JvmName("beforeDate")
    fun before(sa: KMutableProperty1<E, Date?>, f: Date): PathWrap<E, G> {
        pc.add({ cb.lessThan(root.get(sa.name), f) })
        return this
    }

    operator fun <F> rangeTo(sa: KMutableProperty1<E, F>): PathWrap<F, G> {
        return get(sa)
    }

    fun eqIdToPred(id: Long): Predicate {
        return cb.equal(root.get<Path<Long>>(AnyDAO.id_column), id)
    }

    infix fun isIn(list: List<E>): PathWrap<E, G> {
        pc.add({ root.`in`(list) })
        return this
    }

    fun <F : Number> max(sa: KMutableProperty1<E, F>): ExpressionWrap<F, G> {
        return ExpressionWrap<F, G>(pc, pc.cb.max(root.get(sa.name)))
    }

    fun <F : Number> min(sa: KMutableProperty1<E, F>): ExpressionWrap<F, G> {
        return ExpressionWrap<F, G>(pc, pc.cb.min(root.get(sa.name)))
    }


    infix fun orderByAsc(sa: KMutableProperty1<E, *>): PathWrap<E, G> {
        pc.addOrder(cb.asc(root.get<Any>(sa.name)))
        return this
    }

    infix fun orderByDesc(sa: KMutableProperty1<E, *>): PathWrap<E, G> {
        pc.addOrder(cb.desc(root.get<Any>(sa.name)))
        return this
    }

    infix fun orderBy(pw: Pair<PathWrap<*, *>, Boolean>) {
        val (pathWrap, asc) = pw
        pc.addOrder(if (asc) cb.asc(pathWrap.root) else cb.desc(pathWrap.root))
    }

    fun orderBy(vararg pw: Pair<PathWrap<*, *>, Boolean>) {
        for ((pathWrap, asc) in pw) {
            pc.addOrder(if (asc) cb.asc(pathWrap.root) else cb.desc(pathWrap.root))
        }
    }


    // type safety class to not use get with lists paremters
    class UseGetListOnJoinInstead


    infix fun notEq(f: E): PathWrap<E, G> {
        pc.add { cb.notEqual(root, f) }
        return this
    }


    fun <F> get(sa: SingularAttribute<E, F>): PathWrap<F, G> {
        return PathWrap<F, G>(pc, root.get(sa))
    }

    infix fun <F> get(sa: KMutableProperty1<E, F>): PathWrap<F, G> {
        return PathWrap(pc, root.get(sa.name))
    }

    fun <F> get(sa: KMutableProperty1<E, List<F>>): UseGetListOnJoinInstead {
        sa.name
        //val join = (root as Join<Any, E>).join<E, F>(sa.name) as Join<Any, F>
        return UseGetListOnJoinInstead()
    }


    @JvmName("getAsString")
    infix fun get(sa: KMutableProperty1<E, String>): StringPathWrap<G> {
        return StringPathWrap(pc, root.get(sa.name))
    }

    @JvmName("getAsComparable")
    infix fun <F : Comparable<F>> get(sa: KMutableProperty1<E, F>): ComparablePathWrap<F, G> {
        return ComparablePathWrap(pc, root.get(sa.name))
    }

    @JvmName("getAsLocalDateTime")
    infix fun get(sa: KMutableProperty1<E, LocalDateTime>): LocalDateTimePathWrap<G> {
        return LocalDateTimePathWrap(pc, root.get(sa.name))
    }
    @JvmName("getAsLocalDate")
    infix fun get(sa: KMutableProperty1<E, LocalDate>): LocalDatePathWrap<G> {
        return LocalDatePathWrap(pc, root.get(sa.name))
    }



}
class LocalDateTimePathWrap<G>(pc: com.kameo.challenger.utils.odb.newapi.PathContext<G>,
                                               root: Path<LocalDateTime>) : PathWrap<LocalDateTime, G>(pc, root) {
    infix fun before(f: LocalDateTime): PathWrap<LocalDateTime, G> {
        pc.add({ cb.lessThan(root as Expression<LocalDateTime>, f) })
        return this
    }
    infix fun beforeOrEqual(f: LocalDateTime): PathWrap<LocalDateTime, G> {
        pc.add({ cb.lessThanOrEqualTo(root as Expression<LocalDateTime>, f) })
        return this
    }

    infix fun after(f: LocalDateTime): PathWrap<LocalDateTime, G> {
        pc.add({ cb.greaterThan(root as Expression<LocalDateTime>, f) })
        return this
    }
    infix fun afterOrEqual(f: LocalDateTime): PathWrap<LocalDateTime, G> {
        pc.add({ cb.greaterThanOrEqualTo(root as Expression<LocalDateTime>, f) })
        return this
    }
    infix fun lessThan(f: LocalDateTime): PathWrap<LocalDateTime, G> {
        pc.add({ cb.lessThan(root as Expression<LocalDateTime>, f) })
        return this
    }
    infix fun greaterThan(f: LocalDateTime): PathWrap<LocalDateTime, G> {
        pc.add({ cb.greaterThan(root as Expression<LocalDateTime>, f) })
        return this
    }
    infix fun lessThanOrEqualTo(f: LocalDateTime): PathWrap<LocalDateTime, G> {
        pc.add({ cb.lessThanOrEqualTo(root as Expression<LocalDateTime>, f) })
        return this
    }
    infix fun greaterThanOrEqualTo(f: LocalDateTime): PathWrap<LocalDateTime, G> {
        pc.add({ cb.greaterThanOrEqualTo(root as Expression<LocalDateTime>, f) })
        return this
    }
}
class LocalDatePathWrap<G>(pc: com.kameo.challenger.utils.odb.newapi.PathContext<G>,
                               root: Path<LocalDate>) : PathWrap<LocalDate, G>(pc, root) {
    infix fun before(f: LocalDate): PathWrap<LocalDate, G> {
        pc.add({ cb.lessThan(root as Expression<LocalDate>, f) })
        return this
    }

    infix fun after(f: LocalDate): PathWrap<LocalDate, G> {
        pc.add({ cb.greaterThan(root as Expression<LocalDate>, f) })
        return this
    }
    infix fun lessThan(f: LocalDate): PathWrap<LocalDate, G> {
        pc.add({ cb.lessThan(root as Expression<LocalDate>, f) })
        return this
    }
    infix fun greaterThan(f: LocalDate): PathWrap<LocalDate, G> {
        pc.add({ cb.greaterThan(root as Expression<LocalDate>, f) })
        return this
    }
    infix fun lessThanOrEqualTo(f: LocalDate): PathWrap<LocalDate, G> {
        pc.add({ cb.lessThanOrEqualTo(root as Expression<LocalDate>, f) })
        return this
    }
    infix fun greaterThanOrEqualTo(f: LocalDate): PathWrap<LocalDate, G> {
        pc.add({ cb.greaterThanOrEqualTo(root as Expression<LocalDate>, f) })
        return this
    }
    infix fun ge(f: LocalDate): PathWrap<LocalDate, G> {
        pc.add({ cb.greaterThanOrEqualTo(root as Expression<LocalDate>, f) })
        return this
    }

}
class ComparablePathWrap<E : Comparable<E>, G>(pc: com.kameo.challenger.utils.odb.newapi.PathContext<G>,
                                               root: Path<E>) : PathWrap<E, G>(pc, root) {
    infix fun before(f: E): PathWrap<E, G> {
        pc.add({ cb.lessThan(root as Expression<E>, f) })
        return this
    }

    infix fun after(f: E): PathWrap<E, G> {
        pc.add({ cb.greaterThan(root as Expression<E>, f) })
        return this
    }

    infix fun ge(f: E): PathWrap<E, G> {
        pc.add({ cb.greaterThanOrEqualTo(root as Expression<E>, f) })
        return this
    }
    infix fun gt(f: E): PathWrap<E, G> {
        pc.add({ cb.greaterThan(root as Expression<E>, f) })
        return this
    }
    infix fun lt(f: E): PathWrap<E, G> {
        pc.add({ cb.lessThan(root as Expression<E>, f) })
        return this
    }
    infix fun le(f: E): PathWrap<E, G> {
        pc.add({ cb.lessThanOrEqualTo(root as Expression<E>, f) })
        return this
    }
}

class StringPathWrap<G>(pc: com.kameo.challenger.utils.odb.newapi.PathContext<G>,

                        root: Path<String>) : PathWrap<String, G>(pc, root), IStringExpressionWrap<G> {

    override fun lower(): StringExpressionWrap<G> {
        return StringExpressionWrap(pc, pc.cb.lower(root))
    }

    override infix fun like(f: String): PathWrap<String, G> {
        pc.add({ pc.cb.like(root as (Expression<String>), f) })
        return this
    }

    override infix fun like(f: Expression<String>): PathWrap<String, G> {

        pc.add({ pc.cb.like(root as (Expression<String>), f) })
        return this
    }


}


operator fun <T, R> KProperty1<T, R?>.unaryPlus(): KMutableProperty1<T, R> {
    val foo = this
    if (foo is KMutableProperty1)
        return foo as KMutableProperty1<T, R>
    return object : KMutableProperty1<T, R> {
        override val name: String
            get() = foo.name

        override fun invoke(p1: T): R {
            throw UnsupportedOperationException("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun get(receiver: T): R {
            throw UnsupportedOperationException("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun callBy(args: Map<KParameter, Any?>): R {
            throw UnsupportedOperationException("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun call(vararg args: Any?): R {
            throw UnsupportedOperationException("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun set(receiver: T, value: R) {
            throw UnsupportedOperationException("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override val annotations: List<Annotation>
            get() = throw UnsupportedOperationException()
        override val returnType: KType
            get() = throw UnsupportedOperationException()
        override val parameters: List<KParameter>
            get() = throw UnsupportedOperationException()

        override val getter: Getter<T, R>
            get() = throw UnsupportedOperationException()
        override val setter: Setter<T, R>
            get() = throw UnsupportedOperationException()

    }


}

interface ISugarQuerySelect<E>  {
    fun getSelection(): Selection<*>
    fun isSingle(): Boolean
}
class SelectWrap<E> constructor (  val select: Selection<E>): ISugarQuerySelect<E> {
    override fun getSelection(): Selection<E> {
        return select
    }

    override fun isSingle(): Boolean {
        return true
    }

}

interface ISelectExpressionProvider<E> {
    fun getDirectSelection(): ISugarQuerySelect<E>
}

class SugarPredicate(val predicate: Predicate)