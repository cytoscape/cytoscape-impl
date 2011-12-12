package org.cytoscape.application.internal;

import java.io.File;

import org.cytoscape.application.CyApplicationConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CyApplicationConfigurationImpl implements CyApplicationConfiguration {
	
	private static final Logger logger = LoggerFactory.getLogger(CyApplicationConfigurationImpl.class);
	
	private static final String DEF_USER_DIR = System.getProperty("user.home");
	
	private final File configFileLocation;
	
	public CyApplicationConfigurationImpl() {
		configFileLocation = new File(DEF_USER_DIR, DEFAULT_CONFIG_DIR);
		
		if(configFileLocation.exists() == false) {
			configFileLocation.mkdir();
			logger.warn(".cytoscape directory was not available.  New directory created.");
		} else {
			logger.info("Setting file directory = " + configFileLocation.getAbsolutePath());
		}
		
	}

	@Override
	public File getConfigurationDirectoryLocation() {
		return configFileLocation;	
	}

}
