package com.wessles.mercury.utilities.misc;

/**
 * An interface used primarily (but not only) for graphical objects that are used between begin() / end() methods.
 *
 * @author wessles
 */
public interface Usable {
	/**
	 * Begins the use of the object.
	 */
	public void begin();

	/**
	 * Ends use of the object.
	 */
	public void end();
}
