package br.pucrio.tecgraf.demo.utils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.StringTokenizer;
import java.util.Vector;

/**
 * Load resources (or images) from various sources.
 * 
 * @author Ceki G&uuml;lc&uuml;
 */

public class Loader {

	/**
	 * Log da classe
	 */
	// private static final Logger logger = Logger.getLogger(Loader.class);

	public static File getResourceAsFile(String name) throws IOException {
		SecurityException exception = null;

		File file;
		URL url;

		try {
			// Search using the CLASSPATH. If found, "file" is set and the call
			// returns true. A SecurityException might bubble up.

			file = tryClasspath(name);

			if (file != null) {

				return file;
			}
		} catch (SecurityException e) {
			exception = e; // Save for later.
		}

		try {
			// Search using the classloader getResource( ). If found as a file,
			// "file" is set; if found as a URL, "url" is set.

			url = tryLoader(name);

			if (url != null) {

				return new File(url.getPath());
			}
		} catch (SecurityException e) {
			exception = e; // Save for later.
		}

		// If you get here, something went wrong. Report the exception.
		String msg = "";
		if (exception != null) {
			msg = ": " + exception;
		}

		throw new IOException("Resource '" + name + "' could not be found in " + "the CLASSPATH ("
				+ System.getProperty("java.class.path")
				+ "), nor could it be located by the classloader responsible for the "
				+ "web application (WEB-INF/classes)" + msg, exception);
	}

	public static InputStream getResource(String name) throws IOException {
		SecurityException exception = null;

		File file;
		URL url;

		try {
			// Search using the CLASSPATH. If found, "file" is set and the call
			// returns true. A SecurityException might bubble up.

			file = tryClasspath(name);

			if (file != null) {

				return new BufferedInputStream(new FileInputStream(file));
			}
		} catch (SecurityException e) {
			exception = e; // Save for later.
		}

		try {
			// Search using the classloader getResource( ). If found as a file,
			// "file" is set; if found as a URL, "url" is set.

			url = tryLoader(name);

			if (url != null) {

				return new BufferedInputStream(url.openStream());
			}
		} catch (SecurityException e) {
			exception = e; // Save for later.
		}

		// If you get here, something went wrong. Report the exception.
		String msg = "";
		if (exception != null) {
			msg = ": " + exception;
		}

		throw new IOException("Resource '" + name + "' could not be found in " + "the CLASSPATH ("
				+ System.getProperty("java.class.path")
				+ "), nor could it be located by the classloader responsible for the "
				+ "web application (WEB-INF/classes)" + msg, exception);
	}

	public static String getResourcePath(String name) {

		File file;
		URL url;

		try {
			// Search using the CLASSPATH. If found, "file" is set and the call
			// returns true. A SecurityException might bubble up.

			file = tryClasspath(name);

			if (file != null) {

				return file.getPath();
			}
		} catch (SecurityException e) {
			// logger.warn("Falha usando CLASSPATH", e);
		}

		try {
			// Search using the classloader getResource( ). If found as a file,
			// "file" is set; if found as a URL, "url" is set.

			url = tryLoader(name);

			if (url != null) {

				return url.getPath();
			}
		} catch (SecurityException e) {
			// logger.warn("Falha usando o classloader getResource()", e);
		}

		return null;
	}

	// Returns true if found
	private static File tryClasspath(String filename) {
		String classpath = System.getProperty("java.class.path");
		String[] paths = split(classpath, File.pathSeparator);
		return searchDirectories(paths, filename);
	}

	private static File searchDirectories(String[] paths, String filename) {
		SecurityException exception = null;
		for (String element : paths) {
			try {
				File file = new File(element, filename);
				if (file.exists() && !file.isDirectory()) {
					return file;
				}
			} catch (SecurityException e) {
				// Security exceptions can usually be ignored, but if all attempts
				// to find the file fail, report the (last) security exception.
				exception = e;
			}
		}
		// Couldn't find any match
		if (exception != null) {
			throw exception;
		} else {
			return null;
		}
	}

	// Splits a String into pieces according to a delimiter.
	// Uses JDK 1.1 classes for backward compatibility.
	// JDK 1.4 actually has a split( ) method now.
	private static String[] split(String str, String delim) {
		// Use a Vector to hold the split strings.
		Vector v = new Vector();

		// Use a StringTokenizer to do the splitting.
		StringTokenizer tokenizer = new StringTokenizer(str, delim);
		while (tokenizer.hasMoreTokens()) {
			v.addElement(tokenizer.nextToken());
		}

		String[] ret = new String[v.size()];
		v.copyInto(ret);
		return ret;
	}

	// Returns true if found
	private static URL tryLoader(String name) {
		URL url = ClassLoader.getSystemResource("/" + name);
		/*
		 * if( url == null ) { url = System.class.getClassLoader().getResource(name); }
		 */
		if (url == null) {
			url = Loader.class.getClassLoader().getResource(name);
		}
		return url;
	}
}