package org.cytoscape.psi_mi.internal.util;

/*
 * #%L
 * Cytoscape PSI-MI Impl (psi-mi-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2013 The Cytoscape Consortium
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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PushbackInputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;


/**
 * Retrieves Content from Local File System, Web Site, or FTP Site.
 *
 * @author Ethan Cerami
 */
public class ContentReader {
	private static final String HTTP = "http";
	private static final String FTP = "ftp";

	/**
	 * Retrieves Content from Local System, Web Site or FTP site.
	 *
	 * @param urlStr URL String.
	 * @return File contents.
	 * @throws DataServiceException Error Retrieving file.
	 */
	public String retrieveContent(String urlStr) throws DataServiceException {
		String content = null;
		URL url = null;

		try {
			if (urlStr.startsWith(HTTP) || urlStr.startsWith(FTP)) {
				url = new URL(urlStr);

				if (url.getProtocol().equalsIgnoreCase(HTTP)) {
					content = retrieveContentFromWeb(url);
				}

				if (url.getProtocol().equalsIgnoreCase(FTP)) {
					content = retrieveContentFromFtp(url);
				}
			} else {
				File file = new File(urlStr);
				InputStreamReader reader = new InputStreamReader(new FileInputStream(file));
				content = this.retrieveContentFromFile(reader);
			}
		} catch (MalformedURLException e) {
			throw new DataServiceException(e, "URL is malformed:  " + url + ".  Please try again.");
		} catch (UnknownHostException e) {
			String msg = "Network error occurred while trying to "
			             + "make network connection.  Could not find server:  " + e.getMessage()
			             + ". Please check your server and network settings, " + "and try again.";
			throw new DataServiceException(e, msg);
		} catch (IOException e) {
			throw new DataServiceException(e,
			                               "Error occurred "
			                               + " while trying to retrieve data from:  " + urlStr);
		}

		return content;
	}

	/**
	 * Retrieves Content from Web.
	 *
	 * @param url URL String.
	 * @return File contents.
	 */
	private String retrieveContentFromWeb(URL url) throws IOException {
		InputStreamReader isr = new InputStreamReader(url.openStream());
		final int bufSize = 65536;
		char[] buf = new char[bufSize];
		String content = "";
		int charsread = 0;

		while (true) {
			charsread = isr.read(buf, 0, bufSize);

			if (charsread == -1) {
				break;
			}

			String bufstring = new String(buf, 0, charsread);
			content += bufstring;
		}

		return content;
	}

	/**
	 * Retrieves Content from FTP site.
	 *
	 * @param url URL String.
	 * @return File contents.
	 * @throws IOException Error Retrieving file.
	 */
	private String retrieveContentFromFtp(URL url) throws IOException {
		InputStreamReader isr = new InputStreamReader(url.openStream());
		final int bufSize = 65536;
		char[] buf = new char[bufSize];
		String content = "";
		int charsread = 0;

		while (true) {
			charsread = isr.read(buf, 0, bufSize);

			if (charsread == -1) {
				break;
			}

			String bufstring = new String(buf, 0, charsread);
			content += bufstring;
		}

		return content;
	}

	/**
	 * Retrieves Content from local File System.
	 *
	 * @param reader Reader Object
	 * @return File contents.
	 * @throws IOException Error Retrieving file.  //File file
	 */
	private String retrieveContentFromFile(InputStreamReader reader) throws IOException {
		final int bufSize = 65536;
		char[] buf = new char[bufSize];
		StringBuffer content = new StringBuffer("");
		int charsread = 0;

        int totalcharsread = 0;
        while (true) {
			charsread = reader.read(buf, 0, bufSize);
            totalcharsread += charsread;

            if (charsread == -1) {
				break;
			}

			String bufstring = new String(buf, 0, charsread);
			content.append(bufstring);

        }

		return content.toString();
	}

	/**
	 * This method will try to determine whether this stream is connected to a zip file,
	 * a gzipped file, a possible text file or a possible binary file, in that order.
	 * It will return an appropriate Reader for the identified filetype,
	 * or 'null' if the type was not recognizable (ie: we could not read the four
	 * bytes constituting the magic number).
	 *
	 * @param aStream InputStream to determine the nature of
	 * @return Reader   with a Reader into the converted stream.
	 */
	public static InputStreamReader determineFileType(InputStream aStream) {
		InputStreamReader result = null;

		try {
			// Reading the first four bytes.
			byte[] firstFour = new byte[4];
			PushbackInputStream pbi = new PushbackInputStream(aStream, 4);
			int count = pbi.read(firstFour, 0, 4);

			// If we couldn't even read 4 bytes, it cannot be good. In that case
			// we 'll therefore return 'null'.
			if (count == 4) {
				/*System.out.println( "Read magic numbers (first four bytes: " +
				              firstFour[ 0 ] + " " +
				              firstFour[ 1 ] + " " +
				              firstFour[ 2 ] + " " +
				              firstFour[ 3 ] + " " + ")." ); */

				// Now unread our bytes.
				pbi.unread(firstFour);

				// Okay, let's check these magic numbers, shall we?
				if ((firstFour[0] == (byte) 0x1F) && (firstFour[1] == (byte) 0x8b)) {
					// GZIP!
					//System.out.println( "Detected GZIP format." );
					result = new InputStreamReader(new GZIPInputStream(pbi));
				} else if ((firstFour[0] == (byte) 0x50) && (firstFour[1] == (byte) 0x4b)
				           && (firstFour[2] == (byte) 0x03) && (firstFour[3] == (byte) 0x04)) {
					// (pk)ZIP!
					//System.out.println( "Detected ZIP format." );
					ZipInputStream zis = new ZipInputStream(pbi);

					ZipEntry ze = zis.getNextEntry();

					// Extra check: ze cannot be 'null'!
					if (ze != null) {
						result = new InputStreamReader(zis);
					}
				}

				// If we are here and result is still 'null', we weren't able to identify it
				// as either GZIP or ZIP.
				// So create a regular reader and go from here.
				if (result == null) {
					System.out.println("Defaulted to standard Reader.");
					result = new InputStreamReader(pbi);
				}
			}
		} catch (Exception ioe) {
			System.out.println("IOException while attempting to determine filetype." + ioe);
		}

		return result;
	}
}
