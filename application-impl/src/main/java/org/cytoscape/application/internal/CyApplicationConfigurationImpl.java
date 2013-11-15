package org.cytoscape.application.internal;

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
import org.cytoscape.property.CyProperty;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Dictionary;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

public class CyApplicationConfigurationImpl implements CyApplicationConfiguration {
	
	/** Default configuration directory used for all Cytoscape configuration files */
	public static final String DEFAULT_CONFIG_DIR = CyProperty.DEFAULT_PROPS_CONFIG_DIR ;

	private static final Logger logger = LoggerFactory.getLogger(CyApplicationConfigurationImpl.class);
	
	private static final String DEF_USER_DIR = System.getProperty("user.home");

	private static final String APP_CONFIGURATION_DIR = "app-data";
	
	private static final String BUNDLE_SYMBOLIC_NAME = "Bundle-SymbolicName";
	private static final String BUNDLE_VERSION = "Bundle-Version";

	private final File configFileLocation;
	
	public CyApplicationConfigurationImpl() {
		configFileLocation = new File(DEF_USER_DIR, DEFAULT_CONFIG_DIR);
		
		if(configFileLocation.exists() == false) {
			configFileLocation.mkdir();
			logger.warn("CytoscapeConfiguration directory was not available.  New directory created.");
		} else {
			logger.info("Setting file directory = " + configFileLocation.getAbsolutePath());
		}
		
	}

	@Override
	public File getConfigurationDirectoryLocation() {
		return configFileLocation;	
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
