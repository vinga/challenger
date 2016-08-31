package com.kameo.challenger.utils.odb;

import com.challenger.eviauth.odb.api.IIdentity;
import com.google.common.base.Function;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import org.hibernate.Hibernate;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.SingularAttribute;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

public class EntityHelper {




	public static interface IUpdater<E> {
		public void applyUpdateChanges(E toBeUpdated, E incoming);
	}
	public static <E extends IIdentity> void applyUpdateChanges(Collection<E> toBeUpdated, Collection<E> incoming, IUpdater<E> updater) {
		Map<Long, E> map = EntityHelper.toMap(incoming);
		for (E e1: toBeUpdated) {
			E e2 = map.get(e1.getId());
			if (e2!=null) {
				updater.applyUpdateChanges(e1, e2);
			}
		}

	}

	public static void applyHints(TypedQuery<?> query, Hints ... hints) {
		for (Hints h: hints) {
			switch (h) {
			case CACHE:
				setCacheable(query);
				break;
			case READ_ONLY:
				setReadOnlyHint(query);
				break;
			default:
				break;

			}
		}
	}
	public static void setReadOnlyHint(TypedQuery<?> query) {
		query.setHint("org.hibernate.readOnly",true);
	}
	public static void setCacheable(TypedQuery<?> query) {
		query.setHint("org.hibernate.cacheable",true);
	}

