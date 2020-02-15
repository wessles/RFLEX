package com.wessles.mercury.data;

/**
 * An interface for data-manipulation. This should be used for file reading and writing.
 *
 * @author wessles
 */
public interface Data {
	/**
	 * Open, and load data.
	 */
	public void open();

	/**
	 * Close, and save data.
	 */
	public void close();
}
