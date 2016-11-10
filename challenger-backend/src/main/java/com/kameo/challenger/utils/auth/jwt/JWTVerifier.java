package com.kameo.challenger.utils.auth.jwt;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.oauth.jsontoken.Clock;
import net.oauth.jsontoken.JsonToken;
import net.oauth.jsontoken.JsonTokenParser;
import net.oauth.jsontoken.SystemClock;
import net.oauth.jsontoken.crypto.AsciiStringVerifier;
import net.oauth.jsontoken.crypto.HmacSHA256Verifier;
import net.oauth.jsontoken.crypto.SignatureAlgorithm;
import net.oauth.jsontoken.crypto.Verifier;
import net.oauth.jsontoken.discovery.VerifierProvider;
import net.oauth.jsontoken.discovery.VerifierProviders;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.StringUtils;
import org.joda.time.DateTime;

import java.security.InvalidKeyException;
import java.security.SignatureException;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;


public class JWTVerifier<E extends TokenInfo> {

    final String issuer;
    final Class<E> clzE;

    final JWTServiceConfig sc;

    public JWTVerifier(JWTServiceConfig sc, Class<E> e) {
        this.issuer = sc.getIssuer();
        this.clzE = e;
        this.sc = sc;


    }

    public static String fromBase64ToJsonString(String source) {
        return StringUtils.newStringUtf8(Base64.decodeBase64(source));
    }

    public static String toDotFormat(String... parts) {
        return Joiner.on('.').useForNull("").join(parts);
    }

    void verify(String tokenString) throws InvalidKeyException {

        Verifier hmacVerifier = new HmacSHA256Verifier(sc.getSigningKey());
        VerifierProvider hmacLocator = (id, key) -> Lists.newArrayList(hmacVerifier);
        VerifierProviders locators = new VerifierProviders();
        locators.setVerifierProvider(SignatureAlgorithm.HS256, hmacLocator);
        net.oauth.jsontoken.Checker checker = payload -> {
            // don't throw - allow anything
        };
        Clock clock = new SystemClock();
        String[] pieces = tokenString.split(Pattern.quote("."));
        if (pieces.length != 3) {
            System.out.println("badtokenString "+tokenString);
            throw new IllegalArgumentException("Expected JWT to have 3 segments separated by \'.\', but it has " + pieces.length + " segments");
        } else {
            String jwtHeaderSegment = pieces[0];
            String jwtPayloadSegment = pieces[1];
            byte[] signature = Base64.decodeBase64(pieces[2]);
            JsonParser parser = new JsonParser();
            JsonObject header = parser.parse(fromBase64ToJsonString(jwtHeaderSegment))
                                      .getAsJsonObject();
            JsonObject payload = parser.parse(fromBase64ToJsonString(jwtPayloadSegment))
                                       .getAsJsonObject();
            JsonElement algorithmName = header.get("alg");
            if (algorithmName == null) {
                throw new IllegalArgumentException("JWT header is missing the required \'alg\' parameter");
            } else {
                SignatureAlgorithm sigAlg = SignatureAlgorithm.getFromJsonName(algorithmName.getAsString());
                JsonElement keyIdJson = header.get("kid");
                String keyId = keyIdJson == null ? null : keyIdJson.getAsString();
                String baseString = toDotFormat(new String[]{jwtHeaderSegment, jwtPayloadSegment});
                JsonToken jsonToken = new JsonToken(payload, clock);
                List verifiers = locators.getVerifierProvider(sigAlg)
                                         .findVerifier(jsonToken.getIssuer(), keyId);
                if (verifiers == null) {
                    throw new IllegalArgumentException("No valid verifier for issuer: " + jsonToken.getIssuer());
                } else {
                    boolean sigVerified = false;
                    Iterator now = verifiers.iterator();

                    while (now.hasNext()) {
                        Verifier expiration = (Verifier) now.next();
                        AsciiStringVerifier issuedAt = new AsciiStringVerifier(expiration);

                        try {
                            issuedAt.verifySignature(baseString, signature);
                            sigVerified = true;
                            break;
                        } catch (SignatureException var24) {
                            var24.printStackTrace();
                        }
                    }

                    if (!sigVerified) {
                        throw new IllegalArgumentException("Signature verification failed for issuer: " + jsonToken
                                .getIssuer() + " " + verifiers.size());
                    }
                }
            }
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

    public E verifyToken(String token) {
        try {
            Verifier hmacVerifier = new HmacSHA256Verifier(sc.getSigningKey());
            VerifierProvider hmacLocator = (id, key) -> Lists.newArrayList(hmacVerifier);
            VerifierProviders locators = new VerifierProviders();
            locators.setVerifierProvider(SignatureAlgorithm.HS256, hmacLocator);
            net.oauth.jsontoken.Checker checker = payload -> {
                // don't throw - allow anything
            };
            final Gson gson = new Gson();
            JsonTokenParser parser = new JsonTokenParser(locators,
                    checker);
            JsonToken jt;
            try {
                // System.out.println("TOKEN TO VERIFY " + token + " " + new Date());

                verify(token);

                System.out.println("------------------------");
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

                //System.out.println("issued " + e.getIssued() + ", expires " + e.getExpires());
                return e;
            } else {
                return null;
            }
        } catch (InvalidKeyException ex) {
            throw new RuntimeException(ex);
        }
    }

}
