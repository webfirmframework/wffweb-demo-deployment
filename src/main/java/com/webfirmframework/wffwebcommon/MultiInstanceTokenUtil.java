package com.webfirmframework.wffwebcommon;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;
import com.webfirmframework.wffweb.server.page.LocalStorage;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

public enum MultiInstanceTokenUtil {

    SESSION("wffweb", "dfskfjsdoidksfksdfkjjerdfi#%^@&*)@$*+-h'sdwew]s"),

    AUTHORIZATION("wffweb", "dfuewpoqwd-0i123';xc-00-23874023497823jnzX<mDF38=4s");

    private final String issuer;

    private final Algorithm algorithmHS;

    private final JWTVerifier verifier;

    MultiInstanceTokenUtil(final String issuer, final String secret) {
        this.issuer = issuer;
        algorithmHS = Algorithm.HMAC256(secret.getBytes(StandardCharsets.UTF_8));
        verifier = JWT.require(algorithmHS).withIssuer(issuer).build();
    }

    public boolean isValidJWT(final LocalStorage.Item token, final String sessionId) {
        if (token != null && sessionId != null) {
            try {
                final DecodedJWT jwt = verifier.verify(token.value());
                return sessionId.equals(jwt.getClaim("sid").asString());
            } catch (JWTVerificationException e) {
                //Invalid signature/claims
            }
        }
        return false;
    }

    public JSONObject getPayloadFromJWT(LocalStorage.Item token) {
        return getPayloadFromJWT(token.value());
    }

    public String getSessionIdClaimFromJWT(String token) {
        if (token != null) {
            try {
                final DecodedJWT jwt = verifier.verify(token);
                final Claim sid = jwt.getClaim("sid");
                if (sid != null) {
                    return sid.asString();
                }
            } catch (JWTVerificationException e) {
                //Invalid signature/claims
            }
        }
        return null;
    }

    public JSONObject getPayloadFromJWT(String token) {
        if (token != null) {
            try {
                DecodedJWT jwt = verifier.verify(token);
                String decodedPayload = new String(Base64.getUrlDecoder().decode(jwt.getPayload()), StandardCharsets.UTF_8);
                return new JSONObject(decodedPayload);
            } catch (JWTVerificationException e) {
                //Invalid signature/claims
            }
        }
        return null;
    }

    public String createJWT(Map<String, Object> payload, String sessionId) {
        return JWT.create()
                .withPayload(payload)
                .withIssuer(issuer)
                .withClaim("sid", sessionId)
                .sign(algorithmHS);
    }

}
