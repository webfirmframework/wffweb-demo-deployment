package com.webfirmframework.ui.page.component;

import com.webfirmframework.ui.page.common.GlobalSTC;
import com.webfirmframework.ui.page.model.DocumentModel;
import com.webfirmframework.wffweb.tag.html.H1;
import com.webfirmframework.wffweb.tag.html.H2;
import com.webfirmframework.wffweb.tag.html.H6;
import com.webfirmframework.wffweb.tag.html.SharedTagContent;
import com.webfirmframework.wffweb.tag.html.stylesandsemantics.Div;
import com.webfirmframework.wffweb.tag.htmlwff.TagContent;
import com.webfirmframework.wffweb.util.StringUtil;
import com.webfirmframework.wffweb.util.URIUtil;

import java.time.Clock;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

public class ViewItem extends Div {

    private final DocumentModel documentModel;

    public ViewItem(DocumentModel documentModel) {
        super(null);
        this.documentModel = documentModel;
        GlobalSTC.LOGGER_STC.setContent(
                ZonedDateTime.now(Clock.systemUTC()).format(DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss")) +
                        ":~$ created new ViewItem");
        develop();
    }

    private void develop() {
        new H1(this).give(TagContent::text, "View Item");

        Integer itemId = null;
        String uri = documentModel.browserPage().getURI();
        System.out.println("uri = " + uri);
        Map<String, List<String>> queryParameters = URIUtil.parseQueryParameters(uri);
        List<String> values = queryParameters.get("itemId");
        if (values.size() > 0) {
            itemId = Integer.parseInt(values.get(0));
        }

        if (itemId != null) {
            new H6(this).give(TagContent::text, "Item " + itemId);
        } else {
            new H6(this).give(TagContent::text, "Invalid item id");
        }

    }
}
