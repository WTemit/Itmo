package common.models;

import java.io.Serializable;
import java.util.Objects;

public class Location implements Serializable{
	private static final long serialVersionUID = 5L;

	private Double x; //Поле не может быть null
	private Integer y; //Поле не может быть null
	private String nameT; //Поле не может быть null

	public Location(Double x, Integer y, String name) {
		this.x = x;
		this.y = y;
		this.nameT = name;
	}

	public Double getX() {
		return x;
	}

	public void setX(Double x) {
		this.x = x;
	}

	public Integer getY() {
		return y;
	}

	public void setY(Integer y) {
		this.y = y;
	}

	public String getName() {
		return nameT;
	}

	public void setName(String name) {
		this.nameT = name;
	}

	public boolean validate() {
		return nameT != null && !nameT.isEmpty() && x != null && y != null;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Location locate = (Location) o;
		return Objects.equals(x, locate.x) && Objects.equals(y, locate.y)
				&& Objects.equals(nameT, locate.nameT);
	}

	@Override
	public int hashCode() {
		return Objects.hash(x, y, nameT);
	}

	@Override
	public String toString() {
		return x + ";" + y + ";" + nameT;
	}
}