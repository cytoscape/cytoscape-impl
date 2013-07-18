package org.cytoscape.filter.internal;

/*
 * #%L
 * Cytoscape Filters Impl (filter-impl)
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

import java.util.Properties;

import org.cytoscape.filter.TransformerManager;
import org.cytoscape.filter.internal.transformers.DegreeFilterFactory;
import org.cytoscape.filter.internal.transformers.NumericAttributeFilterFactory;
import org.cytoscape.filter.internal.transformers.StringAttributeFilterFactory;
import org.cytoscape.filter.model.TransformerFactory;
import org.cytoscape.filter.model.TransformerSource;
import org.cytoscape.service.util.AbstractCyActivator;
import org.osgi.framework.BundleContext;

public class CyActivator extends AbstractCyActivator {	
	public void start(BundleContext context) {
		TransformerManager transformerManager = new TransformerManagerImpl();
		registerService(context, transformerManager, TransformerManager.class, new Properties());
		
		registerServiceListener(context, transformerManager, "registerTransformerSource", "unregisterTransformerSource", TransformerSource.class);
		registerServiceListener(context, transformerManager, "registerTransformerFactory", "unregisterTransformerFactory", TransformerFactory.class);
		
		registerService(context, new CyNetworkSource(), TransformerSource.class, new Properties());

		registerService(context, new DegreeFilterFactory(), TransformerFactory.class, new Properties());
		registerService(context, new NumericAttributeFilterFactory(), TransformerFactory.class, new Properties());
		registerService(context, new StringAttributeFilterFactory(), TransformerFactory.class, new Properties());
	}
}

