package com.webfirmframework.wffwebcommon;

import com.webfirmframework.wffwebconfig.server.constants.ServerConstants;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.json.JSONObject;

import java.time.Clock;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;

public final class CookieUtil {

    /**
     * Note: this name should be unique.
     */
    private final static String LOGIN_ID_PARAM_NAME = "cookieLid";

    public static class InvalidLoginIdException extends Exception {
        public InvalidLoginIdException(final String s) {
            super(s);
        }
    }

    private CookieUtil() {
    }

    public static String parseLoginIdFromLoginToken(final String httpSessionId, final String loginToken) throws InvalidLoginIdException {
        if (httpSessionId != null) {
            if (loginToken.isBlank()) {
                return null;
            } else {
                final JSONObject payloadFromJWT = MultiInstanceTokenUtil.SESSION.getPayloadFromJWT(loginToken);
                if (payloadFromJWT == null) {
                    //if token is expired payload will be null so it should return null to logout.
                    return null;
                }
                final String loginId = payloadFromJWT.getString(CookieUtil.LOGIN_ID_PARAM_NAME);
                final String sessionId = payloadFromJWT.getString("sid");
                if (httpSessionId.equals(sessionId)) {
                    return loginId;
                }
                throw new InvalidLoginIdException("Invalid loginId");
            }
        }
        throw new IllegalArgumentException("Invalid session");
    }

    public static String createLoginToken(final String sessionId, final String loginId) {
        //the token should be valid for only 5 minutes as it is just a temporary token for login
        final int secondsForFiveMinutes = 60 * 5;
        return MultiInstanceTokenUtil.SESSION.createJWT(Map.of(CookieUtil.LOGIN_ID_PARAM_NAME, loginId),
                sessionId, Clock.systemUTC().instant().plusSeconds(secondsForFiveMinutes));
    }

    public static Cookie createCookie(final String sessionId) {
        return new Cookie(ServerConstants.WFFWEB_TOKEN_COOKIE, MultiInstanceTokenUtil.SESSION.createJWT(Map.of(), sessionId));
    }

    public static Cookie createCookie(final String sessionId, final String loginId) {
        if (loginId == null) {
            return createCookie(sessionId);
        }
        return new Cookie(ServerConstants.WFFWEB_TOKEN_COOKIE, MultiInstanceTokenUtil.SESSION.createJWT(Map.of(), sessionId, loginId));
    }

    public static MultiInstanceTokenUtil.ParsedPayloadAndClaims getHttpSessionDetails(final HttpServletRequest request) {
        final Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            return Arrays.stream(cookies).filter(cookie -> (ServerConstants.WFFWEB_TOKEN_COOKIE.equals(cookie.getName()) && cookie.getValue() != null))
                    .map(cookie -> MultiInstanceTokenUtil.SESSION.getParsedClaimsFromJWT(cookie.getValue())).filter(Objects::nonNull).findAny().orElse(null);
        }
        return null;
    }
}
