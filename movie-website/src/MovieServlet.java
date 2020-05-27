import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import javax.annotation.Resource;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

/**
 * Servlet implementation class MovieServlet
 */
@WebServlet(name = "/MovieServlet", urlPatterns = "/api/movies")
public class MovieServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public MovieServlet() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		response.setContentType("application/json"); // Response mime type
				
		String orderBy = request.getParameter("orderBy");
		String order = request.getParameter("order");
		String browse = request.getParameter("browse");
		
		System.out.println(browse);
		String browseQuery = "";
		if(browse != null && !browse.equals("")) {
			if(browse.length() < 2) {
				browseQuery = "WHERE movies.title LIKE ? \n";
			}
		}
		if( orderBy == null || orderBy.equals("null")) {
			orderBy = "movie";
		}
		
		if( order == null || order.equals("null")) {
			order = "DESC";
		}

		String lim = request.getParameter("limit");
		String off = request.getParameter("offset");
		
		PrintWriter out = response.getWriter();

		try {
			// Incorporate mySQL driver
			Class.forName("com.mysql.jdbc.Driver").newInstance();

			// Connect to the test database
			Context initCtx = new InitialContext();
			Context envCtx = (Context) initCtx.lookup("java:comp/env");
			DataSource ds = (DataSource) envCtx.lookup("jdbc/movie-pool");
            Connection dbcon = ds.getConnection();
			System.out.println(dbcon + " from movies");

			String query = "SELECT movies.id, movies.title, movies.year, movies.director as movie_director, ratings.rating AS movie_rating, GROUP_CONCAT(DISTINCT genres.name SEPARATOR ';') AS genres, \n" +  
					"GROUP_CONCAT( DISTINCT CONCAT(stars.id, ',' , stars.name) SEPARATOR ';') AS stars_info \n" + 
					"FROM movies \n" + 
					"LEFT JOIN ratings ON movies.id=ratings.movieId \n" + 
					"LEFT JOIN genres_in_movies ON movies.id=genres_in_movies.movieId \n" +
					"LEFT JOIN genres ON genres_in_movies.genreId=genres.id \n" +
					"LEFT JOIN stars_in_movies ON movies.id= stars_in_movies.movieId \n" +
					"LEFT JOIN stars ON stars_in_movies.starId=stars.id \n" +
					browseQuery + 
					"GROUP BY id, title, year, movie_director, movie_rating \n";
			
			if(browse != null && !browse.equals("null")) {
				if(browse.length() >= 2) {
					query = query + "HAVING genres LIKE ? \n";
				}
			}
			
			query = query + "ORDER BY ";
								
						
			if ( orderBy.equals("title")) {
				query = query + "title ";
			}
			else if (orderBy.equals("movie")) {
				query = query + "movie_rating ";
			}
				
			if( order.equals("DESC")) {
				query = query + "DESC ";
			}
			else if (order.equals("ASC")) {
				query = query + "ASC ";
			}
			
			query = query + "LIMIT ? OFFSET ? ;";
			
			
			PreparedStatement statement = dbcon.prepareStatement(query);
			System.out.println(statement);
			int r=0;

			if(browse != null && !browse.equals("null")) {
				if(browse.length() < 2) {
					statement.setString(1, browse + "%");
					r = 1;
				}
			}
			
			if(browse != null && !browse.equals("null")) {
				if(browse.length() >= 2) {
					statement.setString(1, "%" + browse + "%");
					r = 1;
				}
			}

	
			if(lim != null) {
				statement.setInt(1+r, Integer.parseInt(lim));
			}else {
				System.out.println("setting limit to 20 by default");
				statement.setInt(1+r, 20);
			}
			if(off != null) {
				statement.setInt(2+r, Integer.parseInt(off));
			}else {
				System.out.println("setting offset to 0 by default");
				statement.setInt(2+r, 0);
			}
						
			System.out.println(statement);
			
			ResultSet rs = statement.executeQuery();
			
			JsonArray jsonArray = new JsonArray();
			while (rs.next()) {
			
				String id = rs.getString("id");
				String movie_title = rs.getString("title");
				int year = rs.getInt("year");
				String movie_director = rs.getString("movie_director");
				String movie_rating = rs.getString("movie_rating");
				String g = rs.getString("genres");
				String s = rs.getString("stars_info"); 
				String [] genres = null;
				String [] stars_info = null;
				JsonArray genres_json = new JsonArray();
				JsonArray stars_json = new JsonArray();
				
				if ( g != null) {
					genres = rs.getString("genres").split(";");
					
					for( int i = 0; i < genres.length; ++i) {
						genres_json.add(genres[i]);
					}
				}
				if ( s != null) {
					stars_info = rs.getString("stars_info").split(";");
					
					for( int i = 0; i<stars_info.length; ++i) {
						JsonObject stars_object = new JsonObject();
						String [] star_tuple = stars_info[i].split(",");
						stars_object.addProperty("star_id", star_tuple[0]);
						stars_object.addProperty("star_name", star_tuple[1]);
						stars_json.add(stars_object);
					}
				}
				
				JsonObject jsonObject = new JsonObject();
				jsonObject.addProperty("id", id);
				jsonObject.addProperty("title", movie_title);
				jsonObject.addProperty("year", year);
				jsonObject.addProperty("director", movie_director);
				jsonObject.addProperty("movie_rating", movie_rating);
				jsonObject.add("genres", genres_json);
				jsonObject.add("stars", stars_json);
				jsonArray.add(jsonObject);	
			}
			
			out.write(jsonArray.toString());
			response.setStatus(200);

			rs.close();
			statement.close();
			dbcon.close();

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			// write error message JSON object to output
			JsonObject jsonObject = new JsonObject();
			jsonObject.addProperty("errorMessage", e.getMessage());
			out.write(jsonObject.toString());

			// set reponse status to 500 (Internal Server Error)
			response.setStatus(500);

		}

		out.close();
		// Declare our statement

	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}

}
