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

import java.io.File;
import java.io.IOException;
import java.util.Properties;
import java.util.Set;

import org.cytoscape.io.datasource.DataSource;
import org.cytoscape.property.CyProperty;
import org.osgi.framework.BundleContext;

public class BioGridDataSoruceBuilder {
	
	public BioGridDataSoruceBuilder(final BundleContext bc, final File settingFileLocation, final CyProperty prop) {
		BiogridDataLoader task = new BiogridDataLoader(prop, settingFileLocation);
		
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
