package cz.muni.fi.pv168.kartoteka;

import java.io.IOException;
import javax.inject.Inject;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet filter which controls if user is authenticated, if not, user is redirected
 * to welcome page. Secondly it forces browser not to cache previous pages so user cannot
 * go back after logout.
 * @author Marián Macik
 */
public class LoginFilter implements Filter {

    @Inject
    AuthenticationBean authenticationBean;

    /**
     * Checks if user is logged in. If not it redirects to the login.xhtml page.
     * @param chain FilterChain
     * @param request Servlet request
     * @param response Servlet response
     */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletResponse hsr = (HttpServletResponse) response;
        hsr.setHeader("Cache-Control", "no-cache, no-store, must-revalidate"); // HTTP 1.1.
        hsr.setHeader("Pragma", "no-cache"); // HTTP 1.0.
        hsr.setDateHeader("Expires", 0); // Proxies.

        // For the first application request there is no loginBean in the session so user needs to log in
        // For other requests loginBean is present but we need to check if user has logged in successfully
        if (authenticationBean == null || (authenticationBean.getProfile() == null)) {
            String contextPath = ((HttpServletRequest) request).getContextPath();
            ((HttpServletResponse) response).sendRedirect(contextPath + "/welcome.xhtml");
        }

        chain.doFilter(request, response);

    }

    @Override
    public void init(FilterConfig config) throws ServletException {
        // Nothing to do
    }

    @Override
    public void destroy() {
        // Nothing to do
    }

}
