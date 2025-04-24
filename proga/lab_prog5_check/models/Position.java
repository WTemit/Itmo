package models;


public enum Position {
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