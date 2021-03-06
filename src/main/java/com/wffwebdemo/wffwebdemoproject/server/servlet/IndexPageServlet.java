package com.wffwebdemo.wffwebdemoproject.server.servlet;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.webfirmframework.wffweb.server.page.BrowserPage;
import com.webfirmframework.wffweb.server.page.BrowserPageContext;
import com.wffwebdemo.wffwebdemoproject.page.IndexPage;
import com.webfirmframework.wffweb.tag.html.attribute.core.AttributeRegistry;
import com.webfirmframework.wffweb.tag.html.core.TagRegistry;

/**
 * Servlet implementation class HomePageServlet
 */
// @WebServlet(urlPatterns = {"/index"})
public class IndexPageServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private static Logger LOGGER = Logger
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
    	
    	TagRegistry.getTagClassNameByTagName();
    	AttributeRegistry.getAttributeClassNameByAttributeName();
    	LOGGER.info("Loaded all wffweb classes");
    }

    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
     *      response)
     */
    protected void doGet(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {

        response.setContentType("text/html;charset=utf-8");

        String localeFromHeader = request.getHeader("X-Wff-Locale");

        Locale locale = request.getLocale();

        if (localeFromHeader != null) {
            try {
                String[] langCountry = localeFromHeader.split("_");
                if (langCountry.length == 2) {
                    locale = new Locale(langCountry[0], langCountry[1]);
                } else if (langCountry.length == 1) {
                    locale = new Locale(langCountry[0]);
                } else {
                    throw new RuntimeException("langCountry.length is not 2 and 1");
                }
                LOGGER.info("X-Wff-Locale " + localeFromHeader);
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE,
                        "Exception while converting X-Wff-Locale", e);
            }
        }

        try (OutputStream os = response.getOutputStream();) {

            HttpSession session = request.getSession();
            

            BrowserPage  browserPage = new IndexPage(session, locale);
            BrowserPageContext.INSTANCE.addBrowserPage(session.getId(),
                    browserPage);
            

            browserPage.toOutputStream(os, "UTF-8");
        }

    }

}
