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
import org.cytoscape.filter.model.ElementTransformerFactory;
import org.cytoscape.filter.model.FilterFactory;
import org.cytoscape.filter.model.HolisticTransformerFactory;
import org.cytoscape.filter.model.TransformerSource;
import org.cytoscape.filter.view.TransformerViewFactory;
import org.cytoscape.io.read.CyTransformerReader;
import org.cytoscape.io.write.CyTransformerWriter;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.service.util.AbstractCyActivator;
import org.cytoscape.session.events.SessionAboutToBeLoadedListener;
import org.cytoscape.session.events.SessionAboutToBeSavedListener;
import org.cytoscape.session.events.SessionLoadedListener;
import org.cytoscape.util.swing.IconManager;
import org.cytoscape.work.TaskManager;
import org.osgi.framework.BundleContext;

public class CyActivator extends AbstractCyActivator {
	
	public void start(BundleContext context) {
		TransformerManagerImpl transformerManager = new TransformerManagerImpl();
		registerService(context, transformerManager, TransformerManager.class, new Properties());
		
		registerServiceListener(context, transformerManager, "registerTransformerSource", "unregisterTransformerSource", TransformerSource.class);
		registerServiceListener(context, transformerManager, "registerFilterFactory", "unregisterFilterFactory", FilterFactory.class);
		registerServiceListener(context, transformerManager, "registerElementTransformerFactory", "unregisterElementTransformerFactory", ElementTransformerFactory.class);
		registerServiceListener(context, transformerManager, "registerHolisticTransformerFactory", "unregisterHolisticTransformerFactory", HolisticTransformerFactory.class);
		
		TransformerViewManager transformerViewManager = new TransformerViewManager(transformerManager);
		registerServiceListener(context, transformerViewManager, "registerTransformerViewFactory", "unregisterTransformerViewFactory", TransformerViewFactory.class);
		
		registerService(context, new CyNetworkSource(), TransformerSource.class, new Properties());

		// Filters
		registerService(context, new DegreeFilterFactory(), FilterFactory.class, new Properties());
		registerService(context, new ColumnFilterFactory(), FilterFactory.class, new Properties());
		registerService(context, new TopologyFilterFactory(), FilterFactory.class, new Properties());
		registerService(context, new CompositeFilterFactory<>(CyNetwork.class, CyIdentifiable.class), FilterFactory.class, new Properties());
		
		// Transformers
		registerService(context, new InteractionTransformerFactory(), ElementTransformerFactory.class, new Properties());
		registerService(context, new AdjacencyTransformerFactory(), ElementTransformerFactory.class, new Properties());
		
		ModelMonitor modelMonitor = new ModelMonitor();
		registerAllServices(context, modelMonitor, new Properties());
		
		IconManager iconManager = getService(context, IconManager.class);
		FilterPanelStyle style = new FlatStyle();
		
		registerService(context, new DegreeFilterViewFactory(style, modelMonitor), TransformerViewFactory.class, new Properties());
		registerService(context, new ColumnFilterViewFactory(style, modelMonitor), TransformerViewFactory.class, new Properties());
		registerService(context, new TopologyFilterViewFactory(style), TransformerViewFactory.class, TopologyFilterViewFactory.getServiceProperties());
		registerService(context, new InteractionTransformerViewFactory(style), TransformerViewFactory.class, new Properties());
		registerService(context, new AdjacencyTransformerViewFactory(style, iconManager), TransformerViewFactory.class, AdjacencyTransformerViewFactory.getServiceProperties());
		
		LazyWorkQueue queue = new LazyWorkQueue();
		CyApplicationManager applicationManager = getService(context, CyApplicationManager.class);
		
		CyTransformerReader reader = getService(context, CyTransformerReader.class);
		CyTransformerWriter writer = getService(context, CyTransformerWriter.class);
		FilterIO filterIo = new FilterIO(reader, writer);

		TaskManager<?, ?> taskManager = getService(context, TaskManager.class);

		FilterWorker filterWorker = new FilterWorker(queue, applicationManager);
		FilterPanelController filterPanelController = new FilterPanelController(transformerManager, transformerViewManager, filterWorker, modelMonitor, filterIo, taskManager, style, iconManager);
		FilterPanel filterPanel = new FilterPanel(filterPanelController, iconManager, filterWorker);
		
		TransformerWorker transformerWorker = new TransformerWorker(queue, applicationManager, transformerManager);
		TransformerPanelController transformerPanelController = new TransformerPanelController(transformerManager, transformerViewManager, filterPanelController, transformerWorker, filterIo, taskManager, style, iconManager);
		TransformerPanel transformerPanel = new TransformerPanel(transformerPanelController, iconManager, transformerWorker);
	
		CytoPanelComponent selectPanel = new FilterCytoPanelComponent(transformerViewManager, applicationManager, iconManager, modelMonitor, filterPanel, transformerPanel);
		registerService(context, selectPanel, CytoPanelComponent.class, new Properties());
		
		FilterSettingsManager settingsManager = new FilterSettingsManager(filterPanel, transformerPanel, filterIo);
		registerService(context, settingsManager, SessionAboutToBeSavedListener.class, new Properties());
		registerService(context, settingsManager, SessionAboutToBeLoadedListener.class, new Properties());
		registerService(context, settingsManager, SessionLoadedListener.class, new Properties());
	}
}

