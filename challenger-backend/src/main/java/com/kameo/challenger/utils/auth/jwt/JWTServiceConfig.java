package com.kameo.challenger.utils.auth.jwt;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class JWTServiceConfig<E extends TokenInfo> {
    byte[] signingKey;
    String issuer;
    String audience;
    Class<E> tokenInfoClass;
}
