package org.cytoscape.datasource.biogrid.internal;

/*
 * #%L
 * Cytoscape BioGrid Datasource Impl (datasource-biogrid-impl)
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
import org.cytoscape.application.CyApplicationConfiguration;
import org.cytoscape.property.CyProperty;
import org.cytoscape.service.util.AbstractCyActivator;
import org.osgi.framework.BundleContext;

public class CyActivator extends AbstractCyActivator {

	//private static final String LATEST_RELEASE_URL = "http://thebiogrid.org/downloads/archives/Latest%20Release/BIOGRID-ORGANISM-LATEST.mitab.zip";

	public CyActivator() {
		super();
	}

	public void start(BundleContext bc) {
		final CyApplicationConfiguration cyApplicationConfigurationServiceRef = getService(bc,
				CyApplicationConfiguration.class);
		final CyProperty<?> cytoscapePropertiesServiceRef = getService(bc, CyProperty.class,
				"(cyPropertyName=cytoscape3.props)");

		final BioGridDataSoruceBuilder builder = new BioGridDataSoruceBuilder(bc,
				cyApplicationConfigurationServiceRef.getConfigurationDirectoryLocation(), cytoscapePropertiesServiceRef);

		// Update network data.
//		URL datasourceUrl = null;
//		try {
//			datasourceUrl = new URL(LATEST_RELEASE_URL);
//		} catch (MalformedURLException e) {
//			e.printStackTrace();
//		}
//
//		final UpdateDatasourceTaskFactory updateDatasourceTaskFactory = new UpdateDatasourceTaskFactory(
//				cytoscapePropertiesServiceRef, datasourceUrl,
//				cyApplicationConfigurationServiceRef.getConfigurationDirectoryLocation());
//
//		Properties updateDatasourceTaskFactoryProps = new Properties();
//		updateDatasourceTaskFactoryProps.setProperty(ID, "updateDatasourceTaskFactory");
//		updateDatasourceTaskFactoryProps.setProperty(PREFERRED_MENU, "Help");
//		updateDatasourceTaskFactoryProps.setProperty(TITLE, "Update Organism Preset Networks");
//
//		registerService(bc, updateDatasourceTaskFactory, TaskFactory.class, updateDatasourceTaskFactoryProps);
	}
}