
public class Star {
	private String name;
	private String dob;
	
	@Override
	public int hashCode() {
		return (name+dob).hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Star other = (Star) obj;
		if (dob == null) {
			if (other.dob != null)
				return false;
		} else if (!dob.equals(other.dob))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}

	public Star(String name, String dob) {
		this.name = name;
		this.dob = dob;
	}
	
	public String getName(){
		return this.name;
	}
	
	public String getDOB() {
		return this.dob;
	}
	
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("Star Details - ");
		sb.append("Name:" + getName());
		sb.append(", ");
		sb.append("DOB: " + getDOB());
		return sb.toString();
	}
}
