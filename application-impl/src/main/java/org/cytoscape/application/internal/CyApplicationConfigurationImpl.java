package org.cytoscape.application.internal;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Dictionary;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

/*
 * #%L
 * Cytoscape Application Impl (application-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2008 - 2013 The Cytoscape Consortium
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

import org.cytoscape.application.CyApplicationConfiguration;
import org.cytoscape.application.CyUserLog;
import org.cytoscape.property.CyProperty;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CyApplicationConfigurationImpl implements CyApplicationConfiguration {
	
	/** Default configuration directory used for all Cytoscape configuration files */
	public static final String DEFAULT_CONFIG_DIR = CyProperty.DEFAULT_PROPS_CONFIG_DIR ;

	private static final Logger logger = LoggerFactory.getLogger(CyUserLog.NAME);
	
	private static final String USER_DIR = System.getProperty("user.dir");
	private static final String USER_HOME_DIR = System.getProperty("user.home");
	private static final String CYTOSCAPE_HOME_DIR = System.getProperty("cytoscape.home");
	private static final String APP_CONFIGURATION_DIR = "app-data";
	
	private static final String BUNDLE_SYMBOLIC_NAME = "Bundle-SymbolicName";
	private static final String BUNDLE_VERSION = "Bundle-Version";

	private final File cytoscapeConfigurationDir;
	private final File cytoscapeInstallationDir;
	
	public CyApplicationConfigurationImpl(String version) {
		cytoscapeConfigurationDir = new File(USER_HOME_DIR, DEFAULT_CONFIG_DIR);
		
		if(cytoscapeConfigurationDir.exists() == false) {
			cytoscapeConfigurationDir.mkdir();
			logger.warn("CytoscapeConfiguration directory was not available.  New directory created.");
		} else {
			logger.info("Setting file directory = " + cytoscapeConfigurationDir.getAbsolutePath());
		}
		
		if(CYTOSCAPE_HOME_DIR != null)
			cytoscapeInstallationDir = new File(CYTOSCAPE_HOME_DIR);
		else if(USER_DIR != null)
			cytoscapeInstallationDir = new File(USER_DIR);
		else
			cytoscapeInstallationDir = null;
		
		addActiveSessionFile(version);
	}
	
	//------------------------------
	/* we keep a file in the configuration folder that should be removed on exit
	 * 
	 * if it is still there on startup, we had a crash / hang and we want to collect a log
	 * 
	 */
	public static final String activeSessionFilename = "tracker.active.session";

	private void addActiveSessionFile(String version) {
		
		// if (version.contains("SNAPSHOT")) 	return;		//  a developer override
		Path path = Paths.get(cytoscapeConfigurationDir.getAbsolutePath(), activeSessionFilename);
		if (path.toFile().exists())
		{
			try
			{
				String content = new String(Files.readAllBytes(path));			
				logger.error("Previous session failed to terminate gracefully. " + content);
				phoneHome(content);
			}
			catch (Exception e)
			{
				logger.error("Previous session failed to terminate gracefully and file failed to read.");
			}
		}
		
		try
		{
			logger.info("Write " + activeSessionFilename + " into " + cytoscapeConfigurationDir);
			Files.write(path, version.getBytes());			
		}
		catch (Exception e)
		{
			logger.error("Failed to write " + activeSessionFilename + " into " + cytoscapeConfigurationDir);
		}
	}


	private void phoneHome(String content) {
		// send the result of the session back to San Diego for processing
	}
	//------------------------------

	@Override
	public File getInstallationDirectoryLocation() {
		return cytoscapeInstallationDir;
	}

	@Override
	public File getConfigurationDirectoryLocation() {
		return cytoscapeConfigurationDir;	
	}
	
	@Override
	public File getAppConfigurationDirectoryLocation(Class<?> appClass) {
		File configurationDirectory = getConfigurationDirectoryLocation();
		Bundle bundle = FrameworkUtil.getBundle(appClass);
		if( bundle != null )
		{
			Dictionary<String, String> headers = bundle.getHeaders();
			String basePath = headers.get(BUNDLE_SYMBOLIC_NAME);
			String version = headers.get(BUNDLE_VERSION);
			if( version == null )
				version = "";
			String path = join(File.separator, configurationDirectory.getPath(), APP_CONFIGURATION_DIR, basePath + "-" + version);
			return new File(path);
		}
		//Bundle is null, we are dealing with a Simple App.   
		URLClassLoader cl = (URLClassLoader)appClass.getClassLoader();
	    URL url = cl.findResource("META-INF/MANIFEST.MF");
		try {
			Manifest manifest = new Manifest(url.openStream());
			Attributes a = manifest.getMainAttributes();
			String app = a.getValue("Cytoscape-App");
			String version = a.getValue("Cytoscape-App-Version");
			if( version == null )
				version = "";
			if( app != null && version != null )
			{
				String path = join(File.separator, configurationDirectory.getPath(), APP_CONFIGURATION_DIR, app + "-" + version);
				return new File(path);
			}
			else
			{
				return null;
			}
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	private static String join(String separator, String... items) {
		StringBuilder builder = new StringBuilder();
		boolean first = true;
		for (String item : items) {
			if (!first) {
				builder.append(separator);
			}
			builder.append(item);
			first = false;
		}
		return builder.toString();
	}
}
