package com.kameo.challenger.utils.auth.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import net.oauth.jsontoken.JsonToken;
import net.oauth.jsontoken.JsonTokenParser;
import net.oauth.jsontoken.crypto.HmacSHA256Verifier;
import net.oauth.jsontoken.crypto.SignatureAlgorithm;
import net.oauth.jsontoken.crypto.Verifier;
import net.oauth.jsontoken.discovery.VerifierProvider;
import net.oauth.jsontoken.discovery.VerifierProviders;
import org.joda.time.DateTime;

import java.security.InvalidKeyException;
import java.security.SignatureException;
import java.util.List;

/**
 * Created by kmyczkowska on 2016-09-01.
 */
public class JWTVerifier<E extends TokenInfo> {
    final net.oauth.jsontoken.Checker checker;
    final VerifierProviders locators;
    final String issuer;
    final Class<E> clzE;
    final Gson gson= new Gson();

    public JWTVerifier(JWTServiceConfig sc, Class e) {
        this.issuer = sc.getIssuer();
        this.clzE = e;


        try {
            Verifier hmacVerifier = new HmacSHA256Verifier(sc.getSigningKey());
            VerifierProvider hmacLocator = new VerifierProvider() {
                @Override
                public List<Verifier> findVerifier(String id, String key) {
                    return Lists.newArrayList(hmacVerifier);
                }
            };
            locators = new VerifierProviders();
            locators.setVerifierProvider(SignatureAlgorithm.HS256, hmacLocator);
            checker = new net.oauth.jsontoken.Checker() {
                @Override
                public void check(JsonObject payload) throws SignatureException {
                    // don't throw - allow anything
                }

            };
        } catch (InvalidKeyException ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Verifies a json web token's validity and extracts the user id and other information from it.
     *
     * @param token
     * @return
     * @throws SignatureException
     * @throws InvalidKeyException
     */
    public TokenInfo verifyToken(String token) {

        JsonTokenParser parser = new JsonTokenParser(locators,
                checker);
        JsonToken jt;
        try {
            jt = parser.verifyAndDeserialize(token);
        } catch (SignatureException e) {
            throw new RuntimeException(e);
        }
        JsonObject payload = jt.getPayloadAsJsonObject();
        String issuer = payload.getAsJsonPrimitive("iss").getAsString();
        JsonObject info = payload.getAsJsonObject("info");

        if (this.issuer.equals(issuer)) {
            JsonObject request = new JsonObject();
            E e = gson.fromJson(info, clzE);
            e.setIssued(new DateTime(payload.getAsJsonPrimitive("iat").getAsLong() * 1000));
            e.setExpires(new DateTime(payload.getAsJsonPrimitive("exp").getAsLong() * 1000));
            return e;
        } else {
            return null;
        }
    }

}
