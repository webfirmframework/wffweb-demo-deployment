package com.webfirmframework.ui.page.layout;

import com.webfirmframework.ui.page.common.NavigationURI;
import com.webfirmframework.ui.page.component.LoginComponent;
import com.webfirmframework.ui.page.component.RealtimeServerLogComponent;
import com.webfirmframework.ui.page.component.UserAccountComponent;
import com.webfirmframework.ui.page.model.DocumentModel;
import com.webfirmframework.wffweb.server.page.BrowserPage;
import com.webfirmframework.wffweb.server.page.BrowserPageSession;
import com.webfirmframework.wffweb.tag.html.*;
import com.webfirmframework.wffweb.tag.html.attribute.*;
import com.webfirmframework.wffweb.tag.html.attribute.global.ClassAttribute;
import com.webfirmframework.wffweb.tag.html.attribute.global.Id;
import com.webfirmframework.wffweb.tag.html.attributewff.CustomAttribute;
import com.webfirmframework.wffweb.tag.html.html5.attribute.Content;
import com.webfirmframework.wffweb.tag.html.html5.attribute.global.DataAttribute;
import com.webfirmframework.wffweb.tag.html.html5.attribute.global.Hidden;
import com.webfirmframework.wffweb.tag.html.links.Link;
import com.webfirmframework.wffweb.tag.html.metainfo.Head;
import com.webfirmframework.wffweb.tag.html.metainfo.Meta;
import com.webfirmframework.wffweb.tag.html.programming.Script;
import com.webfirmframework.wffweb.tag.html.stylesandsemantics.Div;
import com.webfirmframework.wffweb.tag.html.stylesandsemantics.Span;
import com.webfirmframework.wffweb.tag.htmlwff.NoTag;
import com.webfirmframework.wffweb.tag.htmlwff.TagContent;
import com.webfirmframework.wffwebcommon.MultiInstanceTokenUtil;

import java.util.logging.Logger;

public class IndexPageLayout extends Html {

    private static final Logger LOGGER = Logger
            .getLogger(IndexPageLayout.class.getName());

    private final DocumentModel documentModel;

    private final String contextPath;

    private Div mainDiv;

    public IndexPageLayout(BrowserPage browserPage, BrowserPageSession session, String contextPath) {
        super(null, new DataAttribute("bs-theme", "dark"));
        super.setPrependDocType(true);
        this.documentModel = new DocumentModel(session, browserPage, contextPath);
        super.setSharedData(documentModel);
        this.contextPath = contextPath;
        develop();
    }

    // @formatter:off
    private void develop() {


        new Head(this).give(head -> {
            new TitleTag(head).give(TagContent::text, "wffweb with bootstrap 5 css example");
            new Meta(head,
                    new Charset("utf-8"));
            new Meta(head,
                    new Name("viewport"),
                    new Content("width=device-width, initial-scale=1"));

            new Link(head,
                    new Href("https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css"),
                    new Rel(Rel.STYLESHEET),
                    new CustomAttribute("integrity", "sha384-9ndCyUaIbzAi2FUVXJi0CjmCapSmO7SnpJef0486qhLnuZ2cdeRhO02iuK6FUUVM"),
                    new CustomAttribute("crossorigin", "anonymous"));

            new Script(head,
                    new Src("https://code.jquery.com/jquery-3.6.0.min.js"),
                    new CustomAttribute("integrity", "sha256-/xUj+3OJU5yExlq6GSYGSHk7tPXikynS7ogEvDej/m4="),
                    new CustomAttribute("crossorigin", "anonymous"));

            new Script(head,
                    new Defer(),
                    new Src("https://www.gstatic.com/charts/loader.js"));

            new Link(head,
                    new Rel(Rel.STYLESHEET),
                    new Href(contextPath + "/assets/css/app.css?v=1"));

            new Script(head,
                    new Defer(),
                    new Src(contextPath + "/assets/js/app.js?v=5"));

        });

        new Body(this).give(body -> {

            mainDiv = new Div(body, new Id("mainDivId")).give(div -> {
                new NoTag(div, "Loading...");
            });

            new Script(body,
                    new Src("https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"),
                    new CustomAttribute("integrity", "sha384-geWF76RCwLtnZ8qwWowPQNguL3RmwHVBC9FhGdlKrxdiJJigb/j/68SIy3Te4Bkz"),
                    new CustomAttribute("crossorigin", "anonymous"));

        });

    }

    public void buildMainDivTags() {

        documentModel.browserPage().addServerMethod("customServerMethod1", new CustomServerMethod());

        mainDiv.removeAllChildren();
        //common progress icon
        new Div(mainDiv, new Hidden(), new Id("loadingIcon"), new ClassAttribute("spinner-border text-primary"), new Role(Role.STATUS)).give(tag -> {
            new Span(tag, new ClassAttribute("visually-hidden")).give(TagContent::text, "Loading...");
        });

        //To remove serverMethod added by SampleFilesUploadComponent if the uri is not NavigationURI.SAMPLE_FILES_UPLOAD
        //but since wffweb-12.0.1 it can be achieved by adding ParentLostListener on the component
//        new NoTag(mainDiv).whenURI(uriEvent -> !NavigationURI.SAMPLE_FILES_UPLOAD.getUri(documentModel).equals(uriEvent.uriAfter()),
//                tagEvent -> documentModel.browserPage().removeServerMethod(SampleFilesUploadComponent.FILE_UPLOAD_SERVER_METHOD));

        URIStateSwitch componentDiv = new Div(mainDiv);

        componentDiv.whenURI(NavigationURI.LOGIN.getPredicate(documentModel, componentDiv),
                () -> {
                    documentModel.browserPage().getTagRepository().findTitleTag().give(
                            TagContent::text, "Login | wffweb demo");
                    return new AbstractHtml[]{new LoginComponent(documentModel)};
                });

        componentDiv.whenURI(NavigationURI.REALTIME_SERVER_LOG.getPredicate(documentModel, componentDiv),
                () -> {
                    documentModel.browserPage().getTagRepository().findTitleTag().give(
                            TagContent::text, "Server Log | User Account | wffweb demo");
                    return new AbstractHtml[]{new RealtimeServerLogComponent(documentModel)};
                });

        componentDiv.whenURI(NavigationURI.USER.getPredicate(documentModel, componentDiv),
                () -> new AbstractHtml[]{new UserAccountComponent(documentModel)},
                event -> {

                    //if already logged in then navigate to user account page otherwise navigate to login page
                    if (MultiInstanceTokenUtil.hasValidJWT(documentModel.session())) {
                        documentModel.browserPage().setURI(NavigationURI.USER.getUri(documentModel));
                    } else {
                        documentModel.browserPage().setURI(NavigationURI.LOGIN.getUri(documentModel));
                    }

                });
    }

    // @formatter:on

}