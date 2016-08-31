package com.kameo.challenger.utils.odb;

import com.challenger.eviauth.odb.api.IIdentity;

import javax.persistence.metamodel.SingularAttribute;
import java.util.*;

public class Predicates extends ArrayList<Predicate> {
	private static final long serialVersionUID = 1L;
	private final CriteriaBuilder cb;

	public Predicates(CriteriaBuilder cb) {
		this.cb = cb;

	}

	public void addAll(Predicate... ps) {
		for (Predicate p : ps) {
			add(p);
		}
	}

	//if the first argument is greater than or equal to the second.
	public <E> void addEqualsIfPresent(Expression<E> exp, Optional<E> op) {
		if (op.isPresent())
			add(cb.equal(exp, op.get()));
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	//if the first argument is greater than or equal to the second.
	//	for egzample to check if sth was created after DATE_FROM
	//p.addGreaterThanOrEqualToIfPresent(root.get(MessageAnnotationODB_.sysCreationDate), DATE_FROM);
	public <E extends Comparable> void addGreaterThanOrEqualToIfPresent(Expression<E> exp, Optional<E> op) {
		if (op.isPresent())
			add(cb.greaterThanOrEqualTo(exp, op.get()));
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public <E extends Comparable> void addLessThanIfPresent(Expression<E> exp, Optional<E> op) {
		if (op.isPresent())
			add(cb.lessThan(exp, op.get()));
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public <E extends Comparable> void addLessThanOrEqualIfPresent(Expression<E> exp, Optional<E> op) {
		if (op.isPresent())
			add(cb.lessThanOrEqualTo(exp, op.get()));
	}

	public <E> void addInIfNotEmpty(From<?, E> from, Collection<E> col) {
		if (!col.isEmpty())
			add(from.in(col));
	}

	public <E> void addInIfNotEmpty2(Path<E> from, Collection<E> col) {
		if (!col.isEmpty())
			add(from.in(col));
	}

	public void addInIfNotEmpty(Root<? extends IIdentity> from, Set<Long> col) {
		if (!col.isEmpty())
			add(from.in(col));

	}

	public void addInIfNotEmpty(Path<Long> from, Collection<Long> col) {
		if (!col.isEmpty())
			add(from.in(col));
	}

	public <E, F> void addInIfNotEmpty(IJoinDeliverable<E, F> jd, Collection<?> col) {
		if (!col.isEmpty())
			add(jd.deliverJoin().in(col));
	}

	public <E, F> Optional<Predicate> createInIfNotEmpty(IJoinDeliverable<E, F> jd, Collection<?> col) {
		if (!col.isEmpty())
			return Optional.of(jd.deliverJoin().in(col));
		return Optional.empty();
	}

	public <E, F> void addInIfNotEmpty(IJoinDeliverable<?, E> jd, SingularAttribute<E, F> sa, Collection<F> col) {
		if (!col.isEmpty())
			add(jd.deliverJoin().get(sa).in(col));
	}

	public <E, G, F> void addInIfNotEmpty(IJoinDeliverable<?, E> jd, SingularAttribute<E, G> sa, SingularAttribute<G, F> sa2, Collection<F> col) {
		if (!col.isEmpty())
			add(jd.deliverJoin().get(sa).get(sa2).in(col));
	}

	public <F> void addOrInIfNotEmpty(IJoinDeliverable<?, F> d1, IJoinDeliverable<?, F> d2, Collection<?> col) {
		if (!col.isEmpty())
			add(cb.or(d1.deliverJoin().in(col), d2.deliverJoin().in(col)));

	}

	public <E> void addIfPresent(Optional<IRestrictions<E>> rest, CriteriaQuery<?> crit, Root<E> msg) {
		if (rest.isPresent())
			addAll(Arrays.asList(rest.get().apply(cb, crit, msg)));
	}

	public <E> void addIfPresent(Optional<IPathRestrictions<E>> rest, CriteriaQuery<?> crit, Path<E> msg) {
		if (rest.isPresent())
			addAll(Arrays.asList(rest.get().apply(cb, crit, msg)));
	}

	public void addIfPresent(Optional<Predicate> p) {
		if (p.isPresent())
			add(p.get());
	}

	public Predicate[] get() {
		return EntityHelper.toArray(Predicate.class, this);
	}

	public Predicate getAsOne() {
		return cb.and(EntityHelper.toArray(Predicate.class, this));
	}

}
