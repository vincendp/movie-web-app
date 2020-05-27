
import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet Filter implementation class LoginFilter
 */
@WebFilter(filterName = "LoginFilter", urlPatterns = "/*")
public class LoginFilter implements Filter {

	/**
	 * @see Filter#destroy()
	 */
	public void destroy() {
		// TODO Auto-generated method stub
	}

	/**
	 * @see Filter#doFilter(ServletRequest, ServletResponse, FilterChain)
	 */
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		HttpServletRequest httpRequest = (HttpServletRequest) request;
		HttpServletResponse httpResponse = (HttpServletResponse) response;

		System.out.println("LoginFilter: " + httpRequest.getRequestURI());

		if (this.isUrlAllowedWithoutLogin(httpRequest.getRequestURI())) {
			// Keep default action: pass along the filter chain
			chain.doFilter(request, response);
			return;
		}

		// Redirect to login page if the "user" attribute doesn't exist in session
		if (httpRequest.getSession().getAttribute("user") == null) {
			httpResponse.sendRedirect("login.html");
		} else {
			// If the user exists in current session, redirects the user to the
			// corresponding URL
			chain.doFilter(request, response);
		}
	}

	private boolean isUrlAllowedWithoutLogin(String requestURI) {
		requestURI = requestURI.toLowerCase();

		return requestURI.endsWith("login.html") || requestURI.endsWith("login.js") || requestURI.endsWith("api/login")
				|| requestURI.endsWith(".css") || requestURI.endsWith("dashboard.html")
				|| requestURI.endsWith("dashboard.js") || requestURI.endsWith("employee-login.html")
				|| requestURI.endsWith("employee-login.js") || requestURI.endsWith("api/employee-login")
				|| requestURI.endsWith("api/metadata") || requestURI.endsWith("api/insert-new-stars-and-movies")
				|| requestURI.endsWith("_dashboard") || requestURI.endsWith("api/fts") || requestURI.endsWith("api/search");
	}

	/**
	 * @see Filter#init(FilterConfig)
	 */
	public void init(FilterConfig fConfig) throws ServletException {
		// TODO Auto-generated method stub
	}

}
