/*
  File: ReadUtils.java

  Copyright (c) 2006, The Cytoscape Consortium (www.cytoscape.org)

  The Cytoscape Consortium is:
  - Institute for Systems Biology
  - University of California San Diego
  - Memorial Sloan-Kettering Cancer Center
  - Institut Pasteur
  - Agilent Technologies

  This library is free software; you can redistribute it and/or modify it
  under the terms of the GNU Lesser General Public License as published
  by the Free Software Foundation; either version 2.1 of the License, or
  any later version.

  This library is distributed in the hope that it will be useful, but
  WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
  MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
  documentation provided hereunder is on an "as is" basis, and the
  Institute for Systems Biology and the Whitehead Institute
  have no obligations to provide maintenance, support,
  updates, enhancements or modifications.  In no event shall the
  Institute for Systems Biology and the Whitehead Institute
  be liable to any party for direct, indirect, special,
  incidental or consequential damages, including lost profits, arising
  out of the use of this software and its documentation, even if the
  Institute for Systems Biology and the Whitehead Institute
  have been advised of the possibility of such damage.  See
  the GNU Lesser General Public License for more details.

  You should have received a copy of the GNU Lesser General Public License
  along with this library; if not, write to the Free Software Foundation,
  Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 */
package org.cytoscape.io.internal.util;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

import org.cytoscape.io.util.StreamUtil;
import org.cytoscape.work.TaskMonitor;

/**
 */
public class ReadUtils {

	private static final String LINE_SEP = System.getProperty("line.separator");

	private StreamUtil streamUtil;

	public ReadUtils(StreamUtil streamUtil) {
		this.streamUtil = streamUtil;
	}

	/**
	 * A string that defines a simplified java regular expression for a URL.
	 * This may need to be updated to be more precise.
	 */
	public static final String urlPattern = "^(jar\\:)?(\\w+\\:\\/+\\S+)(\\!\\/\\S*)?$";

	/**
	 * DOCUMENT ME!
	 * 
	 * @param name
	 *            DOCUMENT ME!
	 * 
	 * @return DOCUMENT ME!
	 */
	public InputStream getInputStream(String name) {
		return getInputStream(name, null);
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @param name
	 *            DOCUMENT ME!
	 * @param monitor
	 *            DOCUMENT ME!
	 * 
	 * @return DOCUMENT ME!
	 */
	public InputStream getInputStream(String name, TaskMonitor monitor) {
		InputStream in = null;

		try {
			if (name.matches(urlPattern)) {
				URL u = new URL(name);
				// in = u.openStream();
				// Use URLUtil to get the InputStream since we might be using a
				// proxy server
				// and because pages may be cached:
				in = streamUtil.getBasicInputStream(u);
			} else
				in = new FileInputStream(name);
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}

		return in;
	}

	/**
	 * 
	 * @param filename
	 *            File to read in
	 * 
	 * @return The contents of the given file as a string.
	 */
	public String getInputString(String filename) {
		try {
			InputStream stream = getInputStream(filename);
			return getInputString(stream);
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}

		System.out.println("couldn't create string from '" + filename + "'");

		return null;
	}

	/**
	 * 
	 * @param inputStream
	 *            An InputStream
	 * 
	 * @return The contents of the given file as a string.
	 */
	public String getInputString(InputStream inputStream) throws IOException {

		final StringBuilder sb = new StringBuilder();
		String line;
		final BufferedReader br = new BufferedReader(new InputStreamReader(
				inputStream));

		while ((line = br.readLine()) != null)
			sb.append(line + LINE_SEP);

		br.close();

		return sb.toString();
	}
}
