package com.kameo.challenger.config;

public class ServerConfig {
    private static String confirmEmailInvitationPattern = "http://blablah/uapi/confirm/{hash}";
    private static boolean crossDomain = true;

    public static String getConfirmEmailInvitationPattern(String uid) {
        return confirmEmailInvitationPattern.replace("{hash}", uid);
    }

    public static boolean isCrossDomain() {
        return crossDomain;
    }

    public static void setCrossDomain(boolean crossDomain) {
        ServerConfig.crossDomain = crossDomain;
    }
}
