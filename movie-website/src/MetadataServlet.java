import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;

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
 * Servlet implementation class MetadataServlet
 */
@WebServlet(name="MetadataServlet", urlPatterns = "/api/metadata")
public class MetadataServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	@Resource(name = "jdbc/moviedb")
    private DataSource dataSource;
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		PrintWriter out = response.getWriter();
		try {
			Context initCtx = new InitialContext();
			Context envCtx = (Context) initCtx.lookup("java:comp/env");
			DataSource ds = (DataSource) envCtx.lookup("jdbc/movie-pool");
			Connection dbcon = ds.getConnection();
			System.out.println(dbcon + " from metadata");

			JsonArray jsonArray = new JsonArray();
			String query = "";
			PreparedStatement statement = null;
			ResultSet rs = null;
			ArrayList<String> tables = new ArrayList<String>();
			
			query = "SHOW TABLES";
			statement = dbcon.prepareStatement(query);			
			rs = statement.executeQuery();

			while(rs.next()) {
				tables.add(rs.getString("Tables_in_moviedb"));
			}
			
			System.out.println(tables.toString());
			
			for( int i=0; i<tables.size(); ++i) {
				query = "SHOW COLUMNS FROM " + tables.get(i);
				statement = dbcon.prepareStatement(query);			
				rs = statement.executeQuery();
				
				JsonArray fields = new JsonArray();
				JsonObject jsonObject = new JsonObject();

				jsonObject.addProperty("table", tables.get(i));
				
				while ( rs.next()) {
					JsonObject field_json = new JsonObject();
					String field = rs.getString("Field");
					String type = rs.getString("Type");
					field_json.addProperty("field", field);
					field_json.addProperty("type", type);
					fields.add(field_json);
				}
				
				jsonObject.add("fields", fields);
				jsonArray.add(jsonObject);
				
			}
			
			System.out.println(jsonArray.toString());
			
			out.write(jsonArray.toString());
			response.setStatus(200);

			rs.close();
			statement.close();
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

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
	}

}
