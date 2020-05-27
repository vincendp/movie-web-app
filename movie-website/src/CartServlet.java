import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.sql.DataSource;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.Set;


@WebServlet(name = "IndexServlet", urlPatterns = "/api/cart")
public class CartServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    /**
     * handles POST requests to store session information
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    	HttpSession session = request.getSession();
        String sessionId = session.getId();
        Long lastAccessTime = session.getLastAccessedTime();

        JsonObject responseJsonObject = new JsonObject();
        responseJsonObject.addProperty("sessionID", sessionId);
        responseJsonObject.addProperty("lastAccessTime", new Date(lastAccessTime).toString());

        // write all the data into the jsonObject
        response.getWriter().write(responseJsonObject.toString());
        System.out.println(responseJsonObject.toString());
    }

    /**
     * handles GET requests to add and show the item list information
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    	
    	String movieId = request.getParameter("movieId");
    	String update = request.getParameter("update");
    	String deleteId = request.getParameter("deleteId");
    	String deleteAll = request.getParameter("deleteAll");
    	HttpSession session = request.getSession();
        Hashtable<String, Integer> previousItems = (Hashtable<String, Integer>) session.getAttribute("previousItems");
		
        if (previousItems == null) {
        	previousItems = new Hashtable<>();
        	session.setAttribute("previousItems", previousItems);
        }
        
        /*
         * Called whenever user clicks on Add to Cart on any movie 
         * go through this block
         */
    	if( movieId != null && !movieId.isEmpty() ) {

    		// If the movie isn't in the cart, add it to previousItems
 	        if ( !previousItems.containsKey(movieId)) {
 	        	synchronized (previousItems) {
 	                previousItems.put(movieId, 1);
 	            }
 	        }
  	        response.getWriter().write(previousItems.toString());
    		
    	}
    	/*
    	 * This block called when user clicks update for a movie quantity on the 
    	 * cart html page
    	 */
    	else if (update != null && !update.isEmpty() ) {
    		movieId = request.getParameter("id");
    		int quantity = Integer.parseInt(request.getParameter("quantity"));
    		
    		if(quantity <= 0) {
    			synchronized (previousItems) {
	                previousItems.remove(movieId); 
	                getCart(response, previousItems);
	            }
    		}
    		else {
	    		synchronized (previousItems) {
	    				PrintWriter out = response.getWriter();
	    				JsonObject jsonObject = new JsonObject();
	    				jsonObject.addProperty("servlet_response_no_refresh", 1);			
		                previousItems.put(movieId, quantity);
		                out.write(jsonObject.toString());
		                out.close();
		            }
    		}

    	}
    	
    	/*
    	 * Called when user hits delete button client side. Deletes 
    	 * movie from Cart 
    	 */
    	else if ( deleteId != null && !deleteId.isEmpty()) {
    		synchronized (previousItems) {
                previousItems.remove(deleteId); 
                getCart(response, previousItems);
            }
    	}
    	
    	else if ( deleteAll != null && !deleteAll.isEmpty()) {
    		synchronized ( previousItems) {
    			previousItems.clear();
    		}
    	}
    	
    	/*
    	 * This block called when user goes to the cart.html page
    	 */
    	
    	else {
    		getCart(response, previousItems);
    	}
    }
    
    /*
     * Goes into database to fetch the corresponding movie title and as well as sends back a JSON
     * object with [{id:val, title:val, quantity:val}]
     */
    void getCart(HttpServletResponse response, Hashtable<String, Integer> previousItems) throws IOException {
    	
		PrintWriter out = response.getWriter();

		try {
			Context initCtx = new InitialContext();
			Context envCtx = (Context) initCtx.lookup("java:comp/env");
			DataSource ds = (DataSource) envCtx.lookup("jdbc/movie-pool");
			Connection dbcon = ds.getConnection();
			System.out.println(dbcon + " from cart");

			
			
			
			Set<String> setOfMovies = previousItems.keySet();
			int i = 0; 
			int size = setOfMovies.size();
			
			if ( size == 0) {
				JsonObject jsonObject = new JsonObject();
				jsonObject.addProperty("servlet_response_no_movies", 1);
				out.write(jsonObject.toString());
				out.close();
				dbcon.close();
				return;
			}
			String query = "SELECT id, title \n" + 
					"FROM movies \n" + 
					"WHERE id= ?";
			PreparedStatement statement = dbcon.prepareStatement(query);
			JsonArray jsonArray = new JsonArray();
    		for(String key: setOfMovies) {
    			
    			statement.setString(1, key);
    			ResultSet rs = statement.executeQuery();
    			while (rs.next()) {
    				String id = rs.getString("id");
    				String title = rs.getString("title");
    				int quantity = previousItems.get(id);
    				
    				JsonObject jsonObject = new JsonObject();
    				jsonObject.addProperty("id", id);
    				jsonObject.addProperty("title", title);
    				jsonObject.addProperty("quantity", quantity);
    				
    				jsonArray.add(jsonObject);	
    				
    			}
    			rs.close();

    		}

			
			out.write(jsonArray.toString());
			response.setStatus(200);

			statement.close();
			dbcon.close();
		}
		
		
		catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

		}
	}
}
