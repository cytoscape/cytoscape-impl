package org.cytoscape.view;

import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.cytoscape.ding.impl.DGraphView;
import org.cytoscape.ding.impl.DingGraphLOD;
import org.cytoscape.ding.impl.ViewTaskFactoryListener;
import org.cytoscape.ding.impl.cyannotator.AnnotationFactoryManager;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.NetworkTestSupport;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.spacial.SpacialIndex2D;
import org.cytoscape.spacial.SpacialIndex2DFactory;
import org.cytoscape.task.EdgeViewTaskFactory;
import org.cytoscape.task.NetworkViewTaskFactory;
import org.cytoscape.task.NodeViewTaskFactory;
import org.cytoscape.util.swing.IconManager;
import org.cytoscape.view.model.AbstractCyNetworkViewTest;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.model.VisualLexicon;
import org.cytoscape.view.presentation.property.values.HandleFactory;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.work.swing.DialogTaskManager;
import org.cytoscape.work.undo.UndoSupport;
import org.junit.Before;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/*
 * #%L
 * Cytoscape Ding View/Presentation Impl (ding-presentation-impl)
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

public class DNetworkViewTest extends AbstractCyNetworkViewTest {

	@Mock
	private UndoSupport undoSupport;
	@Mock
	private VisualLexicon dingLexicon;
	@Mock
	private ViewTaskFactoryListener vtfListener;
	@Mock
	private Map<NodeViewTaskFactory, Map> nodeViewTFs;
	@Mock
	private Map<EdgeViewTaskFactory, Map> edgeViewTFs;
	@Mock
	private Map<NetworkViewTaskFactory, Map> emptySpaceTFs;
	@Mock
	private DialogTaskManager dialogTaskManager;
	@Mock
	private CyEventHelper eventHelper;
	@Mock
	private IconManager iconManager;
	@Mock
	private AnnotationFactoryManager annotationFactoryManager;
	@Mock
	private DingGraphLOD dingGraphLOD;
	@Mock
	private VisualMappingManager visualMappingManager;
	@Mock
	private CyNetworkViewManager networkViewManager;
	@Mock
	private HandleFactory handleFactory;
	@Mock
	private SpacialIndex2D spacialIndex2D;
	@Mock
	private SpacialIndex2DFactory spacialFactory;
	@Mock
	private CyServiceRegistrar serviceRegistrar;
	
	private final NetworkTestSupport netSupport = new NetworkTestSupport();

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		
		when(spacialFactory.createSpacialIndex2D()).thenReturn(spacialIndex2D);
		
		when(serviceRegistrar.getService(UndoSupport.class)).thenReturn(undoSupport);
		when(serviceRegistrar.getService(SpacialIndex2DFactory.class)).thenReturn(spacialFactory);
		when(serviceRegistrar.getService(DialogTaskManager.class)).thenReturn(dialogTaskManager);
		when(serviceRegistrar.getService(CyEventHelper.class)).thenReturn(eventHelper);
		when(serviceRegistrar.getService(IconManager.class)).thenReturn(iconManager);
		when(serviceRegistrar.getService(VisualMappingManager.class)).thenReturn(visualMappingManager);
		when(serviceRegistrar.getService(CyNetworkViewManager.class)).thenReturn(networkViewManager);
		
		buildNetwork();
		view = new DGraphView(network, dingLexicon, vtfListener, annotationFactoryManager, dingGraphLOD, handleFactory,
				serviceRegistrar);
	}
	
	@Override
	public void buildNetwork() {
		network = netSupport.getNetwork();
		
		node1 = network.addNode();
		node2 = network.addNode();
		node3 = network.addNode();
		node4 = network.addNode();
		node5 = network.addNode();

		List<CyNode> nl = new ArrayList<CyNode>();
		nl.add(node1);
		nl.add(node2);
		nl.add(node3);
		nl.add(node4);
		nl.add(node5);

		edge1 = network.addEdge(node1, node2, true);
		edge2 = network.addEdge(node1, node3, true);
		edge3 = network.addEdge(node1, node4, true);
		edge4 = network.addEdge(node2, node3, true);
		edge5 = network.addEdge(node1, node5, true);
		edge6 = network.addEdge(node3, node4, true);
		edge7 = network.addEdge(node4, node5, true);
		edge8 = network.addEdge(node2, node5, true);

		List<CyEdge> el = new ArrayList<CyEdge>();
		el.add(edge1);
		el.add(edge2);
		el.add(edge3);
		el.add(edge4);
		el.add(edge5);
		el.add(edge6);
		el.add(edge7);
		el.add(edge8);
	}
}
