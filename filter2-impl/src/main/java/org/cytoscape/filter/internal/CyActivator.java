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

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.CytoPanelComponent;
import org.cytoscape.application.swing.CytoPanelComponentName;
import org.cytoscape.filter.TransformerManager;
import org.cytoscape.filter.internal.attribute.AttributeFilterFactory;
import org.cytoscape.filter.internal.attribute.AttributeFilterViewFactory;
import org.cytoscape.filter.internal.degree.DegreeFilterFactory;
import org.cytoscape.filter.internal.degree.DegreeFilterViewFactory;
import org.cytoscape.filter.internal.topology.TopologyFilterFactory;
import org.cytoscape.filter.internal.topology.TopologyFilterViewFactory;
import org.cytoscape.filter.internal.view.IconManager;
import org.cytoscape.filter.internal.view.IconManagerImpl;
import org.cytoscape.filter.internal.view.TransformerViewManager;
import org.cytoscape.filter.model.TransformerFactory;
import org.cytoscape.filter.model.TransformerSource;
import org.cytoscape.filter.view.TransformerViewFactory;
import org.cytoscape.service.util.AbstractCyActivator;
import org.osgi.framework.BundleContext;

public class CyActivator extends AbstractCyActivator {	
	public void start(BundleContext context) {
		TransformerManager transformerManager = new TransformerManagerImpl();
		registerService(context, transformerManager, TransformerManager.class, new Properties());
		
		registerServiceListener(context, transformerManager, "registerTransformerSource", "unregisterTransformerSource", TransformerSource.class);
		registerServiceListener(context, transformerManager, "registerTransformerFactory", "unregisterTransformerFactory", TransformerFactory.class);
		
		TransformerViewManager transformerViewManager = new TransformerViewManager(transformerManager);
		registerServiceListener(context, transformerViewManager, "registerTransformerViewFactory", "unregisterTransformerViewFactory", TransformerViewFactory.class);
		
		registerService(context, new CyNetworkSource(), TransformerSource.class, new Properties());

		registerService(context, new DegreeFilterFactory(), TransformerFactory.class, new Properties());
		registerService(context, new AttributeFilterFactory(), TransformerFactory.class, new Properties());
		registerService(context, new TopologyFilterFactory(), TransformerFactory.class, new Properties());
		
		ModelMonitor modelMonitor = new ModelMonitor();
		registerAllServices(context, modelMonitor, new Properties());
		
		IconManager iconManager = new IconManagerImpl();
		
		registerService(context, new DegreeFilterViewFactory(modelMonitor), TransformerViewFactory.class, new Properties());
		registerService(context, new AttributeFilterViewFactory(modelMonitor, iconManager), TransformerViewFactory.class, new Properties());
		registerService(context, new TopologyFilterViewFactory(), TransformerViewFactory.class, new Properties());

		CyApplicationManager applicationManager = getService(context, CyApplicationManager.class);
		CytoPanelComponent filterPanel = new FilterCytoPanelComponent(transformerManager, transformerViewManager, applicationManager, iconManager);
		Properties filterPanelProps = new Properties();
		filterPanelProps.setProperty("cytoPanelComponentName", CytoPanelComponentName.FILTER.toString());
		registerService(context, filterPanel, CytoPanelComponent.class, filterPanelProps);
	}
}

