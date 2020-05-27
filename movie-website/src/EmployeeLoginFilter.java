

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
 * Servlet Filter implementation class EmployeeLoginFilter
 */
@WebFilter(filterName="EmployeeLoginFilter", urlPatterns= {"/_dashboard", "/dashboard.html"})
public class EmployeeLoginFilter implements Filter {

	public void destroy() {
		// TODO Auto-generated method stub
	}

	
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		
		HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        
        System.out.println("EmployeeLoginFilter: " + httpRequest.getRequestURI());
        
        if (httpRequest.getSession().getAttribute("employee-user") == null) {
            httpResponse.sendRedirect("employee-login.html");
        }
        
        else if (httpRequest.getRequestURI().endsWith("_dashboard") && httpRequest.getSession().getAttribute("employee-user") != null) {
            httpResponse.sendRedirect("dashboard.html");
        }
        
        else {
        	chain.doFilter(request, response);
        }
	}

	public void init(FilterConfig fConfig) throws ServletException {
		// TODO Auto-generated method stub
	}

}
