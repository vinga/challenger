package com.kameo.challenger.domain.accounts.db;

import java.io.Serializable;

/**
 * Created by kmyczkowska on 2016-09-02.
 */
public enum UserStatus implements Serializable {
    WAITING_FOR_EMAIL_CONFIRMATION,
    ACTIVE,
    SUSPENDED
}