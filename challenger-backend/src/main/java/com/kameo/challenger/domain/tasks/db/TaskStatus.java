package com.kameo.challenger.domain.tasks.db;

import java.io.Serializable;

public enum TaskStatus implements Serializable {
    waiting_for_acceptance,
    accepted,
    rejected
}

