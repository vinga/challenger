package com.kameo.challenger.domain.accounts.db;

import java.io.Serializable;

public enum ConfirmationLinkType implements Serializable {
    EMAIL_CONFIRMATION,
    CHALLENGE_CONFIRMATION_ACCEPT,
    CHALLENGE_CONFIRMATION_REJECT,
    PASSWORD_RESET,
    OAUTH2_LOGIN
}