	public static <E, F> F getField(EntityManager em, long id, SingularAttribute<E, F> sa, Hints... hints) {
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<F> criteria = cb.createQuery(sa.getBindableJavaType());
		Root<E> root = criteria.from(sa.getDeclaringType().getJavaType());
		criteria.select(root.get(sa));
		criteria.where(cb.equal(root.get(IIdentity.id_column), id));
		TypedQuery<F> query = em.createQuery(criteria);
		EntityHelper.applyHints(query, hints);
		query.setMaxResults(1);
		return query.getSingleResult();
	}
	public static <E, F> Set<F> transformToSet(Collection<E> col, SingularAttribute<E, F> se) {
		Set<F> res = Sets.newHashSet();
		Method gmethod = getGetterMethod(se);
		for (E e : col) {
			try {
				F f = (F) gmethod.invoke(e);
				if (f != null) {
					res.add(f);
				}
			} catch (IllegalAccessException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (IllegalArgumentException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (InvocationTargetException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		return res;
	}

	public static Method getGetterMethod(Attribute<?, ?> property) {
		Class<?> jt = property.getDeclaringType().getJavaType();
		return getGetterMethodFromField(property.getName(), jt);
	}

	public static Method getSetterMethod(Attribute<?, ?> property, Class<?>... parameterTypes) {
		Class<?> jt = property.getDeclaringType().getJavaType();
		return getSetterMethodFromField(property.getName(), jt, parameterTypes);
	}

	static Method getGetterMethodFromField(String field, Class<?> jt) {
		Method getterMethod = null;
		String getterMethodString = "get" + String.valueOf(field.charAt(0)).toUpperCase() + field.substring(1);

		try {
			getterMethod = jt.getMethod(getterMethodString);
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		}
		return getterMethod;
	}

	static Method getSetterMethodFromField(String field, Class<?> jt, Class<?>... parameterTypes) {
		Method setterMethod = null;
		String setterMethodString = "set" + String.valueOf(field.charAt(0)).toUpperCase() + field.substring(1);

		try {
			setterMethod = jt.getMethod(setterMethodString, parameterTypes);
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		}
		return setterMethod;
	}


	public static List<Predicate> newPredicates() {
		return new ArrayList<Predicate>();
	}
	public static <E extends IIdentity> Optional<E> get(Collection<E> col, long id ) {
		for (E e: col) {
			if (e.getId()==id)
				return Optional.of(e);
		}
		return Optional.<E>empty();
	}
	public static <E extends IIdentity> List<E> getAll(Collection<E> col,Collection<Long> ids ) {
		List<E> list=new ArrayList<E>();
		for (E e: col) {
			if (ids.contains(e.getId()))
				list.add(e);
		}
		return list;
	}


	public static <E extends IIdentity> Optional<E> getFromCollection(Collection<E> col, IIdentity o ) {
		return get(col, o.getId());
	}

	public static void initialize(Object lazy) {
		if (lazy != null)
			Hibernate.initialize(lazy);
	}

	public static void initializeCollection(Collection<?> collection) {
		// works with Hibernate EM 3.6.1-SNAPSHOT
		if (collection == null) {
			return;
		}
		collection.iterator().hasNext();
	}

	public static <E> E[] toArray(Class<E> clz, List<E> list) {
		@SuppressWarnings("unchecked")
		E[] o = (E[]) Array.newInstance(clz, list.size());
		return list.toArray(o);
	}

	public static <E> List<E> getAll(EntityManager em, Class<E> clz, Collection<Long> ids, Hints ...hints ) {
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<E> criteria = cb.createQuery(clz);
		Root<E> root = criteria.from(clz);
		criteria.select(root);
		criteria.where(root.in(ids));
		TypedQuery<E> query = em.createQuery(criteria);
		EntityHelper.applyHints(query, hints);
		return query.getResultList();
	}

	public static <E> List<E> getAll(EntityManager em, Class<E> clz, Hints ... hints) {
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<E> criteria = cb.createQuery(clz);
		Root<E> root = criteria.from(clz);
		criteria.select(root);
		TypedQuery<E> query = em.createQuery(criteria);
		EntityHelper.applyHints(query, hints);
		return query.getResultList();
	}

	public static <E> List<E> getAll(EntityManager em, Class<E> clz, Collection<Long> ids, IRestrictions<E> rest, Hints ... hints) {
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<E> criteria = cb.createQuery(clz);
		Root<E> root = criteria.from(clz);
		criteria.select(root);
		Predicate rrest = rest.apply(cb, criteria, root);
		criteria.where(cb.and(root.in(ids),cb.and(rrest)));
		TypedQuery<E> query = em.createQuery(criteria);
		EntityHelper.applyHints(query, hints);
		applyQueryPage(query, rest);
		return query.getResultList();
	}
	public static <E> List<E> get(EntityManager em, Class<E> clz, IRestrictions<E> rest, Hints ... hints) {
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<E> criteria = cb.createQuery(clz);
		Root<E> root = criteria.from(clz);
		criteria.select(root);
		criteria.where(rest.apply(cb, criteria, root));

		TypedQuery<E> query = em.createQuery(criteria);


		applyQueryPage(query, rest);
		return query.getResultList();
	}

	// can be also done with subquery which may be faster (need to check)
	public static <E extends IIdentity> long getCountGroupedBy(EntityManager em, Class<E> clz, IRestrictions<E> rest, SingularAttribute<E, ?>... groupByAttr) {
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<Long> criteria = cb.createQuery(Long.class);
		Root<E> root = criteria.from(clz);
		Expression<Long> count = cb.count(root.get(IIdentity.id_column));
		criteria.select(count);
		criteria.where(rest.apply(cb, criteria, root));

		List<Expression<?>> groupByPaths = Lists.newArrayList();
		for (SingularAttribute<E, ?> ga : groupByAttr) {
			groupByPaths.add(root.get(ga));
		}
		criteria.groupBy(groupByPaths);
		criteria.orderBy(cb.desc(count));

		TypedQuery<Long> query = em.createQuery(criteria);
		query.setMaxResults(1);
		List<Long> res = query.getResultList();
		if (res.isEmpty())
			return 0;
		return res.get(0);//query.getSingleResult();
	}




	public static <E> E getOne(EntityManager em,IIdentity ident) {
		return (E)em.find(ident.getClass(), ident.getId());
	}


	public static <E> Optional<E> getOne(EntityManager em, Class<E> clz, IRestrictions<E> rest, Hints ... hints) {
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<E> criteria = cb.createQuery(clz);
		Root<E> root = criteria.from(clz);
		criteria.select(root);
		criteria.where(rest.apply(cb, criteria, root));
		TypedQuery<E> query = em.createQuery(criteria);
		EntityHelper.applyHints(query, hints);
		query.setMaxResults(1);
		List<E> res = query.getResultList();
		if (!res.isEmpty())
			return Optional.ofNullable(res.get(0));
		return Optional.empty();
		// return query.getSingleResult();
	}

	public static <E> Optional<E> getOne(EntityManager em, Class<E> clz, Hints ... hints) {
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<E> criteria = cb.createQuery(clz);
		Root<E> root = criteria.from(clz);
		criteria.select(root);
		TypedQuery<E> query = em.createQuery(criteria);
		EntityHelper.applyHints(query, hints);
		query.setMaxResults(1);
		if (!query.getResultList().isEmpty())
			return Optional.ofNullable(query.getResultList().get(0));
		return Optional.empty();
		// return query.getSingleResult();
	}



	public static boolean containsIds(Collection<? extends IIdentity> col, long... ids) {
		for (long i : ids) {
			boolean ok = false;
			for (IIdentity i2 : col) {
				if (i == i2.getId()) {
					ok = true;
					break;
				}
			}
			if (!ok) {
				return false;
			}
		}
		return true;
	}

	public static Set<Long> toIdSet(Collection<? extends IIdentity> col) {
		Set<Long> set = new HashSet<Long>();
		if (col != null)
			for (IIdentity i : col) {
				set.add(i.getId());
			}
		return set;
	}

	public static List<Long> toIdList(Collection<? extends IIdentity> col) {
		List<Long> set = new ArrayList<Long>();
		if (col != null)
			for (IIdentity i : col) {
				set.add(i.getId());
			}
		return set;
	}

	public static <E extends IIdentity> Map<Long, E> toMap(Collection<E> col) {
		Map<Long, E> set = new HashMap<Long, E>();
		for (E i : col) {
			set.put(i.getId(), i);
		}
		return set;
	}

	public static <F, E> Set<F> toSet(Collection<E> col, Function<E, F> y) {
		Set<F> set = new HashSet<F>();
		for (E i : col) {
			set.add(y.apply(i));
		}
		return set;
	}

	public static <F, E> List<F> toList(Collection<E> col, Function<E, F> y) {
		List<F> list = new ArrayList<F>();
		for (E i : col) {
			list.add(y.apply(i));
		}
		return list;
	}

	public static <F, E> Map<F, E> toMap(Collection<E> col, Function<E, F> y) {
		Map<F, E> set = new HashMap<F, E>();
		for (E i : col) {
			set.put(y.apply(i), i);
		}
		return set;
	}

	public static <F, E> Multimap<F, E> toMultimap(Collection<E> col, Function<E, F> y) {
		Multimap<F, E> set = ArrayListMultimap.create();
		for (E i : col) {
			set.put(y.apply(i), i);
		}
		return set;
	}

	public static <F, E, G> Multimap<F, G> toMultimap(Collection<E> col, MultimapYield<E, F, G> y) {
		Multimap<F, G> set = ArrayListMultimap.create();
		for (E i : col) {
			y.put(i, set);
		}
		return set;
	}
	public static <E extends IIdentity> E getObjectWithMaxId(Collection<E> col) {
		long ident=0;
		E emax=null;
		for (E e: col) {
			if (ident<e.getId()) {
				ident=e.getId();
				emax=e;
			}
		}
		return emax;
	}
	public static interface MultimapYield<E, F, G> {
		public void put(E e, Multimap<F, G> map);
	}

	public static interface YieldList<E, F> {
		List<F> yield(E e);
	}

	public static interface YieldLong<E> {
		Long yield(E e);
	}

	public static <E> Set<Long> filter(Collection<E> col, YieldLong<E> y) {
		Set<Long> set = new HashSet<Long>();
		for (E i : col) {
			set.add(y.yield(i));
		}
		return set;
	}

	public static <E> long getCount(EntityManager em, Class<E> clz) {
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<Long> criteria = cb.createQuery(Long.class);
		Root<E> root = criteria.from(clz);
		criteria.select(cb.count(root));
		return em.createQuery(criteria).getSingleResult();
	}

	public static <E> long getCount(EntityManager em, Class<E> clz, IRestrictions<E> rest) {
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<Long> criteria = cb.createQuery(Long.class);
		Root<E> root = criteria.from(clz);
		criteria.select(cb.count(root));
		criteria.where(rest.apply(cb, criteria, root));
		return em.createQuery(criteria).getSingleResult();
	}

	public static <E> int getFieldSumAsDouble(EntityManager em, SingularAttribute<E, Integer> field, IRestrictions<E> rest) {
		Class<E> clz = field.getDeclaringType().getJavaType();
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<Long> criteria = cb.createQuery(Long.class);
		Root<E> root = criteria.from(clz);
		criteria.select(cb.sumAsLong(root.get(field)));
		criteria.where(rest.apply(cb, criteria, root));
		Long res = em.createQuery(criteria).getSingleResult();
		if (res == null)
			return 0;
		return res.intValue();
	}
	private static <E> void applyQueryPage(TypedQuery<E> query, IRestrictions<E> rest) {
		if (rest instanceof IQueryPage) {
			IQueryPage ob = (IQueryPage) rest;
			ob.getRows().applyToQuery(query);
		}
	}

	public static void sort(List<? extends IIdentity> list) {
		Collections.sort(list, new Comparator<IIdentity>() {

			public int compare(IIdentity o1, IIdentity o2) {
				return ComparisionHelper.compare(o1.getId(), o2.getId());
			}
		});
	}



}
