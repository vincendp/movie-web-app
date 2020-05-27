
public class SIM {
	private String movieName;
	private String movieId;
	private String starName;
	
	public SIM(String id, String m, String s) {
		this.movieId = id;
		this.movieName = m;
		this.starName = s;
	}
	
	@Override
	public int hashCode() {
		return (this.movieId+this.movieName+this.starName).hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this.toString() == obj.toString())
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SIM other = (SIM) obj;
		if (movieName == null) {
			if (other.movieName != null)
				return false;
		} else if (!movieName.equals(other.movieName))
			return false;
		if (starName == null) {
			if (other.starName != null)
				return false;
		} else if (!starName.equals(other.starName))
			return false;
		return true;
	}

	public void setMovieName(String movieName) {
		this.movieName = movieName;
	}

	public void setStarName(String starName) {
		this.starName = starName;
	}

	public String getID() {
		return this.movieId;
	}
	
	public String getMovie() {
		return this.movieName;
	}
	
	public String getStar() {
		return this.starName;
	}
	
	@Override
	public String toString() {
		return "ID: "+this.movieId+"\t Movie: "+this.movieName +"\t\t Star:"+ this.starName;
	}
}
