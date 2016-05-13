package org.cytoscape.internal.shutdown;

import static org.cytoscape.property.CyProperty.SavePolicy.CONFIG_DIR;
import static org.cytoscape.property.CyProperty.SavePolicy.SESSION_FILE_AND_CONFIG_DIR;

import java.io.File;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.cytoscape.application.CyApplicationConfiguration;
import org.cytoscape.application.events.CyShutdownEvent;
import org.cytoscape.application.events.CyShutdownListener;
import org.cytoscape.property.CyProperty;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * #%L
 * Cytoscape Swing Application Impl (swing-application-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2016 The Cytoscape Consortium
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

public class ConfigDirPropertyWriter implements CyShutdownListener {
	
	private final Map<CyProperty<?>, Map<?, ?>> configDirProperties;
	private final CyServiceRegistrar serviceRegistrar;
	
	private static final Logger logger = LoggerFactory.getLogger(ConfigDirPropertyWriter.class);

	public ConfigDirPropertyWriter(final CyServiceRegistrar serviceRegistrar) {
		this.serviceRegistrar = serviceRegistrar;
		configDirProperties = new HashMap<>();
	}

	@Override
	public void handleEvent(final CyShutdownEvent event) {
		for (final Map.Entry<CyProperty<?>, Map<?, ?>> keyAndValue : configDirProperties.entrySet()) {
			final String propertyName = (String) keyAndValue.getValue().get("cyPropertyName");
			final String propertyFileName;

			if (propertyName.endsWith(".props"))
				propertyFileName = propertyName;
			else
				propertyFileName = propertyName + ".props";

			final CyApplicationConfiguration config = serviceRegistrar.getService(CyApplicationConfiguration.class);
			final File outputFile = new File(config.getConfigurationDirectoryLocation(), propertyFileName);

			final Properties props = (Properties) keyAndValue.getKey().getProperties();

			try {
				FileOutputStream out = new FileOutputStream(outputFile);
				props.store(out, null);
				out.close();
			} catch (Exception e) {
				logger.error("Error in wring properties file.");
			}
		}
	}

	public void addCyProperty(final CyProperty<?> cyProp, final Map<?, ?> properties) {
		if (cyProp.getSavePolicy() == CONFIG_DIR || cyProp.getSavePolicy() == SESSION_FILE_AND_CONFIG_DIR)
			configDirProperties.put(cyProp, properties);
	}

	public void removeCyProperty(final CyProperty<?> cyProp, final Map<?, ?> properties) {
		if (cyProp.getSavePolicy() == CONFIG_DIR || cyProp.getSavePolicy() == SESSION_FILE_AND_CONFIG_DIR)
			configDirProperties.remove(cyProp);
	}
}
