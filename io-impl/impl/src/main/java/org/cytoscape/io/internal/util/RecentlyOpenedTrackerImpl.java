package org.cytoscape.io.internal.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import org.cytoscape.application.CyApplicationConfiguration;
import org.cytoscape.io.util.RecentlyOpenedTracker;
import org.cytoscape.property.CyProperty;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * #%L
 * Cytoscape IO Impl (io-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2017 The Cytoscape Consortium
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

public class RecentlyOpenedTrackerImpl implements RecentlyOpenedTracker {
	
	private static final int MAX_TRACK_COUNT = 100;
	private static final String MAX_FILES_PROP = "maxRecentlyOpenedFiles";
	
	private static final Logger logger = LoggerFactory.getLogger(RecentlyOpenedTrackerImpl.class); 
	
	private final String trackerFileName;
	private final LinkedList<URL> trackerURLs;
	private final File propDir;
	
	private final CyServiceRegistrar serviceRegistrar;
	
	/**
	 * Creates a "recently opened" file tracker.
	 * 
	 * @param trackerFileName
	 *            the name of the file in the Cytoscape config directory to read saved file names from.
	 */
	public RecentlyOpenedTrackerImpl(final String trackerFileName, final CyServiceRegistrar serviceRegistrar) {
		this.trackerFileName = trackerFileName;
		this.propDir = serviceRegistrar.getService(CyApplicationConfiguration.class).getConfigurationDirectoryLocation();
		this.trackerURLs = new LinkedList<URL>();
		this.serviceRegistrar = serviceRegistrar;

		BufferedReader reader = null;
		
		try {
			final File input = new File(propDir, trackerFileName);
			
			if (!input.exists())
				input.createNewFile();
	
			reader = new BufferedReader(new FileReader(input));
			int max = getMaxRecentlyOpenedFiles();
			String line;
			
			while ((line = reader.readLine()) != null && trackerURLs.size() < max) {
				final String newURL = line.trim();
				
				if (newURL.length() > 0)
					trackerURLs.addLast(new URL(newURL));
			}
		} catch (IOException ioe) {
			logger.warn("Problem reading Recently Opened File list", ioe); 	
		} finally {
			if (reader != null) {
				try {
					reader.close();
					reader = null;
				} catch (IOException e) {
					logger.error("Could not close the reader for RecentlyOpenedTracker.", e); 	
				}
			}
		}
	}

	@Override
	public synchronized List<URL> getRecentlyOpenedURLs() {
		return Collections.unmodifiableList(trackerURLs);
	}

	@Override
	public synchronized void add(final URL newURL) {
		trackerURLs.remove(newURL);
		
		if (trackerURLs.size() == getMaxRecentlyOpenedFiles())
			trackerURLs.removeLast();
		
		trackerURLs.addFirst(newURL);
	}
	
	@Override
	public void remove(URL url) {
		trackerURLs.remove(url);
	}
	
	@Override
	public void clear() {
		trackerURLs.clear();
	}

	@Override
	public void writeOut() throws FileNotFoundException {
		final PrintWriter writer = new PrintWriter(new File(propDir, trackerFileName));
		
		for (final URL trackerURL : trackerURLs)
			writer.println(trackerURL.toString());
		
		writer.close();
	}

	@Override
	public synchronized URL getMostRecentlyOpenedURL() {
		if (trackerURLs.isEmpty())
			return null;
		else
			return trackerURLs.getFirst();
	}
	
	private int getMaxRecentlyOpenedFiles() {
		int max = 12;
		
		try {
			Properties props = (Properties) serviceRegistrar
					.getService(CyProperty.class, "(cyPropertyName=cytoscape3.props)").getProperties();
			String s = props.getProperty(MAX_FILES_PROP);
			
			if (s != null)
				max = Integer.parseInt(s.trim());
		} catch (Exception e) {
			logger.error("Cannot load property " + MAX_FILES_PROP, e);
		}
		
		return Math.min(max, MAX_TRACK_COUNT);
	}
}
