package io.lqd.sdk.model;

import java.io.Serializable;

public abstract class LQModel implements Serializable {

	private static final long serialVersionUID = 1L;

	public static boolean hasInvalidChars(String key) {
		return key.contains("$") || key.contains(".") || key.contains("\0");
	}

}
