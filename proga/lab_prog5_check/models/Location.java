package models;

import java.util.Objects;

public class Location {
	private Double x; //Поле не может быть null
	private Integer y; //Поле не может быть null
	private String name; //Поле не может быть null

	public Location(Double x, Integer y, String name) {
		this.x = x;
		this.y = y;
		this.name = name;
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
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean validate() {
		return name != null && !name.isEmpty() && x != null && y != null;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Location locate = (Location) o;
		return Objects.equals(x, locate.x) && Objects.equals(y, locate.y)
				&& Objects.equals(name, locate.name);
	}

	@Override
	public int hashCode() {
		return Objects.hash(x, y, name);
	}

	@Override
	public String toString() {
		return x + ";" + y + ";" + name;
	}
}