package org.cytoscape.tableimport.internal.util;

/*
 * #%L
 * Cytoscape Table Import Impl (table-import-impl)
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

//import cytoscape.task.TaskMonitor;

//import cytoscape.task.ui.JTask;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.util.jar.JarInputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipInputStream;

/**
 * 
 */
public class URLUtil {
	
	private static final String GZIP = ".gz";
	private static final String ZIP = ".zip";
	private static final String JAR = ".jar";

	private static int msConnectionTimeout = 2000;

	private URLUtil() {
	}

	/**
	 * Gets the an input stream given a URL.
	 * 
	 * @param source
	 *            URL source for a zip or jar file.
	 * @return InputStream for given URL
	 * @throws IOException
	 *             
	 */
	public static InputStream getInputStream(URL source) throws IOException {
		final InputStream newIs;
		final InputStream proxyIs;
		proxyIs = getBasicInputStream(source);
		if (source.toString().toLowerCase().endsWith(GZIP)) {
			newIs = new GZIPInputStream(proxyIs);
		} else if (source.toString().toLowerCase().endsWith(ZIP)) {
			// CyLogger.getLogger().warn(source.toString() + " ZIP ");
			newIs = new ZipInputStream(proxyIs);
		} else if (source.toString().toLowerCase().endsWith(JAR)) {
			newIs = new JarInputStream(proxyIs);
		} else {
			newIs = proxyIs;
		}
		return newIs;
	}

	/**
	 * Obtain an InputStream for a given URL. Ensure proxy servers and an input
	 * stream to the real URL source is created--not a locally cached and out of
	 * date source. Proxy servers and other characteristics can cause pages to
	 * be cached.
	 * 
	 * @param source
	 *            the non-null URL from which to obtain an InputStream.
	 * @return InputStream from the source URL.
	 * @throws IllegalStateException
	 *             if source is null.
	 * @throws IOException
	 *             if a connection to the URL can't be opened or a problem
	 *             occurs with the InputStream.
	 */
	public static InputStream getBasicInputStream(URL source)
			throws IOException {
		if (source == null) {
			throw new IllegalStateException(
					"getBasicInputStream was given a null 'source' argument.");
		}
		URLConnection uc = getURLConnection(source);

		final InputStream is;
		try {
			is = uc.getInputStream();
		} catch (final Exception e) {
			throw new IllegalStateException("Failed to get input stream for \"" + source + "\".");
		}
		return is;
	}

	/**
	 * Obtain a URLConnection for a given URL. Ensure proxy servers are
	 * considered and page caching is ignored.
	 * 
	 * @param source
	 *            the non-null URL from which to obtain a URLConnection.
	 * @return URLConnection to the source URL.
	 * @throws IllegalStateException
	 *             if source is null.
	 * @throws IOException
	 *             if a connection to the URL can't be opened.
	 */
	public static URLConnection getURLConnection(URL source) throws IOException {
		if (source == null) {
			throw new IllegalStateException(
					"getURLConnection was given a null 'source' argument.");
		}
		Proxy cytoProxy = ProxyHandler.getProxyServer();
		URLConnection uc = null;
		if (cytoProxy == null) {
			uc = source.openConnection();
		} else {
			try {
				uc = source.openConnection(cytoProxy);
			} catch (UnsupportedOperationException e) {
				// This happens when we have a URL whose
				// protocol handler doesn't take a proxy
				// argument (such as a URL referring to inside
				// a jar file). In this case, just use the
				// normal way to open a connection:
				uc = source.openConnection();
			}
		}
		uc.setUseCaches(false); // don't use a cached page
		uc.setConnectTimeout(msConnectionTimeout); // set timeout for connection
		return uc;
	}

	/**
	 * Download the file specified by the url string to the given File object
	 * 
	 * @param urlString
	 * @param downloadFile
	 * @param taskMonitor
	 * @return
	 * @throws IOException
	 */
	//public static void download(String urlString, File downloadFile,
	//		TaskMonitor taskMonitor) throws IOException

	public static void download(String urlString, File downloadFile) throws IOException
	{
		/*
		boolean stop = false;

		try {
			URL url = new URL(urlString);
			InputStream is = null;

			try {
				int maxCount = 0; // -1 if unknown
				int progressCount = 0;
				URLConnection conn = getURLConnection(url);
				maxCount = conn.getContentLength();
				is = conn.getInputStream();
				FileOutputStream os = null;
				try {
					os = new FileOutputStream(downloadFile);
					double percent = 0.0d;
					byte[] buffer = new byte[1];
					while (((is.read(buffer)) != -1) && !stop) {
						progressCount += buffer.length;
						// Report on Progress
						if (taskMonitor != null) {
							percent = ((double) progressCount / maxCount) * 100.0;
							if (maxCount == -1) { // file size unknown
								percent = -1;
							}
							JTask jTask = (JTask) taskMonitor;
							if (jTask.haltRequested()) { // abort
								stop = true;
								taskMonitor.setStatus("Canceling the download ...");
								taskMonitor.setPercentCompleted(100);
								break;
							}
							taskMonitor.setPercentCompleted((int) percent);
						}
						os.write(buffer);
					}
					os.flush();
				}
				finally {
					if (os != null) {
						os.close();
					}
				}
			} finally {
				if (is != null)
					is.close();
			}
		} finally {
			if (stop)
				downloadFile.delete();
		}
		*/
	}

	/**
	 * Get the the contents of the given URL as a string.
	 * 
	 * @param source
	 * @return String
	 * @throws IOException
	 */
	public static String download(URL source) throws IOException {
		InputStream is = null;
		StringBuffer buffer = new StringBuffer();
		int c;

		try {
			is = getInputStream(source);
			while ((c = is.read()) != -1) {
				buffer.append((char) c);
			}
			is.close();
		}
		finally {
			if (is != null)
				is.close();
		}

		return buffer.toString();
	}

	public static boolean isValid(final String urlCandidate) {
		try {
			new URL(urlCandidate);	
			return true;
		} catch (final MalformedURLException e) {
			return false;
		}
	}
}
