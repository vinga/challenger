package com.kameo.challenger.domain.accounts.db;

import java.io.Serializable;

public enum UserStatus implements Serializable {
    WAITING_FOR_EMAIL_CONFIRMATION,
    ACTIVE,
    SUSPENDED
}
