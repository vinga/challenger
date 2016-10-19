package com.kameo.challenger.domain.accounts.db;

import java.io.Serializable;

/**
 * Created by kmyczkowska on 2016-09-02.
 */
public enum ConfirmationLinkType implements Serializable {
    EMAIL_CONFIRMATION,
    CHALLENGE_CONTRACT_CONFIRMATION,
    PASSWORD_RESET
}
