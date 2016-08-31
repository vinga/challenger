package com.kameo.challenger.utils.odb;

import javax.persistence.criteria.Join;

public interface IJoinDeliverable<E,F> {
	Join<E, F> deliverJoin();
}
