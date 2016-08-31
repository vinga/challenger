package com.kameo.challenger.utils.odb;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import javax.persistence.metamodel.ListAttribute;
import javax.persistence.metamodel.SingularAttribute;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;
import java.util.Set;

public class Exprs {

	public static <E, F> List<F> getAllFieldsAsList(Collection<E> col, SingularAttribute<E, F> fieldAccessor) {
		List<F> res = Lists.newArrayList();
		for (E e : col) {

			F field = invokeGet(e, fieldAccessor);
			if (field != null)
				res.add(field);
		}
		return res;
	}

	public static <E, F extends Comparable> F getMin(Collection<E> col, SingularAttribute<E, F> fieldAccessor) {
		F min = null;
		for (E e : col) {

			F field = invokeGet(e, fieldAccessor);
			if (field != null) {
				if (min == null || min.compareTo(field) > 0)
					min = field;
			}
		}
		return min;
	}

	public static <E, F extends Comparable> F getMax(Collection<E> col, SingularAttribute<E, F> fieldAccessor) {
		F min = null;
		for (E e : col) {

			F field = invokeGet(e, fieldAccessor);
			if (field != null) {
				if (min == null || min.compareTo(field) < 0)
					min = field;
			}
		}
		return min;
	}
	public static <E, F> Set<F> getAllFieldsAsSet(Collection<E> col, SingularAttribute<E, F> fieldAccessor) {
		Set<F> res = Sets.newHashSet();
		for (E e : col) {

			F field = invokeGet(e, fieldAccessor);
			if (field != null)
				res.add(field);
		}
		return res;
	}

	public static <E, F> Set<F> getAllFieldsAsSet(Collection<E> col, ListAttribute<E, F> fieldAccessor) {
		Set<F> res = Sets.newHashSet();
		for (E e : col) {

			List<F> list = invokeGet(e, fieldAccessor);
			if (list != null)
				res.addAll(list);
		}
		return res;
	}

	public static <E, F> List<F> getAllFieldsAsList(Collection<E> col, ListAttribute<E, F> fieldAccessor) {
		List<F> res = Lists.newArrayList();
		for (E e : col) {

			List<F> list = invokeGet(e, fieldAccessor);
			if (list != null)
				res.addAll(list);
		}
		return res;
	}
	public static <E, F> CheckObjectInCollection<E, F> object(F object) {
		CheckObjectInCollection<E, F> ee = new CheckObjectInCollection<E, F>();
		ee.object = object;
		return ee;
	}

	public static <E, F> CheckFieldInCollection<E, F> object(F object, SingularAttribute<F, E> fieldAccessor) {
		CheckFieldInCollection<E, F> ee = new CheckFieldInCollection<E, F>();
		ee.object = object;
		ee.sa = fieldAccessor;
		return ee;
	}

	public static class CheckFieldInCollection<E, F> {
		F object;
		SingularAttribute<F, E> sa;

		public <E> boolean isInCollection(Collection<E> ee) {

			E field = invokeGet(object, sa);

			for (E e : ee) {
				if (field != null && field.equals(e)) {
					return true;
				}
			}
			return false;
		}

		public <E> boolean isInCollectionAsField(Collection<F> ee, SingularAttribute<F, E> sa2) {

			E field = invokeGet(object, sa);

			for (F e : ee) {
				F field2 = invokeGet(e, sa2);

				if (field != null && field2 != null && field.equals(field2)) {
					return true;
				}
			}
			return false;
		}

		public Optional<F> getFromCollectionByField(Collection<F> ee, SingularAttribute<F, E> sa2) {

			E field = invokeGet(object, sa);

			for (F e : ee) {
				F field2 = invokeGet(e, sa2);

				if (field != null && field2 != null && field.equals(field2)) {
					return Optional.of(e);
				}
			}
			return Optional.absent();
		}

	}

	public static class CheckObjectInCollection<E, F> {
		F object;

		public <E> boolean isInCollectionAsField(Collection<E> ee, SingularAttribute<E, F> sa) {


			for (E e : ee) {

				F field = invokeGet(e, sa);

				if (field != null && field.equals(object)) {
					return true;
				}
			}
			return false;
		}

		public <G> List<G> getContainingItElementsAsList(List<G> gg, SingularAttribute<G, F> sa) {
			List<G> res = Lists.newArrayList();
			for (G g : gg) {

				F field = invokeGet(g, sa);

				if (field != null && field.equals(object)) {
					res.add(g);
				}
			}
			return res;
		}
	}

	private static <E, F> F invokeGet(E object, SingularAttribute<E, ?> sa) {
		Method gm = EntityHelper.getGetterMethod(sa);
		Object field;
		try {
			field = gm.invoke(object);
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
		return (F) field;
	}

	public static <E, F> F get(E object, SingularAttribute<E, F> sa) {
		Method gm = EntityHelper.getGetterMethod(sa);
		Object field;
		try {
			field = gm.invoke(object);
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
		return (F) field;
	}

	public static <E, F> F invokeGet(E object, ListAttribute<E, ?> sa) {
		Method gm = EntityHelper.getGetterMethod(sa);
		Object field;
		try {
			field = gm.invoke(object);
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
		return (F) field;
	}
}
