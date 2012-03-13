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
import java.io.FileInputStream;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Proxy.Type;
import java.net.URL;
import java.net.URLConnection;
import java.util.Properties;
import java.util.jar.JarInputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipInputStream;

import org.cytoscape.io.util.StreamUtil;
import org.cytoscape.property.CyProperty;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 */
public class StreamUtilImpl implements StreamUtil {

	private static final Logger logger = LoggerFactory.getLogger(StreamUtilImpl.class);
	private static final String GZIP = ".gz";
	private static final String ZIP = ".zip";
	private static final String JAR = ".jar";

	private static final int msConnectionTimeout = 2000;
	private Properties properties;
	
	public StreamUtilImpl(CyProperty<Properties> proxyProperties) {
		properties = proxyProperties.getProperties();
	}

	@Override
	public InputStream getInputStream(String name) throws IOException {
		if (name.matches(StreamUtil.URL_PATTERN)) 
			return getInputStream(new URL(name));
		else 
			return new FileInputStream(name);
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
	@Override
	public InputStream getInputStream(URL source) throws IOException {
		if(source == null)
			throw new NullPointerException("Source URL is null");
		
		final InputStream newIs;
		
		final InputStream proxyIs;
		proxyIs = getURLConnection(source).getInputStream();
		
		// These are mainly for Session loading.
		if (source.toString().toLowerCase().endsWith(GZIP))
			newIs = new GZIPInputStream(proxyIs);
		else if (source.toString().toLowerCase().endsWith(ZIP))
			newIs = new ZipInputStream(proxyIs);
		else if (source.toString().toLowerCase().endsWith(JAR))
			newIs = new JarInputStream(proxyIs);
		else
			newIs = proxyIs;
		return newIs;
	}


	private Proxy getProxy() {
		String proxyType = properties.getProperty("proxy.server.type");
		if ("direct".equals(proxyType)) {
			return Proxy.NO_PROXY;
		}
		String hostName = properties.getProperty("proxy.server");
		String portString = properties.getProperty("proxy.server.port");
		try {
			int port = Integer.parseInt(portString);
			if ("http".equals(proxyType)) {
				return new Proxy(Type.HTTP, new InetSocketAddress(hostName, port));
			}
			if ("socks".equals(proxyType)) {
				return new Proxy(Type.SOCKS, new InetSocketAddress(hostName, port));
			}
		} catch (NumberFormatException e) {
		}
		return Proxy.NO_PROXY;
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
	@Override
	public URLConnection getURLConnection(final URL source) throws IOException {
		if (source == null)
			throw new NullPointerException("getURLConnection was given a null 'source' argument.");
		
		URLConnection uc = null;
		
		Proxy cytoProxy = getProxy();
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
}
