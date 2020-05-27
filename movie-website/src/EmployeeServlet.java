
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
 * Servlet implementation class EmployeeServlet
 */
@WebServlet(name = "EmployeeServlet", urlPatterns = "/api/employee-login")
public class EmployeeServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;


	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		PrintWriter out = response.getWriter();
		String username = request.getParameter("username");
		String password = request.getParameter("password");

		try {
			Context initCtx = new InitialContext();
			Context envCtx = (Context) initCtx.lookup("java:comp/env");
			DataSource ds = (DataSource) envCtx.lookup("jdbc/movie-pool");
			Connection dbcon = ds.getConnection();
			System.out.println(dbcon + " from employee");

			
			String query = "SELECT * from employees \n" + "WHERE email=?";

			PreparedStatement statement = dbcon.prepareStatement(query);
			statement.setString(1, username);

			ResultSet rs = statement.executeQuery();

			if (rs.next()) {
				String encryptedPassword = rs.getString("password");
				boolean success = new StrongPasswordEncryptor().checkPassword(password, encryptedPassword);
				
				if(success) {
					JsonObject responseJsonObject = new JsonObject();
					responseJsonObject.addProperty("status", "success");
					responseJsonObject.addProperty("message", "success");
					request.getSession().setAttribute("employee-user", new User(username));
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
