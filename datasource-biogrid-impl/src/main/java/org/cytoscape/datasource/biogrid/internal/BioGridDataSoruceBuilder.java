package org.cytoscape.datasource.biogrid.internal;

import java.io.File;
import java.io.IOException;
import java.util.Properties;
import java.util.Set;

import org.cytoscape.io.datasource.DataSource;
import org.osgi.framework.BundleContext;

public class BioGridDataSoruceBuilder {
	
	public BioGridDataSoruceBuilder(final BundleContext bc, final File settingFileLocation) {
		BiogridDataLoader task = new BiogridDataLoader(settingFileLocation);
		try {
			task.extract();
		} catch (IOException e) {
			e.printStackTrace();
		}
		Set<DataSource> dsSet = task.getDataSources();
		
		for(final DataSource ds: dsSet) {
			bc.registerService("org.cytoscape.io.datasource.DataSource", ds, new Properties());
		}
	}
}
