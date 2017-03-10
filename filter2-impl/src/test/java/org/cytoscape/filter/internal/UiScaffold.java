package org.cytoscape.filter.internal;

import static org.mockito.Mockito.when;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.swing.JFrame;
import javax.swing.JRootPane;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.events.SetCurrentNetworkEvent;
import org.cytoscape.filter.internal.filters.column.ColumnFilterFactory;
import org.cytoscape.filter.internal.filters.degree.DegreeFilterFactory;
import org.cytoscape.filter.internal.filters.degree.DegreeFilterViewFactory;
import org.cytoscape.filter.internal.filters.topology.TopologyFilterFactory;
import org.cytoscape.filter.internal.filters.topology.TopologyFilterViewFactory;
import org.cytoscape.filter.internal.transformers.interaction.InteractionTransformerFactory;
import org.cytoscape.filter.internal.transformers.interaction.InteractionTransformerViewFactory;
import org.cytoscape.filter.internal.view.FilterPanel;
import org.cytoscape.filter.internal.view.FilterPanelController;
import org.cytoscape.filter.internal.view.SelectPanel;
import org.cytoscape.filter.internal.view.TransformerPanel;
import org.cytoscape.filter.internal.view.TransformerPanelController;
import org.cytoscape.filter.internal.view.TransformerViewManager;
import org.cytoscape.filter.internal.view.look.FilterPanelStyle;
import org.cytoscape.filter.internal.view.look.StandardStyle;
import org.cytoscape.filter.internal.work.FilterWorker;
import org.cytoscape.filter.internal.work.LazyWorkQueue;
import org.cytoscape.filter.internal.work.TransformerManagerImpl;
import org.cytoscape.filter.internal.work.TransformerWorker;
import org.cytoscape.filter.internal.work.ValidationManager;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.NetworkTestSupport;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.util.swing.IconManager;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.work.TaskManager;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

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

public class UiScaffold {
	
	static final int TOTAL_NODES = 1000;
	static final int TOTAL_EDGES = 1000;
	
	@Mock CyServiceRegistrar serviceRegistrar;
	@Mock CyApplicationManager applicationManager;
	@Mock TaskManager<?, ?> taskManager;
	@Mock IconManager iconManager;
	@Mock CyNetworkView networkView;
	
	void start() {
		MockitoAnnotations.initMocks(this);
		
		Random random = new Random(0);

		NetworkTestSupport networkTestSupport = new NetworkTestSupport();
		CyNetwork network = networkTestSupport.getNetwork();
		CyTable nodeTable = network.getDefaultNodeTable();
		nodeTable.createColumn("Score", Double.class, false);
		nodeTable.createColumn("IsSpecial", Boolean.class, false);
		for (int i = 0; i < TOTAL_NODES; i++) {
			CyNode node = network.addNode();
			network.getRow(node).set("Score", random.nextGaussian());
			network.getRow(node).set("IsSpecial", i % 2 == 0);
		}
		
		List<CyNode> nodes = network.getNodeList();
		for (int i = 0; i < TOTAL_EDGES; i++) {
			CyNode source = nodes.get(random.nextInt(nodes.size()));
			CyNode target = nodes.get(random.nextInt(nodes.size()));
			network.addEdge(source, target, false);
		}
		
		when(applicationManager.getCurrentNetworkView()).thenReturn(networkView);
		when(applicationManager.getCurrentNetwork()).thenReturn(network);
		
		when(networkView.getModel()).thenReturn(network);
		
		when(serviceRegistrar.getService(CyApplicationManager.class)).thenReturn(applicationManager);
		when(serviceRegistrar.getService(TaskManager.class)).thenReturn(taskManager);
		when(serviceRegistrar.getService(IconManager.class)).thenReturn(iconManager);
		
		JFrame frame = new JFrame();
		frame.setTitle("Select");
		
		Map<String, String> properties = Collections.emptyMap();
		TransformerManagerImpl transformerManager = new TransformerManagerImpl();
		transformerManager.registerFilterFactory(new ColumnFilterFactory(), properties);
		transformerManager.registerFilterFactory(new DegreeFilterFactory(), properties);
		transformerManager.registerFilterFactory(new TopologyFilterFactory(), properties);
		
		transformerManager.registerElementTransformerFactory(new InteractionTransformerFactory(), properties);

		ModelMonitor modelMonitor = new ModelMonitor();
		modelMonitor.handleEvent(new SetCurrentNetworkEvent(applicationManager, network));
		ValidationManager validationManager = new ValidationManager();
		
		FilterPanelStyle style = new StandardStyle();
		TransformerViewManager transformerViewManager = new TransformerViewManager(transformerManager);
//		transformerViewManager.registerTransformerViewFactory(new ColumnFilterViewFactory(style, modelMonitor), properties);
		transformerViewManager.registerTransformerViewFactory(new DegreeFilterViewFactory(style, modelMonitor), properties);
		transformerViewManager.registerTransformerViewFactory(new TopologyFilterViewFactory(style), properties);
		transformerViewManager.registerTransformerViewFactory(new InteractionTransformerViewFactory(style), properties);

		LazyWorkQueue queue = new LazyWorkQueue();
		
		FilterWorker filterWorker = new FilterWorker(queue, serviceRegistrar);
		FilterIO filterIo = null;
		FilterPanelController filterPanelController = new FilterPanelController(transformerManager, transformerViewManager, validationManager, filterWorker, modelMonitor, filterIo, style, serviceRegistrar);
		FilterPanel filterPanel = new FilterPanel(filterPanelController, filterWorker, serviceRegistrar);
		
		TransformerWorker transformerWorker = new TransformerWorker(queue, transformerManager, serviceRegistrar);
		TransformerPanelController transformerPanelController = new TransformerPanelController(transformerManager, transformerViewManager, validationManager, filterPanelController, transformerWorker, filterIo, style, serviceRegistrar);
		TransformerPanel transformerPanel = new TransformerPanel(transformerPanelController, transformerWorker, serviceRegistrar);
		
		SelectPanel selectPanel = new SelectPanel(filterPanel, transformerPanel);
		
		JRootPane root = frame.getRootPane();
		root.setLayout(new GridBagLayout());
		root.add(selectPanel, new GridBagConstraints(0, 0, 1, 1, 1, 1, GridBagConstraints.LINE_START, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
		frame.setSize(450, 500);
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}
	
	public static void main(String[] args) {
		new UiScaffold().start();
	}
}
