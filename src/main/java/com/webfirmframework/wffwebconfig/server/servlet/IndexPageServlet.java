package com.webfirmframework.wffwebconfig.server.servlet;

import com.webfirmframework.wffweb.server.page.BrowserPageContext;
import com.webfirmframework.wffweb.server.page.BrowserPageSession;
import com.webfirmframework.wffweb.util.URIUtil;
import com.webfirmframework.wffwebcommon.CookieUtil;
import com.webfirmframework.wffwebcommon.MultiInstanceTokenUtil;
import com.webfirmframework.wffwebconfig.page.IndexPage;
import com.webfirmframework.wffwebconfig.server.constants.ServerConstants;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

/**
 * Servlet implementation class HomePageServlet
 */
@WebServlet(urlPatterns = {"/ui/*"})
public class IndexPageServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private static final Logger LOGGER = Logger
            .getLogger(IndexPageServlet.class.getName());

    /**
     * @see HttpServlet#HttpServlet()
     */
    public IndexPageServlet() {
        super();
    }

    @Override
    public void init() throws ServletException {
        super.init();
        // optional
//        TagRegistry.loadAllTagClasses();
//        AttributeRegistry.loadAllAttributeClasses();
//        LOGGER.info("Loaded all wffweb classes");
        ServerConstants.CONTEXT_PATH = getServletContext().getContextPath();
    }

    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
     * response)
     */
    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response) throws ServletException, IOException {

        if (request.getRequestURI().endsWith("/ui")) {
            response.sendRedirect(request.getRequestURI() + "/");
            return;
        } else if (request.getRequestURI().equals(request.getContextPath() + "/")) {
            response.sendRedirect(request.getContextPath() + "/ui/");
            return;
        }

        response.setContentType("text/html;charset=utf-8");
        //NB: it is required to work "Reopen Closed Tab"
        response.setHeader("Cache-Control", "no-store");

        final String contextPath = request.getServletContext().getContextPath();

        String httpSessionId;
        HttpSession session = null;
        String loginId;
        if (ServerConstants.MULTI_NODE_MODE) {
            final MultiInstanceTokenUtil.ParsedPayloadAndClaims httpSessionDetails = CookieUtil.getHttpSessionDetails(request);
            httpSessionId = httpSessionDetails != null ? httpSessionDetails.sessionId() : null;
            loginId = httpSessionDetails != null ? httpSessionDetails.loginId() : null;
            if (httpSessionId == null) {
                httpSessionId = UUID.randomUUID().toString();
                final Cookie cookie = CookieUtil.createCookie(httpSessionId);
                cookie.setPath(contextPath + "/ui");
                cookie.setMaxAge(-1);
                cookie.setHttpOnly(true);
                response.addCookie(cookie);
            } else {
                final String loginToken = request.getParameter("loginToken");
                if (loginToken != null) {
                    final Map<String, String[]> queryParamsMap = new HashMap<>(request.getParameterMap());
                    queryParamsMap.remove("loginToken");
                    try {
                        loginId = CookieUtil.parseLoginIdFromLoginToken(httpSessionId, loginToken);
                    } catch (CookieUtil.InvalidLoginIdException e) {
                        response.sendRedirect(request.getRequestURI().concat(buildQueryString(queryParamsMap)));
                        return;
                    }
                    final Cookie cookie = CookieUtil.createCookie(httpSessionId, loginId);
                    cookie.setPath(contextPath + "/ui");
                    // expire only if not logged in
                    if (loginId == null) {
                        cookie.setMaxAge(-1);
                    } else {
                        cookie.setMaxAge(60 * 60 * 24 * 365);
                    }
                    cookie.setHttpOnly(true);
                    response.addCookie(cookie);
                    response.sendRedirect(request.getRequestURI().concat(buildQueryString(queryParamsMap)));
                    return;
                }
            }
        } else {
            session = request.getSession();
            httpSessionId = session.getId();
            session.setMaxInactiveInterval(ServerConstants.SESSION_TIMEOUT_SECONDS);
        }

        final BrowserPageSession bpSession = BrowserPageContext.INSTANCE.getSession(httpSessionId, true);
        if (loginId != null) {
            bpSession.userProperties().put("loginId", loginId);
        } else {
            bpSession.userProperties().remove("loginId");
        }
        if (session != null) {
            bpSession.setWeakProperty("httpSession", session);
        }

        final IndexPage indexPage = new IndexPage(contextPath, bpSession, contextPath + request.getRequestURI());

        BrowserPageContext.INSTANCE.addBrowserPage(httpSessionId,
                indexPage);

        try (OutputStream os = response.getOutputStream()) {
            indexPage.toOutputStream(os, "UTF-8");
            os.flush();
        }

    }

    private static String buildQueryString(final Map<String, String[]> parameters) {
        final String queryString = URIUtil.buildQueryStringFromNameToValues(parameters);
        if (!queryString.isBlank()) {
            return "?".concat(queryString);
        }
        return "";
    }

}
