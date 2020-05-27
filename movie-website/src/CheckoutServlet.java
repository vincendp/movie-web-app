

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Hashtable;
import java.util.Set;
import java.util.UUID;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.sql.DataSource;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

/**
 * Servlet implementation class CheckoutServlet
 */
@WebServlet(name="CheckoutServlet", urlPatterns="/api/checkout")
public class CheckoutServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public CheckoutServlet() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    	HttpSession session = request.getSession();
    	String id = (String) session.getAttribute("id");
    	String confirmation = request.getParameter("confirmation");
		PrintWriter out = response.getWriter();
        Hashtable<String, Integer> previousItems = (Hashtable<String, Integer>) session.getAttribute("previousItems");
        
        if(confirmation != null && !confirmation.isEmpty()) {
        	session.setAttribute("confirmation", true);
        	return;
        }
        
    	try {
    		
    		Date date = new Date();
    		String strDateFormat = "yyyy-MM-dd";
    		DateFormat dateFormat = new SimpleDateFormat(strDateFormat);
    		String current_date = dateFormat.format(date);
    		
    		System.out.println("Got date:" + current_date);
    		
    		Context initCtx = new InitialContext();
    		Context envCtx = (Context) initCtx.lookup("java:comp/env");
    		DataSource ds = (DataSource) envCtx.lookup("jdbc/movie-master");
    		Connection dbcon = ds.getConnection();
			System.out.println(dbcon + " from checkout");

			
			String sql = "";
			PreparedStatement statement = null;
			ResultSet rs = null;
			int saleId = 0;
			String movie_name = "";
			JsonArray jsonArray = new JsonArray();
			Set<String> setOfMovies = previousItems.keySet();
			
			for(String key: setOfMovies) {
				
				for( int i = 0; i < previousItems.get(key); ++i) {
	    			sql = "INSERT INTO sales (customerId, movieId, saleDate) \n" + 
	    				   "VALUES (?, ?, ?)"; 
	    			
	    			statement = dbcon.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
	    			statement.setString(1, id);
	    			statement.setString(2, key);
	    			statement.setString(3, current_date);
	    			
	    			System.out.println(statement);
	    			statement.executeUpdate();
	    			rs = statement.getGeneratedKeys();
	    			
	    			if( rs.next() ) {
	    				saleId = rs.getInt(1);
	    			}
	    			
	    			
	    			sql = "SELECT title FROM movies WHERE id= ?"; 
	    			statement = dbcon.prepareStatement(sql);
	    			statement.setString(1, key);
	    			rs = statement.executeQuery();
	    			
	    			if( rs.next()) {
	    				movie_name = rs.getString("title");
	    			}
	    			
	    			JsonObject jsonObject = new JsonObject();
	    			jsonObject.addProperty("saleId", saleId);
	    			jsonObject.addProperty("movie_name", movie_name);
	    			jsonObject.addProperty("quantity", previousItems.get(key));
	    			jsonArray.add(jsonObject);	
				}
    		}
			
			
			out.write(jsonArray.toString());
			response.setStatus(200);
			rs.close();
			statement.close();
			dbcon.close();
			
		}
		
		
		catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

		}
		
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		String id = request.getParameter("ccid");
		String expiration_date = request.getParameter("date");
		String first_name = request.getParameter("first");
		String last_name = request.getParameter("last");
		PrintWriter out = response.getWriter();
		
		try {
			Context initCtx = new InitialContext();
			Context envCtx = (Context) initCtx.lookup("java:comp/env");
			DataSource ds = (DataSource) envCtx.lookup("jdbc/movie-pool");
			Connection dbcon = ds.getConnection();

			
			String query = "SELECT * FROM creditcards \n" + 
						"WHERE id=? AND firstName=? AND lastName=? AND expiration=? ";
			
    		System.out.println(query);
	
			PreparedStatement statement = dbcon.prepareStatement(query);
			statement.setString(1, id);
			statement.setString(2, first_name);
			statement.setString(3, last_name);
			statement.setString(4, expiration_date);
			
			ResultSet rs = statement.executeQuery();
			JsonObject jsonObject = new JsonObject();
			
			if( rs.next()) {
				jsonObject.addProperty("success", 1);
			}
			
			else {
				jsonObject.addProperty("fail", 1);
			}
			
			out.write(jsonObject.toString());
			response.setStatus(200);

			rs.close();
			statement.close();
			dbcon.close();
		}
		
		
		catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

		}
	}

}
