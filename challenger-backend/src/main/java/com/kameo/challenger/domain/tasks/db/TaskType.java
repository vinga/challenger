package com.kameo.challenger.domain.tasks.db;

import java.io.Serializable;

/**
 * Created by kmyczkowska on 2016-08-31.
 */
public enum TaskType implements Serializable {
    onetime,
    daily,
    weekly,
    monthly
}
