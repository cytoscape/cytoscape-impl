package org.cytoscape.view.layout;

/*
 * #%L
 * Cytoscape Layout Impl (layout-impl)
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

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.cytoscape.ding.NetworkViewTestSupport;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.util.ListSingleSelection;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

public abstract class AbstractLayoutTaskTest {
	
	protected AbstractLayoutTask task;
	@Mock private TaskMonitor taskMonitor;
	
	protected String name;
	
	protected Set<Class<?>> supportedNodeAttributeTypes = new HashSet<>();
	protected Set<Class<?>> supportedEdgeAttributeTypes = new HashSet<>();
	protected List<String> initialAttributes = new ArrayList<>();
	
	final NetworkViewTestSupport support = new NetworkViewTestSupport();
	final CyNetwork network = support.getNetworkFactory().createNetwork();
	CyNode source = network.addNode();
	CyNode target = network.addNode();
	CyEdge edge = network.addEdge(source, target, true);
	protected Set<View<CyNode>> nodesToLayOut = new HashSet<>();
	
	protected final CyNetworkView networkView = support.getNetworkViewFactory().createNetworkView(network);

	@Test
	public void testRun() throws Exception {
		task.run(taskMonitor);
	}

	@Test
	public abstract void testAbstractLayoutTaskConstructor();

	@Test
	public abstract void testDoLayout();

}
