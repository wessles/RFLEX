package com.wessles.mercury.data;

import java.io.*;
import java.util.*;

/**
 * A way to store information outside of local data and store data in files. Uses a system of properties and values.
 *
 * @author wessles
 */
public class MercuryData implements Data {
	/**
	 * Location of the data file.
	 */
	public File location;

	/**
	 * The values of the data file. This is temporary in-code storage.
	 */
	public HashMap<String, String> values = new HashMap<String, String>();

	/**
	 * @param file
	 * 		The file.
	 */
	public MercuryData(File file) {
		location = file;
	}

	/**
	 * @param property
	 * 		The property you are modifying (case sensitive).
	 * @param value
	 * 		The value that you are changing the property to.
	 */
	public void setProperty(String property, String value) {
		values.put(property, value);
	}

	/**
	 * @param prop
	 * 		The property you want to see the value of.
	 *
	 * @return the property's value. If it does not exist, you get null.
	 */
	public String getProperty(String prop) {
		return values.get(prop);
	}

	@Override
	public void open() {
		Scanner scanner;

		try {
			scanner = new Scanner(location);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return;
		}

		while (scanner.hasNextLine()) {
			String line = scanner.nextLine();
			String[] split = line.split(" ", 2);

			if (split.length <= 1)
				try {
					throw new IOException("Corrupted data file.");
				} catch (IOException e) {
					e.printStackTrace();
				}

			String property = split[0];
			String value = split[1];

			values.put(property, value);
		}

		scanner.close();
	}

	@Override
	public void close() {
		PrintWriter write;

		try {
			write = new PrintWriter(location);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return;
		}

		for (int i = 0; i < values.size(); i++) {
			String property = (String) values.keySet().toArray()[i];
			String value = (String) values.values().toArray()[i];

			write.println(property + " " + value);
		}

		write.close();
		values.clear();
	}
}
