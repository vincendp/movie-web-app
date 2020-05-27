

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Types;

import javax.annotation.Resource;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

import com.google.gson.JsonObject;

/**
 * Servlet implementation class InsertStarAndMovieServlet
 */
@WebServlet(name="InsertStarAndMovieServlet", urlPatterns="/api/insert-new-stars-and-movies")
public class InsertStarAndMovieServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	@Resource(name = "jdbc/moviedb")
    private DataSource dataSource;
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String movies = request.getParameter("movies");
		PrintWriter out = response.getWriter();

		if ( movies != null && !movies.equals("")) {
			
			System.out.println("hi");
			
			String title = request.getParameter("movie-title");
			int year = Integer.parseInt(request.getParameter("movie-year"));
			String director = request.getParameter("movie-director");
			String star = request.getParameter("movie-star");
			String genre = request.getParameter("movie-genre");
			
			try {
				Context initCtx = new InitialContext();
				Context envCtx = (Context) initCtx.lookup("java:comp/env");
				DataSource ds = (DataSource) envCtx.lookup("jdbc/movie-master");
				Connection dbcon = ds.getConnection();
				System.out.println(dbcon + " from insert star and movie 1");

				String sql = "{CALL add_movie(?, ?, ?, ?, ?, ?, ?, ?)}";
				CallableStatement cstatement = dbcon.prepareCall(sql);
				cstatement.setString(1, title);
				cstatement.setInt(2, year);
				cstatement.setString(3, director);
				cstatement.setString(4, star);
				cstatement.setString(5, genre);					
				cstatement.registerOutParameter(6, Types.INTEGER);
				cstatement.registerOutParameter(7, Types.INTEGER);
				cstatement.registerOutParameter(8, Types.INTEGER);
				
				System.out.println(cstatement);
				
				cstatement.execute();
				
				int movieMessage = cstatement.getInt(6);
				int genreMessage = cstatement.getInt(7);
				int starMessage = cstatement.getInt(8); 
				
				System.out.println(movieMessage + " " + genreMessage + " " + starMessage);
				JsonObject jsonObject = new JsonObject();
				
				if ( movieMessage == 0) {
					jsonObject.addProperty("movie_message", title + " (" + year + ") directed by " + director + " did not exist before. Added movie to list.");
					
					if ( genreMessage == 0) {
						jsonObject.addProperty("genre_message", genre + " did not exist before. Added genre and linked to " + title);
					}
					else {
						jsonObject.addProperty("genre_message", genre + " is an existing genre. Linked to " + title);
					}
					
					if (starMessage == 0) {
						jsonObject.addProperty("star_message", star + " did not exist before. Added star and linked to " + title);
					}
					else {
						jsonObject.addProperty("star_message", star + " is an existing star. Linked to " + title);
					}
				}
				else {
					jsonObject.addProperty("movie_message", title + " (" + year + ") directed by " + director + " was already found in movie list.");
					
					if ( genreMessage == 0) {
						jsonObject.addProperty("genre_message", genre + " did not exist before. Added genre and linked to " + title);
					}
					else if (genreMessage == 1){
						jsonObject.addProperty("genre_message", genre + " was already linked to " + title + ". No changes made.");
					}
					else if (genreMessage == 2){
						jsonObject.addProperty("genre_message", genre + " is an existing genre. Linked to " + title);
					}
					
					
					if (starMessage == 0) {
						jsonObject.addProperty("star_message", star + " did not exist before. Added star and linked to " + title);
					}
					else if ( starMessage == 1){
						jsonObject.addProperty("star_message", star + " was already linked to " + title + ". No changes made.");
					}
					else if ( starMessage == 2){
						jsonObject.addProperty("star_message", star + " is an existing star. Linked to " + title);
					}
				}
								
				System.out.println(jsonObject.toString());
				
				out.write(jsonObject.toString());
				
				cstatement.close();
				dbcon.close();
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
		
		
		else {
		
			try {
				String star = request.getParameter("star-name");
				String year = request.getParameter("year");
				Context initCtx = new InitialContext();
				Context envCtx = (Context) initCtx.lookup("java:comp/env");
				DataSource ds = (DataSource) envCtx.lookup("jdbc/movie-master");
				Connection dbcon = ds.getConnection();
				System.out.println(dbcon + " from insert star and movie 2");

				String sql = "{CALL add_star(?, ?, ?)}";
				CallableStatement cstatement = dbcon.prepareCall(sql);
				cstatement.setString(1, star);
				
				if( year == null || year.equals("") || year.equals("null")) {
					cstatement.setNull(2, Types.INTEGER);
				}
				else {
					cstatement.setInt(2, Integer.parseInt(year));
				}
					
				cstatement.registerOutParameter(3, Types.VARCHAR);
				cstatement.execute();
				String id = cstatement.getString(3);
				
				JsonObject jsonObject = new JsonObject();
				String message = "Successfully added star " + id + " " + star + " with birth year "; 
				
				if  (year == null || year.equals("") || year.equals("null")) {
					message = message + "NULL";
				}
				else {
					message = message + year;
				}
				
				jsonObject.addProperty("message", message);
				out.write(jsonObject.toString());
			
				cstatement.close();
				dbcon.close();
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
		out.close();	
	}


	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
	}

}
