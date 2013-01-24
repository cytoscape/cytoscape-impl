package org.cytoscape.filter.internal.filters;

/*
 * #%L
 * Cytoscape Filters Impl (filter-impl)
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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.cytoscape.application.events.CyShutdownEvent;
import org.cytoscape.application.events.CyShutdownListener;
import org.cytoscape.application.events.CyStartEvent;
import org.cytoscape.application.events.CyStartListener;
import org.cytoscape.filter.internal.filters.model.CompositeFilter;
import org.cytoscape.filter.internal.filters.model.FilterModelLocator;
import org.cytoscape.filter.internal.filters.util.FilterUtil;
import org.cytoscape.filter.internal.filters.util.ServicesUtil;
import org.cytoscape.filter.internal.read.filter.FilterReader;
import org.cytoscape.filter.internal.write.filter.FilterWriter;
import org.cytoscape.session.CySession;
import org.cytoscape.session.events.SessionAboutToBeSavedEvent;
import org.cytoscape.session.events.SessionAboutToBeSavedListener;
import org.cytoscape.session.events.SessionLoadedEvent;
import org.cytoscape.session.events.SessionLoadedListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 */
public class FilterApp implements CyShutdownListener, SessionLoadedListener, CyStartListener, SessionAboutToBeSavedListener {

	private final FilterReader reader;
	private final FilterWriter writer;
	private final FilterModelLocator modelLocator;
	
	private static final Logger logger = LoggerFactory.getLogger(FilterApp.class);

	public FilterApp(final FilterReader reader, final FilterWriter writer, final FilterModelLocator modelLocator) {
		if (reader == null)
			throw new NullPointerException("reader is null");
		if (writer == null)
			throw new NullPointerException("writer is null");
		if (modelLocator == null)
			throw new NullPointerException("modelLocator is null");
		
		this.reader = reader;
		this.writer = writer;
		this.modelLocator = modelLocator;
	}

	@Override
	public void handleEvent(CyStartEvent e) {
		modelLocator.addFilters(loadDefaultFilters());
	}

	@Override
	public void handleEvent(CyShutdownEvent e) {
		saveGlobalFilters();	
	}

	@Override
	public void handleEvent(SessionLoadedEvent e) {
		// Remove current session-only filters
		List<CompositeFilter> removeList = new ArrayList<CompositeFilter>();
		Set<String> globalNames = new HashSet<String>();
		
		for (CompositeFilter cf : modelLocator.getFilters()) {
			if (!cf.getAdvancedSetting().isGlobalChecked())
				removeList.add(cf);
			else
				globalNames.add(cf.getName());
		}
		
		modelLocator.removeFilters(removeList);
		
		// Add new session filters 
		Collection<CompositeFilter> addList = null;
		CySession sess = e.getLoadedSession();
		
		if (sess != null) {
			Map<String, List<File>> filesMap = sess.getAppFileListMap();
			
			if (filesMap != null) {
				List<File> files = filesMap.get(FilterUtil.FILTER_APP_NAME);
				
				if (files == null) // Try the old (2.x) name
					files = filesMap.get("FilterPlugin");
				
				if (files != null) {
					for (File f : files) {
						if (f.getName().endsWith(FilterUtil.SESSION_FILE_NAME)) {
							// There should be only one file!
							addList = reader.read(f);
							break;
						}
					}
					
					if (addList != null) {
						// TODO: Do not add session filters that have the same name of current global filters?
						Iterator<CompositeFilter> iter = addList.iterator();
						
						while (iter.hasNext()) {
							if (globalNames.contains(iter.next().getName()))
								iter.remove();
						}
					}
				}
			}
		}
		
		if (addList != null)
			modelLocator.addFilters(addList);
	}
	
	@Override
	public void handleEvent(SessionAboutToBeSavedEvent e) {
		Vector<CompositeFilter> allFilters = modelLocator.getFilters();
		List<CompositeFilter> saveList = new ArrayList<CompositeFilter>();
		
		for (CompositeFilter cf : allFilters) {
			if (cf.getAdvancedSetting().isSessionChecked())
				saveList.add(cf);
		}
		
		if (!saveList.isEmpty()) {
			try {
				// Create an empty file on system temp directory
				File tmpFile = new File(System.getProperty("java.io.tmpdir"), FilterUtil.SESSION_FILE_NAME);
				tmpFile.deleteOnExit();
				
				// Write to the file
				writer.write(saveList, tmpFile);
				
				// Add it to the apps list
				List<File> fileList = new ArrayList<File>();
				fileList.add(tmpFile);
				e.addAppFiles(FilterUtil.FILTER_APP_NAME, fileList);
			} catch (Exception ex) {
				logger.error("Error adding filter files to be saved in the session.", ex);
			}
		}
	}
	
	private Collection<CompositeFilter> loadDefaultFilters() {
		Collection<CompositeFilter> filters = null;
		InputStream is = null;
		
		try {
			// Load global filters from CyProperty.DEFAULT_PROPS_CONFIG_DIR  directory.
			final File file = getGlobalFilterFile();
			
			if (file.exists()) {
				try {
					is = new FileInputStream(file);
				} catch (Exception e) {
					logger.error("Cannot load default filter from config directory.", e);
				}
			}
			
			// If there is no global filters, load a default one
			if (is == null) {
				// note: "/" works for both Mac and Windows, File.separator does not work on Windows
				String fileName = "/" + FilterUtil.DEFAULT_FILE_NAME;
				
				try {
					is = getClass().getResourceAsStream(fileName);
				} catch (Exception e) {
					logger.error("Cannot load default filters from jar.", e);
				}
			}
					
			if (is != null)
				filters = reader.read(is);
			else
				logger.warn("could not find resource: " + file.getAbsolutePath());
		} finally {
			if (is != null) {
				try { is.close(); } catch (IOException ioe) {}
				is = null;
			}
		}
		
		return filters;
	}
	
	private void saveGlobalFilters() {
		// Get current global filters
		Set<CompositeFilter> globalFilters = new LinkedHashSet<CompositeFilter>();
		
		for (CompositeFilter cf : modelLocator.getFilters()) {
			if (cf.getAdvancedSetting().isGlobalChecked())
				globalFilters.add(cf);
		}
		
		// Save global filters in the CyProperty.DEFAULT_PROPS_CONFIG_DIR  directory
		final File file = getGlobalFilterFile();
		writer.saveGlobalPropFile(globalFilters, file);
	}
	
	private File getGlobalFilterFile() {
		String cyConfigVerDir = new File(
				ServicesUtil.cyApplicationConfigurationServiceRef.getConfigurationDirectoryLocation(),
				File.separator + ServicesUtil.cytoscapeVersionService.getMajorVersion()).getAbsolutePath(); // + "."
						//+ ServicesUtil.cytoscapeVersionService.getMinorVersion()).getAbsolutePath();
		
		final File file = new File(cyConfigVerDir + File.separator + FilterUtil.DEFAULT_FILE_NAME);
		
		return file;
	}
}
