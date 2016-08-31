package challenger.utils.odb;

import com.challenger.eviauth.odb.api.IIdentity;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import org.jinq.jpa.JPAJinqStream;
import org.jinq.jpa.JinqJPAStreamProvider;
import org.jinq.orm.stream.JinqStream;
import org.springframework.beans.factory.annotation.Autowired;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.metamodel.ListAttribute;
import javax.persistence.metamodel.SetAttribute;
import javax.persistence.metamodel.SingularAttribute;
import java.lang.reflect.Method;
import java.util.*;

public class AnyDAO {
	// @Autowired
	// Logger logger;

	@Autowired
	@PersistenceContext
	public EntityManager em;

	public void initialize(Object o) {
		EntityHelper.initialize(o);
	}

	public void initializeCollection(Collection<?> collection) {
		EntityHelper.initializeCollection(collection);
	}

	public void removeAll(Collection<? extends IIdentity> objs) {
		for (IIdentity obj : objs)
			remove(obj);
	}

	public void removeAll(Collection<? extends IIdentity> objs, Class clz) {
		for (IIdentity obj : objs)
			remove(obj, clz);
	}

	public EntityManager getEm() {
		return em;
	}

	public <E extends IIdentity, F> int update(E obj, SingularAttribute<E, F> set, final F newValue) {
		if (obj == null) {
			return 0;
		}
		Class<E> clz = set.getDeclaringType().getJavaType();
		CriteriaBuilder _ = getEm().getCriteriaBuilder();
		CriteriaUpdate<E> update = _.createCriteriaUpdate(clz);
		Root<E> root = update.from(clz);
		update.set(set, newValue);
		update.where(_.equal(root.get(IIdentity.id_column), obj.getId()));
		return getEm().createQuery(update).executeUpdate();
	}

	public <E> int clearTableCompletely(Class<E> clz) {
		CriteriaDelete<E> delete = getEm().getCriteriaBuilder().createCriteriaDelete(clz);
		delete.from(clz);
		Query query = getEm().createQuery(delete);
		int rowCount = query.executeUpdate();
		return rowCount;
	}

	public void remove(IIdentity obj) {
		remove(obj, obj.getClass());
	}

	public void remove(IIdentity obj, Class clz) {
		IIdentity objDB = (IIdentity) em.find(clz, obj.getId());
		if (objDB != null)
			em.remove(objDB);
	}

	public <E, F> int removeByField(final SingularAttribute<E, F> attr, final F f) {
		return remove(attr.getDeclaringType().getJavaType(), new IDeleteRestrictions<E>() {
			@Override
			public Predicate apply(CriteriaBuilder cb, CriteriaDelete<?> cq, Root<E> root) {
				return cb.equal(root.get(attr), f);
			}
		});
	}

	public <E, F> int removeByFieldPath(final F f, final SingularAttribute<?, ?>... attrs) {
		return remove((Class)attrs[0].getDeclaringType().getJavaType(), new ISubqueryRestrictions<E>() {
			@Override
			public Predicate apply(CriteriaBuilder cb, Subquery<?> cq, Root<E> root) {
				
				Path path=root;
				for (SingularAttribute s: attrs) {
					path=path.get(s);
				}
				// TODO Auto-generated method stub
				return cb.equal(path, f);
			}
		});
		
	}

	public <E extends IIdentity> void reload(Holder<E> holder) {

		if (holder.getHold() == null || holder.getHold().getId()<=0)
			return;
		else {
			Optional<E> reloaded = Optional.ofNullable(reload(holder.getHold()));
			holder.setHolded(reloaded.orElse(null));
		}
	}

	public <E extends IIdentity> Optional<E> reloadIfPersistent(Optional<E> obj) {
		if (!obj.isPresent() || obj.get().getId()<=0)
			return obj;
		else
			return Optional.ofNullable(reload(obj.get()));

	}

	public <E extends IIdentity> E reloadIfPersistent(E obj) {
		if (obj.getId()<=0)
			return obj;
		else
			return reload(obj);

	}

	public <E extends IIdentity> E reloadIfDetached(E obj) {
		if (!em.contains(obj)) {
			return reload(obj);
		}
		return obj;
	}

