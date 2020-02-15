package com.wessles.mercury.utilities.misc;

/**
 * An abstraction for objects that can be cleaned up.
 */
public interface Cleanable {
	/**
	 * The method for cleaning up.
	 */
	public void cleanup();
}
