package common.util;

import common.interfaces.Validatable;

public abstract class Element implements Comparable<Element>, Validatable {
	abstract public Long getId();
}