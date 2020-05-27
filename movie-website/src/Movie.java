import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

public class Movie {
	private String id;
	private String title;
	private String director;
	private int year;
	private HashSet<String> genres;
	
	@Override
	public int hashCode() {
		return this.id.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if(this.hashCode() == obj.hashCode()) {
			return true;
		}
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Movie other = (Movie) obj;
		if (director == null) {
			if (other.director != null)
				return false;
		} else if (!director.equals(other.director))
			return false;
		if (genres == null) {
			if (other.genres != null)
				return false;
		} else if (!genres.equals(other.genres))
			return false;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (title == null) {
			if (other.title != null)
				return false;
		} else if (!title.equals(other.title))
			return false;
		if (year != other.year)
			return false;
		return true;
	}



	public Movie(String id, String title, String director, int year) {
		this.id = id;
		this.title = title;
		this.director = director;
		this.year = year;
		genres = new HashSet<String>();
	}
	
	public String getId() {
		return id;
	}
	
	public String getTitle() { 
		return title;
	}
	
	public String getDirector() {
		return director;
	}
	
	public int getYear() {
		return year;
	}
	
	public void insertGenre(String genre) {
		genres.add(genre);
	}
	
	public HashSet<String> getGenres() {
		return genres;
	}
	
	public boolean genresIsEmpty() {
		return genres.isEmpty();
	}
	
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("Movie Info - ");
		sb.append("id:" + getId());
		sb.append(", ");
		sb.append("Title:" + getTitle());
		sb.append(", ");
		sb.append("Director:" + getDirector());
		sb.append(", ");
		sb.append("Year:" + getYear());
		sb.append(", ");
		sb.append("Genres: ");
		Iterator<String> it = genres.iterator();
	    	while (it.hasNext()) {
	    		sb.append(it.next() + " ");
	    	}
		sb.append(".");
		
		return sb.toString();
	}
}
