package com.wessles.mercury.utilities;

import java.util.*;

/**
 * An array list that you can wipe wipeables with.
 *
 * @author wessles
 */
public class WipingArrayList<T extends Wipeable> extends ArrayList<T> {
	private static final long serialVersionUID = 1L;

	/**
	 * Goes through list, removing any objects that have declared themselves 'wiped.'
	 */
	public void sweep() {
		for (Iterator<?> i = iterator(); i.hasNext(); ) {
			Wipeable w = (Wipeable) i.next();
			if (w.wiped())
				i.remove();
		}
	}
}
