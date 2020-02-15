package com.wessles.mercury.utilities.misc;

/**
 * An abstraction for objects that can be rendered.
 *
 * @author wessles
 */
public interface Renderable {
	/**
	 * The render method. In here there should be peripheral activity, such as graphics, or sound, given g.
	 */
	void render();
}
