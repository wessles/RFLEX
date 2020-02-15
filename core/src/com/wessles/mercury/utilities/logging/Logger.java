package com.wessles.mercury.utilities.logging;

/**
 * A utility for shortening logging. This logger takes in objects to log, as opposed to a single string.
 *
 * @author wessles
 */
public class Logger {
	/**
	 * Whether or not information can be logged
	 */
	private static boolean logging = true;

	/**
	 * Defines whether or not information can be logged.
	 */
	public static void setLogging(boolean logging) {
		Logger.logging = logging;
	}

	/**
	 * Logs a message.
	 */
	public static void log(Object... object) {
		if (!logging)
			return;

		String message = "";

		for (Object obj : object)
			message += obj.toString() + " ";

		System.out.println(message + "");
	}

	/**
	 * Warns a message.
	 */
	public static void warn(Object... object) {
		if (!logging)
			return;

		String message = "";

		for (Object obj : object)
			message += obj.toString() + " ";

		System.err.println(message + "");
	}

	/**
	 * Prints a new line.
	 */
	public static void newLine() {
		System.out.println();
	}
}
