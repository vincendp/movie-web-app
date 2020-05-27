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
 * Servlet Filter implementation class ConfirmationFilter
 */
@WebFilter(filterName="ConfirmationFilter", urlPatterns="/confirmation.html")
public class ConfirmationFilter implements Filter {

	/**
	 * @see Filter#destroy()
	 */
	public void destroy() {
		// TODO Auto-generated method stub
	}

	/**
	 * @see Filter#doFilter(ServletRequest, ServletResponse, FilterChain)
	 */
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        
        System.out.println("ConfirmationFilter: " + httpRequest.getRequestURI());
        
        if (httpRequest.getSession().getAttribute("user") != null && httpRequest.getSession().getAttribute("confirmation") == null ) {
        	httpResponse.sendRedirect("index.html");
        }
        else if (httpRequest.getSession().getAttribute("confirmation") == null) {
            httpResponse.sendRedirect("login.html");
        } else {
        	httpRequest.getSession().removeAttribute("confirmation");
            chain.doFilter(request, response);
        }
	}

	/**
	 * @see Filter#init(FilterConfig)
	 */
	public void init(FilterConfig fConfig) throws ServletException {
		// TODO Auto-generated method stub
	}

}
