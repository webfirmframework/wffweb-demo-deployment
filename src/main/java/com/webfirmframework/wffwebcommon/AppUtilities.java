package com.webfirmframework.wffwebcommon;

import com.webfirmframework.wffweb.json.JsonArrayType;
import com.webfirmframework.wffweb.json.JsonObjectType;
import com.webfirmframework.wffweb.json.JsonParser;

public class AppUtilities {

    public static final JsonParser JSON_PARSER_UNMODIFIABLE = JsonParser.newBuilder()
            .jsonObjectType(JsonObjectType.UNMODIFIABLE_MAP)
            .jsonArrayType(JsonArrayType.UNMODIFIABLE_LIST)
            .build();
}
