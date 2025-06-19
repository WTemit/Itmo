package common.models;


import java.io.Serializable;

public enum Position implements Serializable {
    LABORER,
    ENGINEER,
    HEAD_OF_DEPARTMENT,
    CLEANER;

    public static String names() {
		StringBuilder nameList = new StringBuilder();
		for (var colorType : values()) {
			nameList.append(colorType.name()).append(", ");
		}
		return nameList.substring(0, nameList.length()-2);
	}
}