package com.wessles.mercury.utilities;

/**
 * A utility for simplifying numbers down to -1, 0, and 1.
 *
 * @author wessles
 */
public class Negpos {
	public static float negpos(float value) {
		if (value == 0)
			return 0;
		return value / Math.abs(value);
	}
}
