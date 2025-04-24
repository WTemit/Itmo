package models;

import utility.Validatable;

public class Coordinates implements Validatable {
	private Long x; //Значение поля должно быть больше -472, Поле не может быть null
	private Integer y; //Поле не может быть null

	public Coordinates(Long x, Integer y) {
		this.x = x;
		this.y = y;
	}

	public Coordinates(String s) {
		try {
			String[] parts = s.split(";");
			if (parts.length >= 1) {
				try {
					this.x = Long.parseLong(parts[0]);
				} catch (NumberFormatException e) {
					this.x = null;
				}
			}
			if (parts.length >= 2) {
				try {
					this.y = Integer.parseInt(parts[1]);
				} catch (NumberFormatException e) {
					this.y = null;
				}
			}
		} catch (Exception e) {
			this.x = null;
			this.y = null;
		}
	}

	/**
	 * Валидирует правильность полей.
	 * @return true, если все верно, иначе false
	 */
	@Override
	public boolean validate() {
		if (x == null || y == null) return false;
		if (x <= -472) return false;
		return true;
	}

	public Long getX() {
		return x;
	}

	public Integer getY() {
		return y;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null || getClass() != obj.getClass()) return false;
		Coordinates that = (Coordinates) obj;
		return x.equals(that.x) && y.equals(that.y);
	}

	@Override
	public int hashCode() {
		return x.hashCode() + y.hashCode();
	}

	@Override
	public String toString() {
		return x + ";" + y;
	}
}