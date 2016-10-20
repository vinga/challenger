package com.kameo.challenger.utils.odb;


import com.kameo.challenger.odb.api.IIdentity;

import java.util.Comparator;

public class ComparisionHelper {
	/**
	 * Beware adding not persisted entit to set or list and than invoking
	 * contains (or any other method involving hashCode or equals check)
	 * 
	 * @param a
	 * @param b
	 * @return
	 */

	public static boolean equals(IIdentity a, Object b) {
		if (a == b) {
			return true;
		}
		if (a == null || b == null)
			return false;
		boolean ok1 = b.getClass().isAssignableFrom(a.getClass());
		boolean ok2 = a.getClass().isAssignableFrom(b.getClass());
		if (ok1 || ok2) {
			IIdentity bi = (IIdentity) b;
			if (a.getId() <= 0)
				return false;
			return a.getId() == bi.getId();
		}
		return false;
	}

	public static boolean equalsNullSafe(Object a, Object b) {
		if (a == b) {
			return true;
		}
		if (a == null || b == null)
			return false;		
		return a.equals(b);
	}

	public static Integer compareNullSafe(Comparable a, Comparable b) {
		if (a == null)
			return 1;
		if (b == null)
			return -1;
		return a.compareTo(b);
	}

	public static int hashCode(Object a) {
		if (a instanceof IIdentity)
			return (int) ((IIdentity) a).getId();
		return a.hashCode();
	}

	public static Comparator<IIdentity> createComparator() {
		return new Comparator<IIdentity>() {


			public int compare(IIdentity arg0, IIdentity arg1) {
				if (arg0.getId() > arg1.getId())
					return 1;
				else if (arg0.getId() == arg1.getId())
					return 0;
				else
					return -1;
			}
		};
	}

	public static int compare(int a, int b) {
		if (a > b)
			return 1;
		else if (a < b)
			return -1;
		else
			return 0;
	}

	public static int compare(Integer a, Integer b) {
		if (a > b)
			return 1;
		else if (a < b)
			return -1;
		else
			return 0;
	}

	public static int compare(long a, long b) {
		if (a > b)
			return 1;
		else if (a < b)
			return -1;
		else
			return 0;
	}

	public static int multiCompare(Comparable a, Comparable b, Comparable c, Comparable d) {
		int i = a.compareTo(b);
		if (i == 0)
			return c.compareTo(d);
		return i;
	}
}
