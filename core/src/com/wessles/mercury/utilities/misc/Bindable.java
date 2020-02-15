package com.wessles.mercury.utilities.misc;

/**
 * An interface for OpenGL-related graphical objects that can be bound and released.
 *
 * @author wessles
 */
public interface Bindable {
	/**
	 * Binds the object.
	 */
	public void bind();

	/**
	 * Releases the object.
	 */
	public void release();
}
