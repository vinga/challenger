package com.kameo.challenger.utils.odb.newapi.wraps

import com.kameo.challenger.odb.api.IIdentity
import com.kameo.challenger.utils.odb.AnyDAO
import com.kameo.challenger.utils.odb.AnyDAONew.PathPairSelect
import com.kameo.challenger.utils.odb.AnyDAONew.PathTripleSelect
import com.kameo.challenger.utils.odb.newapi.*
import com.kameo.challenger.utils.odb.newapi.pc.PathContext
import com.kameo.challenger.utils.odb.newapi.pc.QueryPathContext
import com.kameo.challenger.utils.odb.newapi.pc.SubqueryPathContext
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*
import javax.persistence.criteria.*
import javax.persistence.metamodel.SingularAttribute
import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty1

@Suppress("UNCHECKED_CAST")
open class PathWrap<E, G> constructor(
        pc: PathContext<G>,
        open val root: Path<E>
) :
        ExpressionWrap<E, G>(pc, root) {
    override val it: PathWrap<E, G> by lazy {
        this
    }

    infix fun groupBy(expr: KMutableProperty1<E, *>) {
        return pc.groupBy(arrayOf(ExpressionWrap<E, G>(pc, root.get(expr.name))))
    }

    infix fun groupBy(expr: ExpressionWrap<E, *>) {
        return pc.groupBy(arrayOf(ExpressionWrap(pc, expr.getExpression())))
    }

    fun groupBy(vararg exprs: KMutableProperty1<E, *>) {
        return pc.groupBy(exprs.map { ExpressionWrap<E, G>(pc, root.get(it.name)) }.toTypedArray())
    }

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

    infix fun <F> selectDistinct(pw: ExpressionWrap<F, G>): ISugarQuerySelect<F> {
        return SelectWrap(pw.getDirectSelection().getSelection() as Selection<F>, true)
    }

    fun <F, G> select(pw1: ISelectExpressionProvider<F>, pw2: ISelectExpressionProvider<G>, distinct: Boolean = false): PathPairSelect<F, G> {
        return PathPairSelect(pw1.getDirectSelection(), pw2.getDirectSelection(), distinct, pc.cb)
    }


    fun <F, G, H> select(pw1: ISelectExpressionProvider<F>, pw2: ISelectExpressionProvider<G>, pw3: ISelectExpressionProvider<H>, distinct: Boolean = false): PathTripleSelect<F, G, H> {
        return PathTripleSelect(pw1.getDirectSelection(), pw2.getDirectSelection(), pw3.getDirectSelection(), distinct, pc.cb)
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
    @JvmName("isNullInfix")
    infix fun isNull(p: ()->Unit ): PathWrap<E, G> {
        pc.add({ pc.cb.isNull(root) })
        return this
    }

    infix fun inIds(ids: Collection<Long>): PathWrap<E, G> {
        pc.add({ root.get<Path<Long>>(AnyDAO.id_column).`in`(ids) })
        return this
    }


    class ClousureWrap<E, G>(//var innerList:MutableList<() -> Predicate?> = mutableListOf<() -> Predicate?>(),
            pc: PathContext<G>,
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

    infix fun ors(orClause: (PathWrap<E, G>) -> Unit): PathWrap<E, G> =
        newOr(orClause)

    infix fun ands(orClause: (PathWrap<E, G>) -> Unit): PathWrap<E, G> =
            newAnd(orClause)

    infix fun newOr(orClause: (PathWrap<E, G>) -> Unit): PathWrap<E, G> {
        val list = mutableListOf<() -> Predicate?>()
        val pw = ClousureWrap(pc, root)
        pc.add({ calculateOr(list) })
        pc.stackNewArray(list)
        orClause.invoke(pw)
        pc.unstackArray()
        return this
    }


    infix fun or(orClause: PathWrap<E, G>.() -> Unit): PathWrap<E, G> {
        val list = mutableListOf<() -> Predicate?>()
        val pw = ClousureWrap(pc, root)
        pc.stackNewArray(list)
        orClause.invoke(pw)
        pc.mergeLevelUpAsOr()
        return this
    }

    infix fun and(orClause: PathWrap<E, G>.() -> Unit): PathWrap<E, G> {
        val list = mutableListOf<() -> Predicate?>()
        val pw = ClousureWrap(pc, root)
        pc.stackNewArray(list)
        orClause.invoke(pw)
        pc.mergeLevelUpAsAnd()
        return this
    }

    infix fun newAnd(orClause: (PathWrap<E, G>) -> Unit): PathWrap<E, G> {
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

    override infix fun notEq(expr: IExpression<E, *>): PathWrap<E, G> {
        super.notEq(expr)
        return this
    }

    override infix fun isIn(list: List<E>): PathWrap<E, G> {
        super.isIn(list)
        return this
    }
    override infix fun isIn(expr: ExpressionWrap<E,*>): PathWrap<E, G> {
        super.isIn(expr)
        return this
    }
    override infix fun isIn(expr: SubqueryWrap<E,*>): PathWrap<E, G> {
        super.isIn(expr)
        return this
    }
    override infix fun exists(expr: SubqueryWrap<*,*>): PathWrap<E, G> {
        super.exists(expr)
        return this
    }

    override infix fun notExists(expr: SubqueryWrap<*,*>): PathWrap<E, G> {
        super.notExists(expr)
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


    infix fun <F> get(sa: SingularAttribute<E, F>): PathWrap<F, G> = PathWrap<F, G>(pc, root.get(sa))

    infix fun <F> get(sa: KMutableProperty1<E, F>): PathWrap<F, G> = PathWrap(pc, root.get(sa.name))


    fun <F> get(sa: KMutableProperty1<E, List<F>>): UseGetListOnJoinInstead {

        //val join = (root as Join<Any, E>).join<E, F>(sa.name) as Join<Any, F>
        return UseGetListOnJoinInstead()
    }


    @JvmName("getAsString")
    infix fun get(sa: KMutableProperty1<E, String>): StringPathWrap<G> =
        StringPathWrap(pc, root.get(sa.name))

    @JvmName("getAsComparable")
    infix fun <F : Comparable<F>> get(sa: KMutableProperty1<E, F>): ComparablePathWrap<F, G> =
        ComparablePathWrap(pc, root.get(sa.name))

    @JvmName("getAsLocalDateTime")
    infix fun get(sa: KMutableProperty1<E, LocalDateTime>): LocalDateTimePathWrap<G> =
        LocalDateTimePathWrap(pc, root.get(sa.name))

    @JvmName("getAsLocalDate")
    infix fun get(sa: KMutableProperty1<E, LocalDate>): LocalDatePathWrap<G> =
        LocalDatePathWrap(pc, root.get(sa.name))


    @JvmName("getAsNumber")
    infix fun <F> get(sa: KMutableProperty1<E, F>): NumberPathWrap<F, G> where F : Number, F:Comparable<F>  {

        return NumberPathWrap(pc, root.get(sa.name))
    }


    fun <E : Any, RESULT> subqueryFrom(clz: KClass<E>, query: RootWrap<E, E>.() -> (ISugarQuerySelect<RESULT>)): SubqueryWrap<RESULT, G> {
        val criteriaQuery = pc.criteria as CriteriaQuery<E>
        val subqueryPc = SubqueryPathContext(clz.java, pc.em, pc as QueryPathContext<G>, criteriaQuery.subquery(clz.java) as Subquery<G>)
        val returnedExpression = subqueryPc.invokeQuery(query)

        return returnedExpression as SubqueryWrap<RESULT, G>
    }

    fun <J: Any> isInSubquery(clz: KClass<J>, query: RootWrap<J, J>.() -> (ISugarQuerySelect<E>)): PathWrap<E, G>  {
        isIn(subqueryFrom(clz,query))
        return this
    }




}