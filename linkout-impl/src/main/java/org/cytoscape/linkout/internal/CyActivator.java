package org.cytoscape.linkout.internal;

import java.util.Properties;

import org.cytoscape.property.CyProperty;
import org.cytoscape.property.PropertyUpdatedListener;
import org.cytoscape.service.util.AbstractCyActivator;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.osgi.framework.BundleContext;

/*
 * #%L
 * Cytoscape Linkout Impl (linkout-impl)
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

public class CyActivator extends AbstractCyActivator {
	
	@Override
	public void start(BundleContext bc) {
		final CyServiceRegistrar serviceRegistrar = getService(bc, CyServiceRegistrar.class);
		
		{
			PropsReader linkoutProps = new PropsReader("linkout", "linkout.props", CyProperty.SavePolicy.CONFIG_DIR);
			LinkOut linkout = new LinkOut(linkoutProps, serviceRegistrar);
			
			Properties props = new Properties();
			props.setProperty("cyPropertyName", "linkout");
	
			registerService(bc, linkoutProps, CyProperty.class, props);
			registerService(bc, linkout, PropertyUpdatedListener.class, props);
			
			registerServiceListener(bc, linkout, "addCommanLineLinkOut", "removeCommanLineLinkOut", CyProperty.class);
		}
	}
}
