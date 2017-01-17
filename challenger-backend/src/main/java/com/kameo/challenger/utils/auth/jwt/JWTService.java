package com.kameo.challenger.utils.auth.jwt;


public class JWTService<E extends TokenInfo> {
    private JWTSigner signer;
    private JWTVerifier<E> verifier;

    public JWTService(JWTServiceConfig sc) {
        this.signer = new JWTSigner(sc);
        this.verifier = new JWTVerifier<>(sc, sc.getTokenInfoClass());
    }

    public E verifyToken(String token) throws AuthException {
        E tokenInfo = verifier.verifyToken(token);
        return tokenInfo;
    }

    public String tokenToString(E tokenInfo) {
        return signer
                .createJsonWebToken(tokenInfo, tokenInfo.getExpires().toDate());
    }

    public static class AuthException extends Exception {
        public AuthException(String message) {
            super(message);
        }
    }
}
