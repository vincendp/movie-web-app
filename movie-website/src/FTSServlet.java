

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

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
 * Servlet implementation class FTSServlet
 */
@WebServlet(name="/FTSServlet", urlPatterns = "/api/fts")
public class FTSServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public FTSServlet() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		long tsStart = System.nanoTime();

		String search_title = request.getParameter("title");
		String lim = request.getParameter("limit");
		String off = request.getParameter("offset");
		
		PrintWriter out = response.getWriter();
		
		try {
			Context initCtx = new InitialContext();
			Context envCtx = (Context) initCtx.lookup("java:comp/env");
			DataSource ds = (DataSource) envCtx.lookup("jdbc/movie-pool");
			Connection dbcon = ds.getConnection();
			System.out.println(dbcon + " from fts");

			
			PreparedStatement statement = null;
			ResultSet rs = null;
			String query = null;
			
			String [] keywords = search_title.split(" ");
			String fulltext = "";
			
			query = "SELECT DISTINCT entry FROM ft WHERE MATCH (entry) AGAINST (?";
			
			if( keywords.length > 1) {
				
				for( int i = 0; i<keywords.length; ++i) {
					fulltext = fulltext + "+" +keywords[i] + "* ";
				}
			}
			else {
				fulltext = keywords[0] + "* ";
			}
			
			int edit_distance = search_title.length() <= 10 ? 2 : 3;
			query = query + " IN BOOLEAN MODE) OR SIMILARTO(entry, ?, ?) \n";
			query = query + "LIMIT ? OFFSET ? ;";
			
			long tjStart = System.nanoTime();
			
			statement = dbcon.prepareStatement(query);
			statement.setString(1, fulltext);
			statement.setString(2, search_title);
			statement.setInt(3, edit_distance);
			if(lim == null) {
				statement.setInt(4, 10);
			}
			else {
				statement.setInt(4, Integer.parseInt(lim));
			}
			
			if(off == null) {
				statement.setInt(5, 0);
			}
			else {
				statement.setInt(5, Integer.parseInt(off));
			}
			
			System.out.println(statement);
			rs = statement.executeQuery();
			
			long tjEnd = System.nanoTime();
			long tjTotal = tjEnd - tjStart;

			
			JsonArray jsonArray = new JsonArray();
			
			while (rs.next()) {
				
				String title = rs.getString("entry");
				
				query = "SELECT movies.id, movies.title, movies.year, movies.director as movie_director, ratings.rating AS movie_rating, GROUP_CONCAT(DISTINCT genres.name SEPARATOR ';') AS genres, \n"
						+ "GROUP_CONCAT( DISTINCT CONCAT(stars.id, ',' , stars.name) SEPARATOR ';') AS stars_info \n"
						+ "FROM movies \n"
						+ "LEFT JOIN ratings ON movies.id=ratings.movieId \n"
						+ "LEFT JOIN genres_in_movies ON movies.id=genres_in_movies.movieId \n"
						+ "LEFT JOIN genres ON genres_in_movies.genreId=genres.id \n"
						+ "LEFT JOIN stars_in_movies ON movies.id= stars_in_movies.movieId \n"
						+ "LEFT JOIN stars ON stars_in_movies.starId=stars.id \n"
						+ "WHERE title=?\n"
						+ "GROUP BY id, title, year, movie_director, movie_rating \n";
								
				tjStart = System.nanoTime();
				statement = dbcon.prepareStatement(query);
				statement.setString(1, title);				
				ResultSet rs_movies = statement.executeQuery();	
				tjEnd = System.nanoTime();
				tjTotal += tjEnd - tjStart;
				
				while (rs_movies.next()) {
					String id = rs_movies.getString("id");
					String movie_title = rs_movies.getString("title");
					int year = rs_movies.getInt("year");
					String movie_director = rs_movies.getString("movie_director");
					String movie_rating = rs_movies.getString("movie_rating");
					String g = rs_movies.getString("genres");
					String s = rs_movies.getString("stars_info"); 
					String [] genres = null;
					String [] stars_info = null;
					JsonArray genres_json = new JsonArray();
					JsonArray stars_json = new JsonArray();
					
					if ( g != null) {
						genres = rs_movies.getString("genres").split(";");
						
						for( int i = 0; i < genres.length; ++i) {
							genres_json.add(genres[i]);
						}
					}
					if ( s != null) {
						stars_info = rs_movies.getString("stars_info").split(";");
						
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
			
			System.out.println("writing output to " + filePath);
			
			File myfile = new File(filePath);
			myfile.createNewFile();
			FileWriter writer = new FileWriter(myfile, true);
			writer.write(tsTotal + ";" + tjTotal+"\n");
			writer.flush();
			writer.close();
		}
		
		catch (Exception e) {
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
		
			
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	}

}
