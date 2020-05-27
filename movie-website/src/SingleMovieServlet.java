

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

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

/**
 * Servlet implementation class SingleMovieServlet
 */
@WebServlet(name = "/SingleMovieServlet", urlPatterns="/api/single-movie")
public class SingleMovieServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
    /**
     * @see HttpServlet#HttpServlet()
     */
    public SingleMovieServlet() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("application/json");
		String id = request.getParameter("id");
		PrintWriter out = response.getWriter();
		
		try {
			
			// Get a connection from dataSource
			Context initCtx = new InitialContext();
			Context envCtx = (Context) initCtx.lookup("java:comp/env");
			DataSource ds = (DataSource) envCtx.lookup("jdbc/movie-pool");
            Connection dbcon = ds.getConnection();
			System.out.println(dbcon + " from single movie");
			// Construct a query with parameter represented by "?"
			String query ="SELECT movies.id, movies.title, movies.year, movies.director as movie_director, ratings.rating AS movie_rating, GROUP_CONCAT(DISTINCT genres.name SEPARATOR ';') AS genres, \n" +  
					"GROUP_CONCAT( DISTINCT CONCAT(stars.id, ',' , stars.name) SEPARATOR ';') AS stars_info \n" + 
					"FROM movies \n" + 
					"LEFT JOIN ratings ON movies.id=ratings.movieId \n" + 
					"LEFT JOIN genres_in_movies ON movies.id=genres_in_movies.movieId \n" +
					"LEFT JOIN genres ON genres_in_movies.genreId=genres.id \n" + 
					"LEFT JOIN stars_in_movies ON movies.id= stars_in_movies.movieId \n" + 
					"LEFT JOIN stars ON stars_in_movies.starId=stars.id \n" + 
					"WHERE movies.id = ? \n" +
					"GROUP BY id, title, year, movie_director, movie_rating \n";

			// Declare our statement
			PreparedStatement statement = dbcon.prepareStatement(query);

			// Set the parameter represented by "?" in the query to the id we get from url,
			// num 1 indicates the first "?" in the query
			statement.setString(1, id);

			// Perform the query
			ResultSet rs = statement.executeQuery();

			JsonArray jsonArray = new JsonArray();
			
			// Iterate through each row of rs
			while (rs.next()) {

				String movie_id = rs.getString("id");
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
				jsonObject.addProperty("id", movie_id);
				jsonObject.addProperty("title", movie_title);
				jsonObject.addProperty("year", year);
				jsonObject.addProperty("director", movie_director);
				jsonObject.addProperty("movie_rating", movie_rating);
				jsonObject.add("genres", genres_json);
				jsonObject.add("stars", stars_json);
				jsonArray.add(jsonObject);	
			}
			
            // write JSON string to output
            out.write(jsonArray.toString());
            // set response status to 200 (OK)
            response.setStatus(200);

			rs.close();
			statement.close();
			dbcon.close();
		} catch (Exception e) {
			// write error message JSON object to output
			JsonObject jsonObject = new JsonObject();
			jsonObject.addProperty("errorMessage", e.getMessage());
			out.write(jsonObject.toString());

			// set reponse status to 500 (Internal Server Error)
			response.setStatus(500);
		}
		out.close();
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}

}
