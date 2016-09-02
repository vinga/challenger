package com.kameo.challenger.config;

import lombok.Data;
import org.springframework.stereotype.Component;

/**
 * Created by kmyczkowska on 2016-09-01.
 */


public class ServerConfig {
    private static String confirmEmailInvitationPattern="http://blablah/uapi/confirm/{hash}";

    public static String getConfirmEmailInvitationPattern(String uid) {
        return confirmEmailInvitationPattern.replace("{hash}", uid);
    }

}
