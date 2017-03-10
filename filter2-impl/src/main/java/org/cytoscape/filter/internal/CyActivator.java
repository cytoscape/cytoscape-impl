package org.cytoscape.filter.internal;

import java.util.Properties;

import org.cytoscape.application.swing.CytoPanelComponent;
import org.cytoscape.filter.TransformerManager;
import org.cytoscape.filter.internal.filters.column.ColumnFilterFactory;
import org.cytoscape.filter.internal.filters.column.ColumnFilterViewFactory;
import org.cytoscape.filter.internal.filters.composite.CompositeFilterFactory;
import org.cytoscape.filter.internal.filters.degree.DegreeFilterFactory;
import org.cytoscape.filter.internal.filters.degree.DegreeFilterViewFactory;
import org.cytoscape.filter.internal.filters.topology.TopologyFilterFactory;
import org.cytoscape.filter.internal.filters.topology.TopologyFilterViewFactory;
import org.cytoscape.filter.internal.transformers.adjacency.AdjacencyTransformerFactory;
import org.cytoscape.filter.internal.transformers.adjacency.AdjacencyTransformerViewFactory;
import org.cytoscape.filter.internal.transformers.interaction.InteractionTransformerFactory;
import org.cytoscape.filter.internal.transformers.interaction.InteractionTransformerViewFactory;
import org.cytoscape.filter.internal.view.FilterPanel;
import org.cytoscape.filter.internal.view.FilterPanelController;
import org.cytoscape.filter.internal.view.TransformerPanel;
import org.cytoscape.filter.internal.view.TransformerPanelController;
import org.cytoscape.filter.internal.view.TransformerViewManager;
import org.cytoscape.filter.internal.view.look.FilterPanelStyle;
import org.cytoscape.filter.internal.view.look.FlatStyle;
import org.cytoscape.filter.internal.work.FilterWorker;
import org.cytoscape.filter.internal.work.LazyWorkQueue;
import org.cytoscape.filter.internal.work.TransformerManagerImpl;
import org.cytoscape.filter.internal.work.TransformerWorker;
import org.cytoscape.filter.internal.work.ValidationManager;
import org.cytoscape.filter.model.ElementTransformerFactory;
import org.cytoscape.filter.model.FilterFactory;
import org.cytoscape.filter.model.HolisticTransformerFactory;
import org.cytoscape.filter.model.TransformerSource;
import org.cytoscape.filter.view.TransformerViewFactory;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.service.util.AbstractCyActivator;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.session.events.SessionAboutToBeLoadedListener;
import org.cytoscape.session.events.SessionAboutToBeSavedListener;
import org.cytoscape.session.events.SessionLoadedListener;
import org.osgi.framework.BundleContext;

/*
 * #%L
 * Cytoscape Filters 2 Impl (filter2-impl)
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
		
		TransformerManagerImpl transformerManager = new TransformerManagerImpl();
		registerService(bc, transformerManager, TransformerManager.class, new Properties());
		
		registerServiceListener(bc, transformerManager, "registerTransformerSource", "unregisterTransformerSource", TransformerSource.class);
		registerServiceListener(bc, transformerManager, "registerFilterFactory", "unregisterFilterFactory", FilterFactory.class);
		registerServiceListener(bc, transformerManager, "registerElementTransformerFactory", "unregisterElementTransformerFactory", ElementTransformerFactory.class);
		registerServiceListener(bc, transformerManager, "registerHolisticTransformerFactory", "unregisterHolisticTransformerFactory", HolisticTransformerFactory.class);
		
		TransformerViewManager transformerViewManager = new TransformerViewManager(transformerManager);
		registerServiceListener(bc, transformerViewManager, "registerTransformerViewFactory", "unregisterTransformerViewFactory", TransformerViewFactory.class);
		
		registerService(bc, new CyNetworkSource(), TransformerSource.class, new Properties());

		// Filters
		registerService(bc, new DegreeFilterFactory(), FilterFactory.class, new Properties());
		registerService(bc, new ColumnFilterFactory(), FilterFactory.class, new Properties());
		registerService(bc, new TopologyFilterFactory(), FilterFactory.class, new Properties());
		registerService(bc, new CompositeFilterFactory<CyNetwork, CyIdentifiable>(CyNetwork.class, CyIdentifiable.class), FilterFactory.class, new Properties());
		
		// Transformers
		registerService(bc, new InteractionTransformerFactory(), ElementTransformerFactory.class, new Properties());
		registerService(bc, new AdjacencyTransformerFactory(), ElementTransformerFactory.class, new Properties());
		
		ModelMonitor modelMonitor = new ModelMonitor();
		registerAllServices(bc, modelMonitor, new Properties());
		ValidationManager validationManager = new ValidationManager();
		registerAllServices(bc, validationManager, new Properties());
		
		FilterPanelStyle style = new FlatStyle();
		
		registerService(bc, new DegreeFilterViewFactory(style, modelMonitor), TransformerViewFactory.class, new Properties());
		registerService(bc, new ColumnFilterViewFactory(style, modelMonitor, serviceRegistrar), TransformerViewFactory.class, new Properties());
		registerService(bc, new TopologyFilterViewFactory(style), TransformerViewFactory.class, TopologyFilterViewFactory.getServiceProperties());
		registerService(bc, new InteractionTransformerViewFactory(style), TransformerViewFactory.class, new Properties());
		registerService(bc, new AdjacencyTransformerViewFactory(style, serviceRegistrar), TransformerViewFactory.class, AdjacencyTransformerViewFactory.getServiceProperties());
		
		LazyWorkQueue queue = new LazyWorkQueue();
		FilterIO filterIo = new FilterIO(serviceRegistrar);

		FilterWorker filterWorker = new FilterWorker(queue, serviceRegistrar);
		FilterPanelController filterPanelController = new FilterPanelController(transformerManager, transformerViewManager, validationManager, filterWorker, modelMonitor, filterIo, style, serviceRegistrar);
		FilterPanel filterPanel = new FilterPanel(filterPanelController, filterWorker, serviceRegistrar);
		
		TransformerWorker transformerWorker = new TransformerWorker(queue, transformerManager, serviceRegistrar);
		TransformerPanelController transformerPanelController = new TransformerPanelController(transformerManager, transformerViewManager, validationManager, filterPanelController, transformerWorker, filterIo, style, serviceRegistrar);
		TransformerPanel transformerPanel = new TransformerPanel(transformerPanelController, transformerWorker, serviceRegistrar);
	
		CytoPanelComponent selectPanel = new FilterCytoPanelComponent(transformerViewManager, modelMonitor, filterPanel, transformerPanel);
		registerService(bc, selectPanel, CytoPanelComponent.class, new Properties());
		
		FilterSettingsManager settingsManager = new FilterSettingsManager(filterPanel, transformerPanel, filterIo);
		registerService(bc, settingsManager, SessionAboutToBeSavedListener.class, new Properties());
		registerService(bc, settingsManager, SessionAboutToBeLoadedListener.class, new Properties());
		registerService(bc, settingsManager, SessionLoadedListener.class, new Properties());
	}
}

