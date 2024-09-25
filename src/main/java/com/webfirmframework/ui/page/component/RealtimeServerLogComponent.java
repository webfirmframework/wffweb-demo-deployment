package com.webfirmframework.ui.page.component;

import com.webfirmframework.ui.page.common.GlobalSTC;
import com.webfirmframework.ui.page.model.DocumentModel;
import com.webfirmframework.wffweb.server.page.BrowserPageContext;
import com.webfirmframework.wffweb.tag.html.AbstractHtml;
import com.webfirmframework.wffweb.tag.html.H1;
import com.webfirmframework.wffweb.tag.html.html5.attribute.global.Hidden;
import com.webfirmframework.wffweb.tag.html.stylesandsemantics.Div;
import com.webfirmframework.wffweb.tag.html.stylesandsemantics.Span;
import com.webfirmframework.wffweb.tag.htmlwff.TagContent;
import com.webfirmframework.wffwebconfig.AppSettings;

import java.time.Clock;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class RealtimeServerLogComponent extends Div {

    private volatile Boolean existedInBrowserPage;

    public RealtimeServerLogComponent(DocumentModel documentModel) {
        super(null);
        GlobalSTC.LOGGER_STC.setContent(
                ZonedDateTime.now(Clock.systemUTC()).format(DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss")) +
                        ":~$ created new RealtimeServerLogComponent");
        develop(documentModel);
    }

    private void develop(DocumentModel documentModel) {

        new H1(this).give(TagContent::text, "Server Log (50 only)");
        Div logDiv = new Div(this);

        //hidden span just to listen to logs
        Span hiddenSpan = new Span(this, new Hidden());
        hiddenSpan.subscribeTo(GlobalSTC.LOGGER_STC, content -> {

            AbstractHtml log = new Div(null).give(TagContent::text, content.content());
            logDiv.appendChild(log);

            //to show only last 50 logs
            if (logDiv.getChildrenSize() > 50) {
                logDiv.removeChild(logDiv.getFirstChild());
            }

            //we don't need to write anything to this span so returning null
            return null;
        });

        // to reduce GC overhead, manually removing LOGGER_STC from hiddenSpan if RealtimeServerLogComponent is not
        // available in the UI
        GlobalSTC.LOGGER_STC.addContentChangeListener(hiddenSpan, changeEvent -> () -> {
            //this is a Runnable scope, returning Runnable works well only in wffweb-12.0.0 or later
            final Boolean existed = this.existedInBrowserPage;

            if (existed != null && existed) {
                AppSettings.CACHED_THREAD_POOL.execute(() -> {
                    if (!BrowserPageContext.INSTANCE.existsAndValid(documentModel.browserPage())) {
                        this.existedInBrowserPage = false;
                        hiddenSpan.removeSharedTagContent(false);
                    }
                });
            }
        });
        RealtimeServerLogComponent.this.addParentGainedListener(event -> RealtimeServerLogComponent.this.existedInBrowserPage = true);
        RealtimeServerLogComponent.this.addParentLostListener(event -> {
            RealtimeServerLogComponent.this.existedInBrowserPage = false;
            AppSettings.CACHED_THREAD_POOL.execute(() -> hiddenSpan.removeSharedTagContent(false));
        });

    }
}
