package com.kameo.challenger.domain.challenges.db;

import java.io.Serializable;

public enum ChallengeStatus implements Serializable {
    ACTIVE,
    WAITING_FOR_ACCEPTANCE,
    REFUSED,
    REMOVED,
}
