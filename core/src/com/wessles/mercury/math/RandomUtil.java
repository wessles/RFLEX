package com.wessles.mercury.math;

/**
 * A utilities class for pseudo-randomness.
 *
 * @author wessles
 */
public class RandomUtil {
	/**
	 * Base method for random number methods.
	 *
	 * @return a random double value between minimum and maximum.
	 */
	public static double random(double minimum, double maximum) {
		return minimum + (Math.random() * (maximum - minimum + 1));
	}

	/**
	 * Base method for random number methods.
	 *
	 * @return a random double value between minimum and maximum.
	 */
	public static float random(float minimum, float maximum) {
		return minimum + (float) (Math.random() * (maximum - minimum + 1));
	}

	/**
	 * @return a random boolean. 50-50 chance of true or false.
	 */
	public static boolean nextBoolean() {
		return (int) random(0, 20) % 2 == 0;
	}

	/**
	 * @param percent
	 * 		Percent chance of true
	 *
	 * @return a boolean that has a percent chance of being true.
	 */
	public static boolean chance(int percent) {
		if (percent > 100)
			percent %= 100;

		return random(0, 100) < percent;
	}

	/**
	 * @param percent
	 * 		Percent chance of true
	 *
	 * @return a boolean that has a percent chance of being true.
	 */
	public static boolean chance(float percent) {
		if (percent > 1)
			percent %= 1;

		return random(0, 1000) < percent * 1000;
	}

	/**
	 * @return an integer value between Integer.MIN_VALUE and Integer.MAX_VALUE.
	 */
	public static int nextInt() {
		return (int) random(Integer.MIN_VALUE, Integer.MAX_VALUE);
	}

	/**
	 * @return either 1 or -1.
	 */
	public static int negpos() {
		return nextBoolean() ? 1 : -1;
	}

	/**
	 * @return a floating point value between 0.0 and 1.0.
	 */
	public static float nextFloat() {
		return random(0, 100) / 100;
	}

	/**
	 * @return a double value between Double.MIN_VALUE and Double.MAX_VALUE.
	 */
	public static double nextDouble() {
		return random(Double.MIN_VALUE, Double.MAX_VALUE);
	}
}
