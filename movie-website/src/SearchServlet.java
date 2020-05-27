
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

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
 * Servlet implementation class SearchServlet
 */
@WebServlet(name = "/SearchServlet", urlPatterns = "/api/search")
public class SearchServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		long tsStart = System.nanoTime();
		response.setContentType("application/json"); // Response mime type

		String orderBy = request.getParameter("orderBy");
		String order = request.getParameter("order");
		String title = request.getParameter("title");
		String search_year = request.getParameter("year");
		String search_director = request.getParameter("director");
		String search_star = request.getParameter("star");
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

			String query = "SELECT movies.id, movies.title, movies.year, movies.director as movie_director, ratings.rating AS movie_rating, GROUP_CONCAT(DISTINCT genres.name SEPARATOR ';') AS genres, \n"
					+ "GROUP_CONCAT( DISTINCT CONCAT(stars.id, ',' , stars.name) SEPARATOR ';') AS stars_info \n"
					+ "FROM movies \n"
					+ "LEFT JOIN ratings ON movies.id=ratings.movieId \n"
					+ "LEFT JOIN genres_in_movies ON movies.id=genres_in_movies.movieId \n"
					+ "LEFT JOIN genres ON genres_in_movies.genreId=genres.id \n"
					+ "LEFT JOIN stars_in_movies ON movies.id= stars_in_movies.movieId \n"
					+ "LEFT JOIN stars ON stars_in_movies.starId=stars.id \n"
					+ "WHERE \n"
					+ "(movies.title LIKE ? OR movies.title LIKE ? OR movies.title LIKE ?) AND"
					+ "(movies.year LIKE ?) AND \n"
					+ "(movies.director LIKE ? OR movies.director LIKE ? OR movies.director LIKE ?) \n"
					+ "GROUP BY id, title, year, movie_director, movie_rating \n";
					
			
					if( search_star != null && !search_star.equals("") ) {
						query = query + "HAVING stars_info LIKE ? OR stars_info LIKE ? OR stars_info LIKE ? \n";
					}
			
					if( orderBy != null && order != null) {
						
						if ( orderBy.equals("title")) {
							query = query + "ORDER BY title ";
						}
						else if (orderBy.equals("movie")) {
							query = query + "ORDER BY movie_rating ";
						}
						
						if (!order.equals("null")) {
							
							if( order.equals("DESC")) {
								query = query + "DESC ";
							}
							else if (order.equals("ASC")){
								query = query + "ASC ";
							}
						}
					}
					
					
					query = query + "LIMIT ? OFFSET ? ";
							
			PreparedStatement statement = dbcon.prepareStatement(query);
			
			statement.setString(1, "%" + title);
			statement.setString(2, title + "%");
			statement.setString(3, "%" + title + "%");
			statement.setString(4, search_year + "%");
			statement.setString(5, search_director + "%");
			statement.setString(6, "%" + search_director);
			statement.setString(7, "%" + search_director + "%");
			
			if ( search_star != null && !search_star.equals("")) {
				statement.setString(8, search_star + "%");
				statement.setString(9, "%" + search_star);
				statement.setString(10, "%" + search_star + "%");
			}
			int r;
			
			if ( search_star == null || search_star.equals("")) {
				r = -3;
			}
			else {
				r = 0;
			}
						
			if(lim != null) {
				statement.setInt(11+r, Integer.parseInt(lim));
			}else {
				statement.setInt(11+r, 20);
			}
			if(off != null) {
				statement.setInt(12+r, Integer.parseInt(off));
			}else {
				statement.setInt(12+r, 0);
			}
			
			
			long tjStart = System.nanoTime();
			ResultSet rs = statement.executeQuery();
			long tjEnd = System.nanoTime();
			long tjTotal = tjEnd - tjStart;
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
			// write JSON string to output
			out.write(jsonArray.toString());
			// set response status to 200 (OK)
			response.setStatus(200);

			rs.close();
			statement.close();
			dbcon.close();
			
			long tsEnd = System.nanoTime();
			long tsTotal = tsEnd - tsStart;
			
			String contextPath = getServletContext().getRealPath("/");
			String filePath = contextPath+"\\test";
						
			File myfile = new File(filePath);
			myfile.createNewFile();
			FileWriter writer = new FileWriter(myfile, true);
			
			writer.write(tsTotal + ";" + tjTotal+"\n");
			writer.flush();
			writer.close();
			
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

}