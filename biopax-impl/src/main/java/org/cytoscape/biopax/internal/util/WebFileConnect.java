// $Id: WebFileConnect.java,v 1.2 2006/06/15 22:06:02 grossb Exp $
//------------------------------------------------------------------------------
/** Copyright (c) 2006 Memorial Sloan-Kettering Cancer Center.
 **
 ** Code written by: Ethan Cerami
 ** Authors: Ethan Cerami, Gary Bader, Chris Sander
 **
 ** This library is free software; you can redistribute it and/or modify it
 ** under the terms of the GNU Lesser General Public License as published
 ** by the Free Software Foundation; either version 2.1 of the License, or
 ** any later version.
 **
 ** This library is distributed in the hope that it will be useful, but
 ** WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
 ** MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
 ** documentation provided hereunder is on an "as is" basis, and
 ** Memorial Sloan-Kettering Cancer Center
 ** has no obligations to provide maintenance, support,
 ** updates, enhancements or modifications.  In no event shall
 ** Memorial Sloan-Kettering Cancer Center
 ** be liable to any party for direct, indirect, special,
 ** incidental or consequential damages, including lost profits, arising
 ** out of the use of this software and its documentation, even if
 ** Memorial Sloan-Kettering Cancer Center
 ** has been advised of the possibility of such damage.  See
 ** the GNU Lesser General Public License for more details.
 **
 ** You should have received a copy of the GNU Lesser General Public License
 ** along with this library; if not, write to the Free Software Foundation,
 ** Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 **/
package org.cytoscape.biopax.internal.util;

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
