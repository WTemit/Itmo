package common.models;

import java.io.Serializable;
import java.util.Objects;

public class Organization implements Serializable {
	private static final long serialVersionUID = 3L;

	private Integer annualTurnover; //Поле может быть null, Значение поля должно быть больше 0
	private OrganizationType type; //Поле может быть null
	private Address officialAddress; //Поле может быть null

	public Organization(Integer annualTurnover, OrganizationType type, Address officialAddress) {
		this.annualTurnover = annualTurnover;
		this.type = type;
		this.officialAddress = officialAddress;
	}

	public Organization() {
		// Empty constructor for XML parsing
	}



	public boolean validate() {
		if (annualTurnover != null && annualTurnover <= 0) return false;
		return true;
	}

	public Integer getAnnualTurnover() {
		return annualTurnover;
	}

	public void setAnnualTurnover(Integer annualTurnover) {
		this.annualTurnover = annualTurnover;
	}

	public OrganizationType getType() {
		return type;
	}

	public void setType(OrganizationType type) {
		this.type = type;
	}

	public Address getOfficialAddress() {
		return officialAddress;
	}

	public void setOfficialAddress(Address officialAddress) {
		this.officialAddress = officialAddress;
	}


	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Organization organization = (Organization) o;
		return Objects.equals(annualTurnover, organization.annualTurnover)
				&& Objects.equals(type, organization.type)
				&& Objects.equals(officialAddress, organization.officialAddress);
	}

	@Override
	public int hashCode() {
		return Objects.hash(annualTurnover, type, officialAddress);
	}

	@Override
	public String toString() {
		return annualTurnover + ";" + type + ";" + officialAddress;
	}

	public void setOrganization(double v) {
		this.annualTurnover = (int) v;
	}
}