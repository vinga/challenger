package com.kameo.challenger.domain.tasks.db;

import java.io.Serializable;

public enum TaskType implements Serializable {
    onetime,
    daily,
    weekly,
    monthly
}