	public <E extends IIdentity> E reload(E obj) {
		if (obj == null)
			return null;
		int index = obj.getClass().getName().indexOf("_$$_");
		if (index != -1) {
			index = obj.getClass().getName().indexOf("_$$_javassist_");
			if (index == -1)
				index = obj.getClass().getName().indexOf("_$$_jvst"); //$$_jvst553_9
		}
		Class<?> clz = null;
		if (index == -1) {
			clz = obj.getClass();
		} else {
			try {
				clz = Class.forName(obj.getClass().getName().substring(0, index));
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		E objDB = (E) em.find(clz, obj.getId());
		return objDB;
	}

	/**
	 * Reloads objects from database and puts them in same order in results list
	 * If object is not found in DB it won't be included in results list
	 * 
	 * @param objs
	 * @param clz
	 * @param hints
	 * @return
	 */
	public <E extends IIdentity> List<E> reloadList(List<E> objs, Class<E> clz, Hints... hints) {
		List<E> result = new ArrayList<E>();
		Set<Long> idSet = EntityHelper.toIdSet(objs);
		if (idSet.isEmpty())
			return result;
		Map<Long, E> reloaded = EntityHelper.toMap(getAll(clz, idSet, hints));
		for (E o : objs) {
			E odDB = reloaded.get(o.getId());
			if (odDB != null) {
				result.add(odDB);
			}
		}
		return result;
	}



	public <E> int remove(Class<E> clz, IDeleteRestrictions<E> restr) {
		CriteriaBuilder cb = getEm().getCriteriaBuilder();
		CriteriaDelete<E> delete = cb.createCriteriaDelete(clz);
		Root<E> root = delete.from(clz);
		delete.where(restr.apply(cb, delete, root));
		Query query = getEm().createQuery(delete);
		int rowCount = query.executeUpdate();
		return rowCount;
	}

	public <E> int remove(Class<E> clz, ISubqueryRestrictions<E> restr) {
		CriteriaBuilder cb = getEm().getCriteriaBuilder();
		CriteriaDelete<E> delete = cb.createCriteriaDelete(clz);
		Root<E> root = delete.from(clz);

		Subquery<E> s = delete.subquery(clz);

		Root<E> root2 = s.from(clz);
		s.select(root2);
		s.where(restr.apply(cb, s, root2));

		delete.where(root.in(s));
		Query query = getEm().createQuery(delete);
		int rowCount = query.executeUpdate();
		return rowCount;
	}
	
	public <E,F> int update(SingularAttribute<E,F> se, F newVal, ISubqueryRestrictions<E> restr) {
		CriteriaBuilder cb = getEm().getCriteriaBuilder();
		Class<E> clz=se.getDeclaringType().getJavaType();
		CriteriaUpdate<E> update = cb.createCriteriaUpdate(clz);
		Root<E> root = update.from(clz);
		update.set(root.get(se), newVal);

		Subquery<E> s = update.subquery(clz);

		Root<E> root2 = s.from(clz);
		s.select(root2);
		s.where(restr.apply(cb, s, root2));

		update.where(root.in(s));
		Query query = getEm().createQuery(update);
		int rowCount = query.executeUpdate();
		return rowCount;
	}

	public <E extends IIdentity> List<Long> getIds(Class<E> clz, Hints... hints) {
		return getIds(clz, null, hints);
	}

	public <E extends IIdentity> List<Long> getIds(Class<E> clz, IRestrictions<E> restrictions, Hints... hints) {
		CriteriaBuilder criteriaBuilder = getEm().getCriteriaBuilder();
		CriteriaQuery<Long> criteriaQuery = criteriaBuilder.createQuery(Long.class);
		Root<E> root = criteriaQuery.from(clz);
		criteriaQuery.select((Selection) root.get(IIdentity.id_column));
		if (restrictions != null)
			restrictions.apply(criteriaBuilder, criteriaQuery, root);
		TypedQuery<Long> query = getEm().createQuery(criteriaQuery);
		EntityHelper.applyHints(query, hints);
		return query.getResultList();
	}

	public <E, F> List<E> getAll(Class<E> clz, final SingularAttribute<E, F> attr, final F obj, Hints... hints) {
		return EntityHelper.get(getEm(), clz, new IRestrictions<E>() {
			@Override
			public Predicate apply(CriteriaBuilder _, CriteriaQuery<?> cq, Root<E> root) {
				return _.equal(root.get(attr), obj);
			}
		}, hints);
	}

	
	public <E,F> List<E> getByField(F obj, SingularAttribute<E, F> sa) {
		return EntityHelper.get(getEm(), sa.getDeclaringType().getJavaType(), new IRestrictions<E>() {
			@Override
			public Predicate apply(CriteriaBuilder _, CriteriaQuery<?> cq, Root<E> root) {
				return _.equal(root.get(sa), obj);
			}
		});
	}
	public <E,F> List<E> getByFieldPath(F obj, SingularAttribute<E, ?> sa, SingularAttribute<?, ?> ... sas) {
		return EntityHelper.get(getEm(), sa.getDeclaringType().getJavaType(), new IRestrictions<E>() {
			@Override
			public Predicate apply(CriteriaBuilder _, CriteriaQuery<?> cq, Root<E> root) {
				
				Path path=root;
				path=path.get(sa);
				for (SingularAttribute s: sas) {
					path=path.get(s);
				}
				
				return _.equal(path, obj);
			}
		});
	}
	
	public class Crit<E> {
		List<SingularAttribute<E, Object>> ef = Lists.newArrayList();
		List<Object> object = Lists.newArrayList();

		public <F> Crit<E> and(SingularAttribute<E, F> sa, F f) {
			ef.add((SingularAttribute) sa);
			object.add(f);
			return this;
		}

		public List<E> getAll(Hints... hints) {
			return EntityHelper.get(getEm(), ef.get(0).getDeclaringType().getJavaType(), new IRestrictions<E>() {
				@Override
				public Predicate apply(CriteriaBuilder _, CriteriaQuery<?> cq, Root<E> root) {
					Predicates p = new Predicates(_);

					for (int i = 0; i < ef.size(); i++) {
						p.add(_.equal(root.get(ef.get(i)), object.get(i)));

					}
					return p.getAsOne();
				}
			}, hints);
		}

	}

	public <E, F> Crit<E> crit(SingularAttribute<E, F> sa, F f) {
		Crit<E> c = new Crit<E>();
		c.and(sa, f);
		return c;
	}

	public <E, F> List<E> getAll(final SingularAttribute<E, F> attr, final F obj, Hints... hints) {
		return EntityHelper.get(getEm(), attr.getDeclaringType().getJavaType(), new IRestrictions<E>() {
			@Override
			public Predicate apply(CriteriaBuilder _, CriteriaQuery<?> cq, Root<E> root) {
				return _.equal(root.get(attr), obj);
			}
		}, hints);
	}

	public <E, F> List<E> getAll(final SingularAttribute<E, F> attr, final List<F> objList, Hints... hints) {
		return EntityHelper.get(getEm(), attr.getDeclaringType().getJavaType(), new IRestrictions<E>() {
			@Override
			public Predicate apply(CriteriaBuilder _, CriteriaQuery<?> cq, Root<E> root) {
				return root.get(attr).in(objList);
			}
		}, hints);
	}

	public <E, F> List<E> getAll(final SingularAttribute<E, F> attr, final Collection<F> obj, Hints... hints) {
		return EntityHelper.get(getEm(), attr.getDeclaringType().getJavaType(), new IRestrictions<E>() {
			@Override
			public Predicate apply(CriteriaBuilder _, CriteriaQuery<?> cq, Root<E> root) {
				return root.get(attr).in(obj);
			}
		}, hints);
	}

	public <E, F> List<E> getAll(Class<E> clz, final SingularAttribute<E, F> attr, final Collection<F> obj, Hints... hints) {
		return EntityHelper.get(getEm(), clz, new IRestrictions<E>() {
			@Override
			public Predicate apply(CriteriaBuilder _, CriteriaQuery<?> cq, Root<E> root) {
				return root.get(attr).in(obj);
			}
		}, hints);
	}

	public <E> List<E> getAll(Class<E> clz, Collection<Long> ids, Hints... hints) {
		return EntityHelper.getAll(em, clz, ids, hints);
	}

	public <E> List<E> getAll(Class<E> clz, Collection<Long> ids, IRestrictions<E> restr, Hints... hints) {
		return EntityHelper.getAll(em, clz, ids, restr, hints);
	}

	public <E> List<E> getAll(Class<E> clz, Hints... hints) {
		return EntityHelper.getAll(em, clz, hints);
	}



	public <E> List<E> get(Class<E> clz, IRestrictions<E> rest, Hints... hints) {
		return EntityHelper.get(em, clz, rest, hints);
	}

	public <E> List<E> get(Class<E> clz, Optional<IRestrictions<E>> rest, Hints... hints) {
		if (rest.isPresent())
			return EntityHelper.get(em, clz, rest.get(), hints);
		else
			return EntityHelper.getAll(em, clz, hints);
	}

	public <E> Optional<E> getOne(Class<E> clz, IRestrictions<E> rest, Hints... hints) {
		return EntityHelper.getOne(em, clz, rest, hints);
	}

	public <E> Optional<E> getOne(Class<E> clz, Hints... hints) {
		return EntityHelper.getOne(em, clz, hints);
	}

	public <E extends IIdentity, F extends IIdentity> void loadFor(Collection<? extends E> objs, SingularAttribute<E, F> se, Hints... hints) {
		MapIdentityLoader<E, F> loader = new MapIdentityLoader<E, F>();
		loader.objsLoaded = EntityHelper.toMap(objs);
		loadFor(loader, se, hints);
	}

	public <E extends IIdentity, F extends IIdentity> void loadList(Collection<E> objs, ListAttribute<E, F> res, SingularAttribute<F, E> se, Hints... hints) {
		MapIdentityLoader<E, F> loader = new MapIdentityLoader<E, F>();
		loader.objsLoaded = EntityHelper.toMap(objs);
		loadList(loader, res, se, hints);
	}




	public <E extends IIdentity, F extends IIdentity> void loadSet(Collection<E> objs, SetAttribute<E, F> res, SingularAttribute<F, E> se, Hints... hints) {
		MapIdentityLoader<E, F> loader = new MapIdentityLoader<E, F>();
		loader.objsLoaded = EntityHelper.toMap(objs);
		loadSet(loader, res, se, hints);
	}

	public <E extends IIdentity, F extends IIdentity> void loadObjWithIn(Collection<E> objs, SingularAttribute<E, F> res, SingularAttribute<F, E> se,
			Hints... hints) {
		CollectionIdentityLoader<E, F> loader = new CollectionIdentityLoader<E, F>();
		loader.objsLoaded = objs;
		loadObjWithIn(loader, res, se, hints);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public <E extends IIdentity, F extends IIdentity> void loadSet(MapIdentityLoader<E, F> loader, SetAttribute<E, F> res, SingularAttribute<F, E> se,
			Hints... hints) {
		if (loader.objsLoaded.isEmpty())
			return;
		CriteriaBuilder _ = em.getCriteriaBuilder();

		Class<F> jt = se.getDeclaringType().getJavaType();
		CriteriaQuery<Object[]> q = _.createQuery(Object[].class);
		Root<F> from = q.from(jt);
		Path<E> path = from.get(se);
		q.multiselect(path.get(IIdentity.id_column), from);

		Predicates p = new Predicates(_);
		p.add(path.get(IIdentity.id_column).in(loader.objsLoaded.keySet()));
		p.addIfPresent(loader.restrToLoad, q, from);
		p.addIfPresent(loader.restrLoaded, q, path);
		q.where(p.getAsOne());
		// q.where(from.get(se).get(IIdentity.id_column).in(objs.keySet()));
		String setterMethod = "set" + String.valueOf(res.getName().charAt(0)).toUpperCase() + res.getName().substring(1);
		try {
			Method method = se.getBindableJavaType().getMethod(setterMethod, Set.class);
			TypedQuery<Object[]> query = em.createQuery(q);
			EntityHelper.applyHints(query, hints);
			List<Object[]> resultList = query.getResultList();
			Multimap<Long, F> multimap = ArrayListMultimap.create();
			for (Object[] o : resultList) {

				Long id = (Long) o[0];
				F f = (F) o[1];
				Collection<F> collection = multimap.get(id);
				collection.add(f);
			}
			for (E e : loader.objsLoaded.values()) {
				method.invoke(e, new HashSet(multimap.get(e.getId())));
			}

		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}

	@SuppressWarnings({ "unchecked" })
	public <E extends IIdentity, F extends IIdentity> void loadObjWithIn(CollectionIdentityLoader<E, F> loader, SingularAttribute<E, F> res,
			SingularAttribute<F, E> se, Hints... hints) {
		if (loader.objsLoaded.isEmpty())
			return;
		Set<Long> idSet = EntityHelper.toIdSet(loader.objsLoaded);
		CriteriaBuilder _ = em.getCriteriaBuilder();

		Class<F> jt = se.getDeclaringType().getJavaType();
		CriteriaQuery<Object[]> q = _.createQuery(Object[].class);
		Root<F> from = q.from(jt);
		Path<E> path = from.get(se);
		q.multiselect(path.get(IIdentity.id_column), from);

		Predicates p = new Predicates(_);
		p.add(path.get(IIdentity.id_column).in(idSet));
		p.addIfPresent(loader.restrToLoad, q, from);
		p.addIfPresent(loader.restrLoaded, q, path);
		q.where(p.getAsOne());

		// q.where(from.get(se).get(IIdentity.id_column).in(idSet));
		String setterMethod = "set" + String.valueOf(res.getName().charAt(0)).toUpperCase() + res.getName().substring(1);
		try {
			Method method = se.getBindableJavaType().getMethod(setterMethod, jt);
			TypedQuery<Object[]> query = em.createQuery(q);
			EntityHelper.applyHints(query, hints);
			List<Object[]> resultList = query.getResultList();
			Map<Long, F> multimap = new HashMap<Long, F>();
			for (Object[] o : resultList) {

				Long id = (Long) o[0];
				F f = (F) o[1];
				multimap.put(id, f);
			}
			for (E e : loader.objsLoaded) {
				method.invoke(e, multimap.get(e.getId()));
			}

		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public <E extends IIdentity, F extends IIdentity> void loadList(MapIdentityLoader<E, F> loader, ListAttribute<E, F> res, SingularAttribute<F, E> se,
			Hints... hints) {
		if (loader.objsLoaded.isEmpty())
			return;
		CriteriaBuilder _ = em.getCriteriaBuilder();

		Class<F> jt = se.getDeclaringType().getJavaType();
		CriteriaQuery<Object[]> q = _.createQuery(Object[].class);
		Root<F> from = q.from(jt);
		Path<E> path = from.get(se);
		q.multiselect(path.get(IIdentity.id_column), from);

		Predicates p = new Predicates(_);
		p.add(path.get(IIdentity.id_column).in(loader.objsLoaded.keySet()));
		p.addIfPresent(loader.restrToLoad, q, from);
		p.addIfPresent(loader.restrLoaded, q, path);
		q.where(p.getAsOne());
		// q.where(from.get(se).get(IIdentity.id_column).in(objs.keySet()));

		String setterMethod = "set" + String.valueOf(res.getName().charAt(0)).toUpperCase() + res.getName().substring(1);
		try {
			Method method = se.getBindableJavaType().getMethod(setterMethod, List.class);
			TypedQuery<Object[]> query = em.createQuery(q);
			EntityHelper.applyHints(query, hints);
			List<Object[]> resultList = query.getResultList();
			Multimap<Long, F> multimap = ArrayListMultimap.create();
			for (Object[] o : resultList) {

				Long id = (Long) o[0];
				F f = (F) o[1];
				Collection<F> collection = multimap.get(id);
				collection.add(f);
			}
			for (E e : loader.objsLoaded.values()) {
				method.invoke(e, new ArrayList(multimap.get(e.getId())));
			}

		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}

	public static class AbstractIdentityLoader<E extends IIdentity, F extends IIdentity> {
		Optional<IPathRestrictions<E>> restrLoaded = Optional.empty();
		Optional<IPathRestrictions<F>> restrToLoad = Optional.empty();

		Map<Long, ? extends E> objsLoaded;

		public void setRestrLoaded(IPathRestrictions<E> restrLoaded) {
			this.restrLoaded = Optional.ofNullable(restrLoaded);
		}

		public void setRestrToLoad(IPathRestrictions<F> restrToLoad) {
			this.restrToLoad = Optional.ofNullable(restrToLoad);
		}
	}

	public static class MapIdentityLoader<E extends IIdentity, F extends IIdentity> extends AbstractIdentityLoader<E, F> {
		Map<Long, ? extends E> objsLoaded;
	}

	public static class CollectionIdentityLoader<E extends IIdentity, F extends IIdentity> extends AbstractIdentityLoader<E, F> {
		Collection<? extends E> objsLoaded;
	}

	@SuppressWarnings({ "unchecked" })
	public <E extends IIdentity, F extends IIdentity> void loadFor(MapIdentityLoader<E, F> loader, SingularAttribute<E, F> se, Hints... hints) {
		if (loader.objsLoaded.isEmpty())
			return;
		CriteriaBuilder _ = em.getCriteriaBuilder();

		Class<E> jt = se.getDeclaringType().getJavaType();
		CriteriaQuery<Object[]> q = _.createQuery(Object[].class);
		Root<E> from = q.from(jt);
		Path<F> path = from.get(se);
		q.multiselect(from.get(IIdentity.id_column), path);

		Predicates p = new Predicates(_);
		p.add(from.get(IIdentity.id_column).in(loader.objsLoaded.keySet()));
		p.addIfPresent(loader.restrLoaded, q, from);
		p.addIfPresent(loader.restrToLoad, q, path);
		q.where(p.getAsOne());

		String setterMethod = "set" + String.valueOf(se.getName().charAt(0)).toUpperCase() + se.getName().substring(1);
		try {
			Method method = jt.getMethod(setterMethod, se.getBindableJavaType());
			TypedQuery<Object[]> query = em.createQuery(q);

			EntityHelper.applyHints(query, hints);

			List<Object[]> resultList = query.getResultList();
			for (Object[] o : resultList) {
				Long id = (Long) o[0];
				F f = (F) o[1];
				E e = loader.objsLoaded.get(id);

				method.invoke(e, f);

			}
		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}

	public <E> List<E> getAll(Class<E> clz, Collection<Long> ids) {
		if (ids.isEmpty())
			return Lists.newArrayList();
		return EntityHelper.getAll(em, clz, ids);
	}

	public <E> List<E> getAll(Class<E> clz, Collection<Long> ids, IRestrictions<E> restr) {
		if (ids.isEmpty())
			return Lists.newArrayList();
		return EntityHelper.getAll(em, clz, ids, restr);
	}

	public <E> E get(Class<E> clz, long id) {
		return em.find(clz, id);
	}

	public <E> List<E> getAll(Class<E> clz) {
		return EntityHelper.getAll(em, clz);
	}



	public <E> List<E> get(Class<E> clz, IRestrictions<E> rest) {
		return EntityHelper.get(em, clz, rest);
	}

	public <E> List<E> get(Class<E> clz, Optional<IRestrictions<E>> rest) {
		if (rest.isPresent())
			return EntityHelper.get(em, clz, rest.get());
		else
			return EntityHelper.getAll(em, clz);
	}

	public <E> Optional<E> getOne(Class<E> clz, IRestrictions<E> rest) {
		return EntityHelper.getOne(em, clz, rest);
	}

	public <E> Optional<E> getOne(Class<E> clz) {
		return EntityHelper.getOne(em, clz);
	}

	public static <E extends IIdentity> long getMaxCountGroupedBy(EntityManager em, Class<E> clz, IRestrictions<E> rest, SingularAttribute<E, ?>... groupByAttr) {
		return EntityHelper.getCountGroupedBy(em, clz, rest, groupByAttr);
	}

	public <E extends IIdentity, F extends IIdentity> void loadFor(Collection<E> objs, SingularAttribute<E, F> se) {
		Collection<E> col = Lists.newArrayList();
		for (E e : objs) {
			if (!em.contains(Exprs.get(e, se))) {
				col.add(e);
			}
		}

		loadFor(EntityHelper.toMap(col), se);
	}

	public <E extends IIdentity, F extends IIdentity> void loadList(Collection<E> objs, ListAttribute<E, F> res, SingularAttribute<F, E> se) {
		loadList(EntityHelper.toMap(objs), res, se);
	}

	/**
	 * loads e.collection_f (doesn't load f.collection_e)
	 * @param objs
	 * @param res
	 */
	public <E extends IIdentity, F extends IIdentity> void loadListManyToMany(Collection<E> objs, ListAttribute<E, F> res) {
		loadListManyToMany(EntityHelper.toMap(objs), res);
	}

	public <E extends IIdentity, F extends IIdentity> void loadSet(Collection<E> objs, SetAttribute<E, F> res, SingularAttribute<F, E> se) {
		loadSet(EntityHelper.toMap(objs), res, se);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public <E extends IIdentity, F extends IIdentity> void loadSet(Map<Long, E> objs, SetAttribute<E, F> res, SingularAttribute<F, E> se) {
		if (objs.isEmpty())
			return;
		CriteriaBuilder _ = em.getCriteriaBuilder();

		Class<F> jt = se.getDeclaringType().getJavaType();
		CriteriaQuery<Object[]> q = _.createQuery(Object[].class);
		Root<F> from = q.from(jt);

		q.multiselect(from.get(se).get(IIdentity.id_column), from);

		q.where(from.get(se).get(IIdentity.id_column).in(objs.keySet()));
		String setterMethod = "set" + String.valueOf(res.getName().charAt(0)).toUpperCase() + res.getName().substring(1);
		try {
			Method method = se.getBindableJavaType().getMethod(setterMethod, Set.class);
			TypedQuery<Object[]> query = em.createQuery(q);
			List<Object[]> resultList = query.getResultList();
			Multimap<Long, F> multimap = ArrayListMultimap.create();
			for (Object[] o : resultList) {

				Long id = (Long) o[0];
				F f = (F) o[1];
				Collection<F> collection = multimap.get(id);
				collection.add(f);
			}
			for (E e : objs.values()) {
				method.invoke(e, new HashSet(multimap.get(e.getId())));
			}

		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public <E extends IIdentity, F extends IIdentity> void loadObjWithIn(Collection<E> objs, SingularAttribute<E, F> res, SingularAttribute<F, E> se) {
		if (objs.isEmpty())
			return;
		Set<Long> idSet = EntityHelper.toIdSet(objs);
		CriteriaBuilder _ = em.getCriteriaBuilder();

		Class<F> jt = se.getDeclaringType().getJavaType();
		CriteriaQuery<Object[]> q = _.createQuery(Object[].class);
		Root<F> from = q.from(jt);

		q.multiselect(from.get(se).get(IIdentity.id_column), from);

		q.where(from.get(se).get(IIdentity.id_column).in(idSet));
		String setterMethod = "set" + String.valueOf(res.getName().charAt(0)).toUpperCase() + res.getName().substring(1);
		try {
			Method method = se.getBindableJavaType().getMethod(setterMethod, jt);
			TypedQuery<Object[]> query = em.createQuery(q);
			List<Object[]> resultList = query.getResultList();
			Map<Long, F> multimap = new HashMap<Long, F>();
			for (Object[] o : resultList) {

				Long id = (Long) o[0];
				F f = (F) o[1];
				multimap.put(id, f);
			}
			for (E e : objs) {
				method.invoke(e, multimap.get(e.getId()));
			}

		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public <E extends IIdentity, F extends IIdentity> void loadListManyToMany(Map<Long, E> objs, ListAttribute<E, F> res) {

		if (objs.isEmpty())
			return;

		CriteriaBuilder _ = em.getCriteriaBuilder();

		Class<E> eClz = res.getDeclaringType().getJavaType();
		Class<F> fClz = res.getElementType().getJavaType();

		CriteriaQuery<Object[]> q = _.createQuery(Object[].class);
		Root<E> from = q.from(eClz);
		ListJoin<E, F> join = from.join(res);
		q.multiselect(from.get(IIdentity.id_column), join.get(IIdentity.id_column));
		q.where(from.in(objs.keySet()));
		TypedQuery<Object[]> query = em.createQuery(q);
		List<Object[]> resultList = query.getResultList();
		// we have all needed id-s

		Multimap<Long, F> mmap = ArrayListMultimap.create();
		//Set<Long> fToLoad = Sets.newHashSet();
		//for (Object[] l : resultList) {
		//fToLoad.add((Long) l[1]);
		//F fObj = em.getReference(fClz, l[1]);

		//System.out.println("CONS " + cons);
		//boolean cons = em.contains(fObj);
		//System.out.println("CONS " + cons);
		//}
		//List<F> all = getAll(fClz, fToLoad);

		//Map<Long, F> fMap = EntityHelper.toMap(all);
		for (Object[] l : resultList) {
			mmap.put((Long) l[0], em.getReference(fClz, l[1]));//fMap.get(l[1])); // e.id, f
		}

		from.join(res);

		String setterMethod = "set" + String.valueOf(res.getName().charAt(0)).toUpperCase() + res.getName().substring(1);
		try {
			Method method = eClz.getMethod(setterMethod, List.class);

			for (E e : objs.values()) {
				Collection<F> ress = mmap.get(e.getId());
				method.invoke(e, new ArrayList(ress));
			}

		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public <E extends IIdentity, F extends IIdentity> void loadList(Map<Long, E> objs, ListAttribute<E, F> res, SingularAttribute<F, E> se) {
		if (objs.isEmpty())
			return;
		CriteriaBuilder _ = em.getCriteriaBuilder();

		Class<F> jt = se.getDeclaringType().getJavaType();
		CriteriaQuery<Object[]> q = _.createQuery(Object[].class);
		Root<F> from = q.from(jt);

		q.multiselect(from.get(se).get(IIdentity.id_column), from);

		q.where(from.get(se).get(IIdentity.id_column).in(objs.keySet()));
		String setterMethod = "set" + String.valueOf(res.getName().charAt(0)).toUpperCase() + res.getName().substring(1);
		try {
			Method method = se.getBindableJavaType().getMethod(setterMethod, List.class);
			TypedQuery<Object[]> query = em.createQuery(q);
			List<Object[]> resultList = query.getResultList();
			Multimap<Long, F> multimap = ArrayListMultimap.create();
			for (Object[] o : resultList) {

				Long id = (Long) o[0];
				F f = (F) o[1];
				Collection<F> collection = multimap.get(id);
				collection.add(f);
			}
			for (E e : objs.values()) {
				method.invoke(e, new ArrayList(multimap.get(e.getId())));
			}

		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}

	@SuppressWarnings({ "unchecked" })
	public <E extends IIdentity, F extends IIdentity> void loadFor(Map<Long, E> objs, SingularAttribute<E, F> se) {
		if (objs.isEmpty())
			return;
		CriteriaBuilder _ = em.getCriteriaBuilder();

		Class<E> jt = se.getDeclaringType().getJavaType();
		CriteriaQuery<Object[]> q = _.createQuery(Object[].class);
		Root<E> from = q.from(jt);
		Path<F> path = from.get(se);
		q.multiselect(from.get(IIdentity.id_column), path);
		q.where(from.get(IIdentity.id_column).in(objs.keySet()));

		String setterMethod = "set" + String.valueOf(se.getName().charAt(0)).toUpperCase() + se.getName().substring(1);
		try {
			Method method = jt.getMethod(setterMethod, se.getBindableJavaType());
			TypedQuery<Object[]> query = em.createQuery(q);
			List<Object[]> resultList = query.getResultList();
			for (Object[] o : resultList) {
				Long id = (Long) o[0];
				F f = (F) o[1];
				E e = objs.get(id);

				method.invoke(e, f);

			}
		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}

	public <E> long getCount(Class<E> clz, IRestrictions<E> rest) {
		return EntityHelper.getCount(em, clz, rest);
	}

	public <E> long getCount(Class<E> clz) {
		return EntityHelper.getCount(em, clz);
	}

	public <E, F> F getField(long id, SingularAttribute<E, F> sa, Hints... hints) {
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

	public <E> int getFieldSumAsDouble(SingularAttribute<E, Integer> field, IRestrictions<E> rest) {
		return EntityHelper.getFieldSumAsDouble(em, field, rest);
	}

	public <E extends Object> int update(Class<E> clz, IUpdateRestrictions<E> restr) {
		CriteriaBuilder _ = getEm().getCriteriaBuilder();
		CriteriaUpdate<E> update = _.createCriteriaUpdate(clz);
		Root<E> root = update.from(clz);

		CriteriaUpdate<?> where = restr.applySetAndWhere(_, update, root);

		return getEm().createQuery(where).executeUpdate();
	}
	
	
	public <E,F> int nullify(SingularAttribute<E,F> se, ISubqueryRestrictions<E> restr) {
		CriteriaBuilder cb = getEm().getCriteriaBuilder();
		Class<E> clz=se.getDeclaringType().getJavaType();
		Class<F> bindableJavaType = se.getBindableJavaType();
		CriteriaUpdate<E> update = cb.createCriteriaUpdate(clz);
		Root<E> root = update.from(clz);
		update.set(root.get(se), cb.nullLiteral(bindableJavaType));
 
		Subquery<E> s = update.subquery(clz);

		Root<E> root2 = s.from(clz);
		s.select(root2);
		s.where(restr.apply(cb, s, root2));

		update.where(root.in(s));
		Query query = getEm().createQuery(update);
		int rowCount = query.executeUpdate();
		return rowCount;
	}

	public <E> Optional<E> getSafeSingleResult(CriteriaQuery<E> criteria) {
		List<E> e = em.createQuery(criteria).getResultList();
		if (e.isEmpty())
			return Optional.empty();
		return Optional.ofNullable(e.get(0));
	}


	@Inject
	JinqJPAStreamProvider streams;

	/**
	 * Create bean like this:
	 * 	@Bean
	 @Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
	 public JinqJPAStreamProvider streams() {
	 return new JinqJPAStreamProvider(entityManagerFactory().getMetamodel());
	 }
	  * @param clz
	 * @param <E>
	 * @return
	 */

	public <E> JPAJinqStream<E> streamAll(Class<E> clz) {
		return streams.streamAll(em, clz);
	}

	public <E> E getOnlyOne(Class<E> clz, JinqStream.Where<E,?> where) {
		return streams.streamAll(em, clz).where(where).getOnlyValue();
	}
	public <E> Optional<E> getOne(Class<E> clz, JinqStream.Where<E,?> where) {
		return streams.streamAll(em, clz).where(where).findOne();
	}



}