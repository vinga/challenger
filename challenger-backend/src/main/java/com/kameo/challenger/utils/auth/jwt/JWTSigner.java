package com.kameo.challenger.utils.auth.jwt;

import com.google.gson.JsonObject;
import net.oauth.jsontoken.JsonToken;
import net.oauth.jsontoken.crypto.HmacSHA256Signer;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.InvalidKeyException;
import java.security.SignatureException;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;



public class JWTSigner {
    private final JWTServiceConfig sc;
    private final HmacSHA256Signer signer;


    public JWTSigner(JWTServiceConfig sc) {
        this.sc = sc;
        try {
            signer = new HmacSHA256Signer(sc.getIssuer(), null, sc.getSigningKey());
        } catch (InvalidKeyException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Creates a json web token which is a digitally signed token that contains a payload (e.g. userId to identify
     * the user). The signing key is secret. That ensures that the token is authentic and has not been modified.
     * Using a jwt eliminates the need to store authentication session information in a database.
     *
     * @return
     */
    public <E> String createJsonWebToken(E tokenData, Date expirationDate) {
        Calendar cal = Calendar.getInstance();

        //Configure JSON token
        JsonToken token = new net.oauth.jsontoken.JsonToken(signer);
        token.setAudience(sc.getAudience());
        token.setIssuedAt(new org.joda.time.Instant(cal.getTimeInMillis()));
        token.setExpiration(new org.joda.time.Instant(expirationDate));
        try {
            JsonObject request = new JsonObject();
            Map<String, Object> tokenDataFields = convertToMap(tokenData);
            for (Map.Entry<String, Object> e : tokenDataFields.entrySet()) {
                fillWithDataField(request, e);
            }

            JsonObject payload = token.getPayloadAsJsonObject();
            payload.add("info", request);

            return token.serializeAndSign();
        } catch (SignatureException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    private void fillWithDataField(JsonObject request, Map.Entry<String, Object> e) {
        if (e.getValue() == null)
            return;
        if (e.getValue() instanceof Number)
            request.addProperty(e.getKey(), (Number) e.getValue());
        else if (e.getValue() instanceof Boolean)
            request.addProperty(e.getKey(), (Boolean) e.getValue());
        else if (e.getValue() instanceof Character)
            request.addProperty(e.getKey(), (Character) e.getValue());
        else if (e.getValue() instanceof String)
            request.addProperty(e.getKey(), (String) e.getValue());

    }


    private Map<String, Object> convertToMap(Object entity) throws InvocationTargetException, IllegalAccessException {
        Method[] methods = sc.getTokenInfoClass().getMethods();
        Map<String, Object> map = new HashMap<>();
        for (Method m : methods) {
            if (m.getName().startsWith("get")) {
                Object value = m.invoke(entity);
                if (value != null) {
                    map.put(String.valueOf(m.getName().charAt(3)).toLowerCase()+m.getName().substring(4), value);
                }
            }
        }
        return map;
    }
}

