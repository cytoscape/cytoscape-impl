package org.cytoscape.biopax.internal.util;

/*
 * #%L
 * Cytoscape BioPAX Impl (biopax-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2013
 *   Memorial Sloan-Kettering Cancer Center
 *   The Cytoscape Consortium
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as 
 * published by the Free Software Foundation, either version 2.1 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */

import java.io.*;

import java.net.MalformedURLException;
import java.net.URL;


/**
 * Web/File Connect Utility Class.
 *
 * @author Ethan Cerami.
 */
public class WebFileConnect {
	/**
	 * Retrieves the Document from the Specified URL.
	 *
	 * @param urlStr URL String.
	 * @return String Object containing the full Document Content.
	 * @throws MalformedURLException URL is Malformed.
	 * @throws IOException           Network Error.
	 */
	public static String retrieveDocument(String urlStr) throws MalformedURLException, IOException {
		URL url = new URL(urlStr);
		BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));

		return readFile(in);
	}

	/**
	 * Retrieves the Document from the Specified File.
	 *
	 * @param file File Object.
	 * @return String Object containing the full Document Content.
	 * @throws FileNotFoundException File Not Found.
	 * @throws IOException           Read Error.
	 */
	public static String retrieveDocument(File file) throws FileNotFoundException, IOException {
		BufferedReader in = new BufferedReader(new FileReader(file));

		return readFile(in);
	}

	/**
	 * Reads a Document from a Buffered Reader.
	 */
	private static String readFile(BufferedReader in) throws IOException {
		StringBuffer buf = new StringBuffer();
		String str;

		while ((str = in.readLine()) != null) {
			buf.append(str + "\n");
		}

		in.close();

		return buf.toString();
	}
}
