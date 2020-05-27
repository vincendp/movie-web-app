import java.sql.BatchUpdateException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.sql.SQLException;

/*

The SQL command to create the table ft.

DROP TABLE IF EXISTS ft;
CREATE TABLE ft (
    entryID INT AUTO_INCREMENT,
    entry text,
    PRIMARY KEY (entryID),
    FULLTEXT (entry)) ENGINE=MyISAM;

*/

/*

Note: Please change the username, password and the name of the datbase.

*/

public class BatchInsert {
	
	
	
	public static void main(String[] args)
			throws InstantiationException, IllegalAccessException, ClassNotFoundException {
		Connection conn = null;
		Class.forName("com.mysql.jdbc.Driver").newInstance();
		String jdbcURL = "jdbc:mysql://localhost:3306/moviedb";

		try {
			conn = DriverManager.getConnection(jdbcURL, "mytestuser", "mypassword");
		} catch (SQLException e) {
			e.printStackTrace();
		}

		Statement st = null;
		ResultSet rs = null;
		PreparedStatement psInsertRecord = null;
		PreparedStatement psGenreInsert = null;
		PreparedStatement psGIMInsert = null;
		String sqlInsertStar = null;
		String sqlInsertMovie = null;
		String sqlInsertSIM = null;
		String sqlInsertGenre = null;
		String sqlInsertGIM = null;
		int[] iNoRows = null;
		
		sqlInsertMovie = "INSERT INTO movies (id, title, year, director) VALUES (?, ?, ?, ?)";
		sqlInsertStar = "CALL add_star(?,?, @a);"; //might need to modify this query
		sqlInsertSIM = "INSERT INTO stars_in_movies (starId, movieId) VALUES (?,?);"; //stars_in_movies
		sqlInsertGenre = "CALL add_genre(?,@b)";
		sqlInsertGIM = "INSERT INTO genres_in_movies (genreId, movieId) VALUES (?, ?)"; //genres_in_movies
		try {
			conn.setAutoCommit(false);

			
			// get hashset of all genres (only names)
			String selectAllGenres = "select name from genres;";
			st = conn.createStatement();
			rs = st.executeQuery(selectAllGenres);
			HashSet<String> genres = new HashSet<>(); 
			
			while(rs.next()) {
				String name = rs.getString("name"); //genres already in the table
				genres.add(name);
			}
			System.out.println("PRINTING OUT GNERES: "+genres);
			// call parser and get all movies from XML
			psInsertRecord = conn.prepareStatement(sqlInsertMovie);
			psGenreInsert = conn.prepareStatement(sqlInsertGenre);
			psGIMInsert = conn.prepareStatement(sqlInsertGIM);
			MoviesAndGenresParser mgp = new MoviesAndGenresParser();
			mgp.runExample();
			
			HashSet<Movie> movies = mgp.myMovies;
			
			// iterate through those movies
			Iterator<Movie> movie_it = movies.iterator();
			HashMap<String, HashSet<String>> titleToGenresMap = new HashMap<>();
	        while (movie_it.hasNext()) {
	        	
	        	// insert movies into database
	            Movie m = movie_it.next();
	            psInsertRecord.setString(1, m.getId());
            	psInsertRecord.setString(2, m.getTitle());
            	psInsertRecord.setInt(3, m.getYear());
            	psInsertRecord.setString(4, m.getDirector()); 	
            	psInsertRecord.addBatch();
            	
            	// insert genres into database
	            HashSet<String> movie_genres = m.getGenres();
	            //each movie has many genres
	            titleToGenresMap.put(m.getTitle(), movie_genres);
	            
	            System.out.println("PRNTING OUT MOVIE-GENRES: "+movie_genres);
	            System.out.println();
	            for(String s: movie_genres) {
	            	if(s!=null && !genres.contains(s.trim())) { //not alreayd in table
		            	psGenreInsert.setString(1, s.trim());
		            	psGenreInsert.addBatch();
		            	genres.add(s.trim());
	            	}
	            }          
	        }
	        try {
		        psInsertRecord.executeBatch();
	        }catch(Exception e) {
	        	System.out.println("Duplicate Movie IDs found, not adding.");
	        }
			psGenreInsert.executeBatch();
	        // get hashmap of all genres (name, id)
	        selectAllGenres = "select id, name from genres;";
			st = conn.createStatement();
			rs = st.executeQuery(selectAllGenres);
			HashMap<String, Integer> genresTable = new HashMap<>();
			while(rs.next()) {
				int id = Integer.parseInt(rs.getString("id"));
				String name = rs.getString("name");
				genresTable.put(name, id);
			}
	        
			// get hashmap of all movies (title, id)
			String selectAllMovies = "select id, title from movies;";
			st = conn.createStatement();
			rs = st.executeQuery(selectAllMovies);
			HashMap<String, String> moviesTable = new HashMap<>();
			while(rs.next()) {
				String id = rs.getString("id");
				String title = rs.getString("title");
				moviesTable.put(title, id);
				
			}
			
			
			for (HashMap.Entry<String, HashSet<String>> entry :titleToGenresMap.entrySet())
			{
				String movie_id = moviesTable.get(entry.getKey()) ;
				if(movie_id == null) {
					continue;
				}
				
				for(String s: entry.getValue()) {
					if(s != null) {
						int genre_id = genresTable.get(s);
		            	
		            	psGIMInsert.setInt(1, genre_id);
		            	psGIMInsert.setString(2, movie_id);
		            	psGIMInsert.addBatch();
					}
	            	
	            }          
			}
			
			psGIMInsert.executeBatch();
			
			
			psInsertRecord = conn.prepareStatement(sqlInsertStar);
			//fill up stars set with xml data
			StarsParser dpe = new StarsParser();
			
			
			dpe.runExample();
						
			HashSet<Star> stars = dpe.myStars; //set containing stars data
			for(Star s: stars) { //add all data from stars set into the batch
				psInsertRecord.setString(1, s.getName());
				psInsertRecord.setString(2, s.getDOB());
				psInsertRecord.addBatch();
			}
			iNoRows = psInsertRecord.executeBatch();
			psInsertRecord.clearBatch();

			String selectAllStars = "select id, name from stars;";
			st = conn.createStatement();
			rs = st.executeQuery(selectAllStars);
			HashMap<String, String> starsTable = new HashMap<>();
			while(rs.next()) {
				String id = rs.getString("id");
				String name = rs.getString("name");
				starsTable.put(name, id);
			}
			
			/*we have a hashmap of all the stars 
			 * loop through the hashmap and insert into table
			 * 
			 * */
			psInsertRecord = conn.prepareStatement(sqlInsertSIM);
			StarsInMoviesParser SIMParser = new StarsInMoviesParser();
			SIMParser.runExample();
			HashSet<SIM> hs = SIMParser.mySIM;
			for(SIM s: hs) {
				String movieId = s.getID();
				String starId = starsTable.get(s.getStar());
				if(starId != null) { //linking star name to star id
					psInsertRecord.setString(1, starId);
					psInsertRecord.setString(2, movieId);
					psInsertRecord.addBatch();
				}
			}
			try {
				psInsertRecord.executeBatch();
			}catch(Exception e) {
				System.out.println("Tried to add a movie that was not in the database. Rejected");
			}
			System.out.println("Done executing batch for stars and stars_in_movies table");
//			finally, execute batch when all done
			
			
			conn.commit();

		} catch (SQLException e) {
			e.printStackTrace();
		}

		try {
			if (psInsertRecord != null)
				psInsertRecord.close();
			if (conn != null)
				conn.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	


}
