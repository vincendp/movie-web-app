
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javax.annotation.Resource;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

import org.jasypt.util.password.StrongPasswordEncryptor;

import com.google.gson.JsonObject;

/**
 * Servlet implementation class LoginServlet
 */
@WebServlet(name = "LoginServlet", urlPatterns = "/api/login")
public class LoginServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	@Resource(name = "jdbc/moviedb")
	private DataSource dataSource;

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		PrintWriter out = response.getWriter();

		String gRecaptchaResponse = request.getParameter("g-recaptcha-response");
		System.out.println("gRecaptchaResponse=" + gRecaptchaResponse);
		
		String userAgent = request.getHeader("User-Agent");
	    System.out.println("recieved login request");
	    System.out.println("userAgent: " + userAgent);

	    /*
	    if (userAgent != null && !userAgent.contains("Android")) {
			try {
				RecaptchaVerifyUtils.verify(gRecaptchaResponse);
			} catch (Exception e) {
				out.println("<html>");
				out.println("<head><title>Error</title></head>");
				out.println("<body>");
				out.println("<p>recaptcha verification error</p>");
				out.println("<p>" + e.getMessage() + "</p>");
				out.println("</body>");
				out.println("</html>");
	
				out.close();
				return;
			}
	    }
		*/
	    
	    
		String username = request.getParameter("username");
		String password = request.getParameter("password");

		try {
			Context initCtx = new InitialContext();
			Context envCtx = (Context) initCtx.lookup("java:comp/env");
			DataSource ds = (DataSource) envCtx.lookup("jdbc/movie-pool");
			Connection dbcon = ds.getConnection();

			String query = "SELECT * from customers \n" + "WHERE email=?";

			PreparedStatement statement = dbcon.prepareStatement(query);
			statement.setString(1, username);

			ResultSet rs = statement.executeQuery();

			if (rs.next()) {
				String encryptedPassword = rs.getString("password");

				boolean success = new StrongPasswordEncryptor().checkPassword(password, encryptedPassword);
				System.out.println(success);
				if (success) {
					String sessionId = ((HttpServletRequest) request).getSession().getId();
					Long lastAccessTime = ((HttpServletRequest) request).getSession().getLastAccessedTime();
					request.getSession().setAttribute("user", new User(username));
					String id = rs.getString("id");
					request.getSession().setAttribute("id", id);

					JsonObject responseJsonObject = new JsonObject();
					responseJsonObject.addProperty("status", "success");
					responseJsonObject.addProperty("message", "success");

					response.getWriter().write(responseJsonObject.toString());
				}else {
					JsonObject responseJsonObject = new JsonObject();
					responseJsonObject.addProperty("status", "fail");
					responseJsonObject.addProperty("message", "username or password incorrect");
					response.getWriter().write(responseJsonObject.toString());
				}
			}

			else {
				JsonObject responseJsonObject = new JsonObject();
				responseJsonObject.addProperty("status", "fail");
				responseJsonObject.addProperty("message", "username or password incorrect");
				response.getWriter().write(responseJsonObject.toString());
			}
		}

		catch (Exception e) {
			// write error message JSON object to output
			JsonObject jsonObject = new JsonObject();
			jsonObject.addProperty("errorMessage", e.getMessage());
			out.write(jsonObject.toString());
			out.close();
			// set reponse status to 500 (Internal Server Error)
			response.setStatus(500);
		}

	}

}
