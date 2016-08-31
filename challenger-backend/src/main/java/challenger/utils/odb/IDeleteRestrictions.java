package challenger.utils.odb;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaDelete;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

public interface IDeleteRestrictions<E> {
	Predicate apply(CriteriaBuilder cb, CriteriaDelete<?> cq, Root<E> root);
}
