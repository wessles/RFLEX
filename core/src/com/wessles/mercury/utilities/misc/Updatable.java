package com.wessles.mercury.utilities.misc;

/**
 * An abstraction for objects that can be updated.
 *
 * @author wessles
 */
public interface Updatable {
	/**
	 * The method for updating. In here, logic should occur.
	 */
	public void update(double delta);
}
