package com.kameo.challenger.odb;

import java.io.Serializable;

/**
 * Created by kmyczkowska on 2016-09-01.
 */
public enum ChallengeStatus implements Serializable {
    INACIVE,
    ACTIVE,
    WAITING_FOR_ACCEPTANCE,
    REFUSED
}
