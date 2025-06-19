package common.models;

import java.io.Serializable;

public enum OrganizationType implements Serializable {
    PUBLIC,
    TRUST,
    PRIVATE_LIMITED_COMPANY,
    OPEN_JOINT_STOCK_COMPANY;

    public static String names() {
		StringBuilder nameList = new StringBuilder();
		for (var colorType : values()) {
			nameList.append(colorType.name()).append(", ");
		}
		return nameList.substring(0, nameList.length()-2);
	}
}