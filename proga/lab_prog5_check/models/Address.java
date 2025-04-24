package models;

import java.util.Objects;

public class Address {
	private String street; //Поле не может быть null
	private Location town; //Поле может быть null

	public Address(String street, Location town) {
		this.street = street;
		this.town = town;
	}

	public String getStreet() {
		return street;
	}

	public void setStreet(String street) {
		this.street = street;
	}

	public Location getTown() {
		return town;
	}

	public void setTown(Location town) {
		this.town = town;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Address address = (Address) o;
		return Objects.equals(street, address.street) && Objects.equals(town, address.town);
	}

	@Override
	public int hashCode() {
		return Objects.hash(street, town);
	}

	@Override
	public String toString() {
		return street + ";" + town;
	}
}
