package com.webfirmframework.wffwebcommon;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;
import com.webfirmframework.wffweb.server.page.BrowserPageSession;
import com.webfirmframework.wffweb.server.page.LocalStorage;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;

public enum MultiInstanceTokenUtil {

    // TODO initialize your own random unique string
    SESSION("wffweb", "dfskfjsdoidksfksdfkjjerdfi#%^@&*)@$*+-h'sdwew]s"),

    // TODO initialize your own random unique string
    AUTHORIZATION("wffweb", "dfuewpoqwd-0i123';xc-00-23874023497823jnzX<mDF38=4s");

    private final String issuer;

    private final Algorithm algorithmHS;

    private final JWTVerifier verifier;

    public record ParsedPayloadAndClaims(String sessionId, String loginId) {
    }

    MultiInstanceTokenUtil(final String issuer, final String secret) {
        this.issuer = issuer;
        algorithmHS = Algorithm.HMAC512(secret.getBytes(StandardCharsets.UTF_8));
        verifier = JWT.require(algorithmHS).withIssuer(issuer).build();
    }

    private boolean isValidJWT(final LocalStorage.Item token, final String sessionId, final String loginId) {
        if (token != null && sessionId != null && loginId != null) {
            try {
                final DecodedJWT jwt = verifier.verify(token.value());
                final Claim sidClaim = jwt.getClaim("sid");
                final Claim loginIdClaim = jwt.getClaim("loginId");
                final String sidFromToken = sidClaim != null ? sidClaim.asString() : null;
                final String loginIdFromToken = loginIdClaim != null ? loginIdClaim.asString() : null;
                return sessionId.equals(sidFromToken) && loginId.equals(loginIdFromToken);
            } catch (JWTVerificationException e) {
                //Invalid signature/claims
            }
        }
        return false;
    }

    public static boolean hasValidJWT(final BrowserPageSession session) {
        final LocalStorage localStorage = session.localStorage();
        final LocalStorage.Item jwtToken = localStorage.getToken("jwtToken");
        final String loginId = (String) session.userProperties().get("loginId");
        final String sessionId = session.id();
        if (jwtToken == null || loginId == null) {
            return false;
        }
        final String loginDataKey = "%s:%s:%s".formatted(sessionId, loginId, jwtToken);
        if ((boolean) session.userProperties().getOrDefault(loginDataKey, false)) {
            return true;
        }
        final boolean validJWT = MultiInstanceTokenUtil.AUTHORIZATION.isValidJWT(jwtToken, sessionId, loginId);
        session.userProperties().put(loginDataKey, validJWT);
        return validJWT;
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

    public Map<String, Object> getPayloadFromJWT(LocalStorage.Item token) {
        return getPayloadFromJWT(token.value());
    }

    public Map<String, Object> getPayloadFromJWT(String token) {
        if (token != null) {
            try {
                DecodedJWT jwt = verifier.verify(token);
                String decodedPayload = new String(Base64.getUrlDecoder().decode(jwt.getPayload()), StandardCharsets.UTF_8);
                return AppUtilities.JSON_PARSER_UNMODIFIABLE.parseJsonObject(decodedPayload);
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

    public String createJWT(final Map<String, Object> payload, final String sessionId, final Instant expiresAt) {
        return JWT.create()
                .withPayload(payload)
                .withIssuer(issuer)
                .withClaim("sid", sessionId)
                .withExpiresAt(expiresAt)
                .sign(algorithmHS);
    }

    public String createJWT(final Map<String, Object> payload, final String sessionId, final String loginId) {
        return JWT.create()
                .withPayload(payload)
                .withIssuer(issuer)
                .withClaim("sid", sessionId)
                .withClaim("loginId", loginId)
                .sign(algorithmHS);
    }

    public ParsedPayloadAndClaims getParsedClaimsFromJWT(final String token) {
        if (token != null) {
            try {
                final DecodedJWT jwt = verifier.verify(token);
                final Claim sid = jwt.getClaim("sid");
                final Claim loginId = jwt.getClaim("loginId");
                return new ParsedPayloadAndClaims(sid != null ? sid.asString() : null,
                        loginId != null ? loginId.asString() : null);
            } catch (JWTVerificationException e) {
                //Invalid signature/claims
            }
        }
        return null;
    }

}
