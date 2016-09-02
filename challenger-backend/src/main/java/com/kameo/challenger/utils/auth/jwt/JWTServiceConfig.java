package com.kameo.challenger.utils.auth.jwt;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Created by kmyczkowska on 2016-09-01.
 */
@Data
@AllArgsConstructor
public class JWTServiceConfig<E extends TokenInfo> {
    byte[] signingKey;
    String issuer;
    String audience;
    Class<E> tokenInfoClass;
}
