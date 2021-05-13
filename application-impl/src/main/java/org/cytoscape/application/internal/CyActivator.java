package org.cytoscape.application.internal;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

import org.cytoscape.application.CyApplicationConfiguration;
import org.cytoscape.application.CyUserLog;
import org.cytoscape.application.NetworkViewRenderer;
import org.cytoscape.application.TableViewRenderer;
import org.cytoscape.property.CyProperty;
import org.cytoscape.service.util.AbstractCyActivator;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * #%L
 * Cytoscape Application Impl (application-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2021 The Cytoscape Consortium
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

public class CyActivator extends AbstractCyActivator {

	private static final String PROP_PREFIX = "installoptions";
	private static final String SHARE_STATISTICS_TAG = "shareStatistics";
	private static final String SHARE_STATISTICS_DATE_TAG = "shareStatisticsDate";
	private static final String INSTALL_OPTIONS_FILE_NAME = "cytoscape.installoptions";

	private static final Logger logger = LoggerFactory.getLogger(CyUserLog.NAME);

	@Override
	public void start(BundleContext bc) {
		final CyServiceRegistrar serviceRegistrar = getService(bc, CyServiceRegistrar.class);

		Bundle rootBundle = bc.getBundle(0);
		ShutdownHandler cytoscapeShutdown = new ShutdownHandler(rootBundle, serviceRegistrar);
		CyVersionImpl cytoscapeVersion = new CyVersionImpl(serviceRegistrar);
		CyApplicationConfigurationImpl cyApplicationConfiguration = new CyApplicationConfigurationImpl(cytoscapeVersion.getVersion());
		CyApplicationManagerImpl cyApplicationManager = new CyApplicationManagerImpl(serviceRegistrar);

		registerAllServices(bc, cyApplicationManager, new Properties());
		registerAllServices(bc, cytoscapeShutdown, new Properties());
		registerAllServices(bc, cytoscapeVersion, new Properties());
		registerAllServices(bc, cyApplicationConfiguration, new Properties());

		registerServiceListener(bc, cyApplicationManager::addNetworkViewRenderer, cyApplicationManager::removeNetworkViewRenderer, NetworkViewRenderer.class);
		registerServiceListener(bc, cyApplicationManager::addTableViewRenderer, cyApplicationManager::removeTableViewRenderer, TableViewRenderer.class);
		
		DefaultNetworkViewFactory viewFactory = new DefaultNetworkViewFactory(cyApplicationManager);
		Properties viewFactoryProperties = new Properties();
		viewFactoryProperties.put(Constants.SERVICE_RANKING, Integer.MAX_VALUE);
		registerService(bc, viewFactory, CyNetworkViewFactory.class, viewFactoryProperties);

		// For telemetry
		final CyProperty<Properties> cyPropertyServiceRef = getService(bc, CyProperty.class, "(cyPropertyName=cytoscape3.props)");

		checkIsntallOptions(cyApplicationConfiguration, cyPropertyServiceRef);
	}

	private void checkIsntallOptions(CyApplicationConfiguration appConfig,
									 CyProperty<Properties> cyPropertyServiceRef) {
		final File installationLocation = appConfig.getInstallationDirectoryLocation();
		final File optionFile = new File(
				installationLocation.getAbsolutePath(), INSTALL_OPTIONS_FILE_NAME);


		if(optionFile.exists()) {
			logger.info("Install option file found: " + optionFile.toString());

			// Extract
			final Properties prop = new Properties();
			try {
				final FileInputStream is = new FileInputStream(optionFile);
				prop.load(is);
			} catch(IOException e) {
				logger.warn("Could not read the install option property file.", e);
			}

			final String shareStatistics = prop.getProperty(SHARE_STATISTICS_TAG);
			final String shareStatisticsDate = prop.getProperty(SHARE_STATISTICS_DATE_TAG);

			final String currentDate = cyPropertyServiceRef.getProperties().getProperty(PROP_PREFIX + "." + SHARE_STATISTICS_DATE_TAG);

			if(shareStatistics == null || shareStatisticsDate == null) {
				return;
			}

			if(currentDate == null) {
				cyPropertyServiceRef.getProperties().setProperty(PROP_PREFIX + "." + SHARE_STATISTICS_TAG, shareStatistics);
				cyPropertyServiceRef.getProperties().setProperty(PROP_PREFIX + "." + SHARE_STATISTICS_DATE_TAG, shareStatisticsDate);
			} else {
			    // Define date format.
				final SimpleDateFormat parser = new SimpleDateFormat("yyyymmdd hhmmss");
				try {
					final Date oldDate = parser.parse(currentDate);
					final Date newDate = parser.parse(shareStatisticsDate);
					final int comparison = oldDate.compareTo(newDate);

					if(comparison < 0) {
						cyPropertyServiceRef.getProperties().setProperty(PROP_PREFIX + "." + SHARE_STATISTICS_TAG, shareStatistics);
						cyPropertyServiceRef.getProperties().setProperty(PROP_PREFIX + "." + SHARE_STATISTICS_DATE_TAG,
								shareStatisticsDate);
					}
				} catch(Exception e) {
					// Could not parse
					logger.warn("Failed to parse Share Statistics Date.", e);
				}
			}
		}
	}
}
