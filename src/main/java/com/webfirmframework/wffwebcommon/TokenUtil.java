package com.webfirmframework.wffwebcommon;

import com.webfirmframework.wffweb.server.page.LocalStorage;
import org.json.JSONObject;

import java.util.Map;

public final class TokenUtil {

    public static boolean isValidJWT(LocalStorage.Item token, String sessionId) {
        return MultiInstanceTokenUtil.AUTHORIZATION.isValidJWT(token, sessionId);
    }

    public static JSONObject getPayloadFromJWT(LocalStorage.Item token) {
        return getPayloadFromJWT(token.value());
    }

    public static JSONObject getPayloadFromJWT(String token) {
        return MultiInstanceTokenUtil.AUTHORIZATION.getPayloadFromJWT(token);
    }

    public static String createJWT(Map<String, Object> payload, String sessionId) {
        return MultiInstanceTokenUtil.AUTHORIZATION.createJWT(payload, sessionId);
    }

}
