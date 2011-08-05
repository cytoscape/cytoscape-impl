/*
 * Copyright (c) 2006, 2007, The Cytoscape Consortium (www.cytoscape.org) The
 * Cytoscape Consortium is: - Institute for Systems Biology - University of
 * California San Diego - Memorial Sloan-Kettering Cancer Center - Institut
 * Pasteur - Agilent Technologies This library is free software; you can
 * redistribute it and/or modify it under the terms of the GNU Lesser General
 * Public License as published by the Free Software Foundation; either version
 * 2.1 of the License, or any later version. This library is distributed in the
 * hope that it will be useful, but WITHOUT ANY WARRANTY, WITHOUT EVEN THE
 * IMPLIED WARRANTY OF MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE. The
 * software and documentation provided hereunder is on an "as is" basis, and the
 * Institute for Systems Biology and the Whitehead Institute have no obligations
 * to provide maintenance, support, updates, enhancements or modifications. In
 * no event shall the Institute for Systems Biology and the Whitehead Institute
 * be liable to any party for direct, indirect, special, incidental or
 * consequential damages, including lost profits, arising out of the use of this
 * software and its documentation, even if the Institute for Systems Biology and
 * the Whitehead Institute have been advised of the possibility of such damage.
 * See the GNU Lesser General Public License for more details. You should have
 * received a copy of the GNU Lesser General Public License along with this
 * library; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place, Suite 330, Boston, MA 02111-1307 USA.
 */
package org.cytoscape.io.internal.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.util.jar.JarInputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipInputStream;

import org.cytoscape.io.util.StreamUtil;

/**
 * 
 */
public class StreamUtilImpl implements StreamUtil {

	private static final String GZIP = ".gz";

	private static final String ZIP = ".zip";

	private static final String JAR = ".jar";

	private static int msConnectionTimeout = 2000;
	/**
	 * 
	 */
	public static boolean STOP = false;

	/**
	 * Gets the an input stream given a URL.
	 * 
	 * @param source
	 *            URL source for a zip or jar file.
	 * @return InputStream for given URL
	 * @throws IOException
	 * 
	 */
	public InputStream getInputStream(URL source) throws IOException {
		final InputStream newIs;
		final InputStream proxyIs;
		proxyIs = getBasicInputStream(source);
		if (source.toString().toLowerCase().endsWith(GZIP)) {
			newIs = new GZIPInputStream(proxyIs);
		} else if (source.toString().toLowerCase().endsWith(ZIP)) {
			// System.err.println(source.toString() + " ZIP ");
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
	public InputStream getBasicInputStream(URL source) throws IOException {
		if (source == null) {
			throw new IllegalStateException(
					"getBasicInputStream was given a null 'source' argument.");
		}
		URLConnection uc = getURLConnection(source);
		return uc.getInputStream();
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
	public URLConnection getURLConnection(URL source) throws IOException {
		if (source == null) {
			throw new IllegalStateException(
					"getURLConnection was given a null 'source' argument.");
		}
		// TODO add proxy support back -- should be inserted as a service
		Proxy cytoProxy = null; // ProxyHandler.getProxyServer();
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

	// /**
	// * Download the file specified by the url string to the given File object
	// *
	// * @param urlString
	// * @param downloadFile
	// * @param taskMonitor
	// * @return
	// * @throws IOException
	// */
	// public static void download(String urlString, File downloadFile,
	// TaskMonitor taskMonitor) throws IOException {
	// URL url = new URL(urlString);
	// InputStream is = null;
	// int maxCount = 0; // -1 if unknown
	// int progressCount = 0;
	// URLConnection conn = getURLConnection(url);
	// maxCount = conn.getContentLength();
	// is = conn.getInputStream();
	// FileOutputStream os = new FileOutputStream(downloadFile);
	// double percent = 0.0d;
	// byte[] buffer = new byte[1];
	// while (((is.read(buffer)) != -1) && !STOP) {
	// progressCount += buffer.length;
	// // Report on Progress
	// if (taskMonitor != null) {
	// percent = ((double) progressCount / maxCount) * 100.0;
	// if (maxCount == -1) { // file size unknown
	// percent = -1;
	// }
	// JTask jTask = (JTask) taskMonitor;
	// if (jTask.haltRequested()) { // abort
	// downloadFile = null;
	// taskMonitor.setStatus("Canceling the download ...");
	// taskMonitor.setPercentCompleted(100);
	// break;
	// }
	// taskMonitor.setPercentCompleted((int) percent);
	// }
	// os.write(buffer);
	// }
	// os.flush();
	// os.close();
	// is.close();
	// if (STOP) {
	// downloadFile.delete();
	// }
	// }
	//
	// /**
	// * Get the the contents of the given URL as a string.
	// *
	// * @param source
	// * @return String
	// * @throws IOException
	// */
	// public static String download(URL source) throws IOException {
	// InputStream is = getInputStream(source);
	// StringBuffer buffer = new StringBuffer();
	// int c;
	// while ((c = is.read()) != -1) {
	// buffer.append((char) c);
	// }
	// is.close();
	// return buffer.toString();
	// }
}
