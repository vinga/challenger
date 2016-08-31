package challenger.utils.odb;

import java.io.Serializable;

public class Holder<E> implements Serializable, IHolderImmutable<E> {

	private static final long serialVersionUID = 1L;
	E holded;

	public Holder(E e) {
		this.holded=e;
	}

	public void setHolded(E holded) {
		this.holded = holded;
	}
	@Override
	public E getHold() {
		return holded;
	}

	public static <E> Holder<E> absent() {
		return new Holder<E>(null);
	}

	public static <E> Holder<E> of(E e) {
		if (e == null)
			throw new IllegalArgumentException("Cannot be null");
		return new Holder<E>(e);
	}
}
