package challenger.utils.odb;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaUpdate;
import javax.persistence.criteria.Root;

public interface IUpdateRestrictions<E> {
	/**
	 *	cq.where and cq.set must be set 
	 * @param _
	 * @param cq
	 * @param root
	 * @return modified criteria update
	 */
	CriteriaUpdate<?> applySetAndWhere(CriteriaBuilder cb, CriteriaUpdate<?> cq, Root<E> root);

}
