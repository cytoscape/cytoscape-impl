package org.cytoscape.datasource.biogrid.internal;

import org.cytoscape.application.CyApplicationConfiguration;
import org.cytoscape.service.util.AbstractCyActivator;
import org.osgi.framework.BundleContext;

public class CyActivator extends AbstractCyActivator {
	public CyActivator() {
		super();
	}

	public void start(BundleContext bc) {
		CyApplicationConfiguration cyApplicationConfigurationServiceRef = getService(bc,
				CyApplicationConfiguration.class);

		BioGridDataSoruceBuilder builder = new BioGridDataSoruceBuilder(bc,
				cyApplicationConfigurationServiceRef.getConfigurationDirectoryLocation());
	}
}