package org.cytoscape.task.internal.select;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashSet;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.presentation.annotations.Annotation;
import org.cytoscape.view.presentation.annotations.AnnotationManager;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.undo.UndoSupport;

/*
 * #%L
 * Cytoscape Core Task Impl (core-task-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2010 - 2021 The Cytoscape Consortium
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

public class AbstractSelectTaskTester {

    protected CyEventHelper eventHelper;
    protected CyNetworkViewManager networkViewManager;
    protected UndoSupport undoSupport;
    protected CyApplicationManager applicationManager;
    protected AnnotationManager annotationManager;
    protected CyServiceRegistrar serviceRegistrar;
    
	TaskMonitor tm;
	CyTable nodeTable;
	CyTable edgeTable;
	CyNetwork net;
	CyRow r1;
	CyEdge e1;
	CyRow r2;
	CyEdge e2;
	CyRow r3;
	CyNode e3;
	CyRow r4;
	CyNode e4;
	Annotation a1;
	Annotation a2;
	Annotation a3;
	Annotation a4;

	public void setUp() throws Exception {
		eventHelper = mock(CyEventHelper.class);
		undoSupport = mock(UndoSupport.class);
		applicationManager = mock(CyApplicationManager.class);
		networkViewManager = mock(CyNetworkViewManager.class);
		annotationManager = mock(AnnotationManager.class);
		serviceRegistrar = mock(CyServiceRegistrar.class);
		when(serviceRegistrar.getService(CyEventHelper.class)).thenReturn(eventHelper);
		when(serviceRegistrar.getService(CyNetworkViewManager.class)).thenReturn(networkViewManager);
		when(serviceRegistrar.getService(UndoSupport.class)).thenReturn(undoSupport);
		when(serviceRegistrar.getService(CyApplicationManager.class)).thenReturn(applicationManager);
		when(serviceRegistrar.getService(AnnotationManager.class)).thenReturn(annotationManager);
		
		nodeTable = mock(CyTable.class);
		edgeTable = mock(CyTable.class);
		net = mock(CyNetwork.class);
		when(net.getDefaultNodeTable()).thenReturn(nodeTable);
		when(net.getDefaultEdgeTable()).thenReturn(edgeTable);

		var view = mock(CyNetworkView.class);
		when(view.getModel()).thenReturn(net);

		var views = new HashSet<CyNetworkView>();
		views.add(view);
		when(networkViewManager.getNetworkViews(any(CyNetwork.class))).thenReturn(views);

		tm = mock(TaskMonitor.class);

		r1 = mock(CyRow.class);
		when(r1.getTable()).thenReturn(edgeTable);
		e1 = mock(CyEdge.class);
		when(net.getRow(e1)).thenReturn(r1);
		when(r1.get(CyNetwork.SUID, Long.class)).thenReturn(1L);
		when(e1.getSUID()).thenReturn(1L);
		when(net.getEdge(1L)).thenReturn(e1);
		when(edgeTable.getRow(1L)).thenReturn(r1);

		r2 = mock(CyRow.class);
		when(r2.getTable()).thenReturn(edgeTable);
		e2 = mock(CyEdge.class);
		when(net.getRow(e2)).thenReturn(r2);
		when(r2.get(CyNetwork.SUID, Long.class)).thenReturn(2L);
		when(e2.getSUID()).thenReturn(2L);
		when(net.getEdge(2L)).thenReturn(e2);
		when(edgeTable.getRow(2L)).thenReturn(r2);

		var el = new ArrayList<CyEdge>();
		el.add(e1);
		el.add(e2);
		when(net.getEdgeList()).thenReturn(el);

		r3 = mock(CyRow.class);
		when(r3.getTable()).thenReturn(nodeTable);
		e3 = mock(CyNode.class);
		when(net.getRow(e3)).thenReturn(r3);
		when(r3.get(CyNetwork.SUID, Long.class)).thenReturn(3L);
		when(e3.getSUID()).thenReturn(3L);
		when(net.getNode(3L)).thenReturn(e3);
		when(nodeTable.getRow(3L)).thenReturn(r3);

		r4 = mock(CyRow.class);
		when(r4.getTable()).thenReturn(nodeTable);
		e4 = mock(CyNode.class);
		when(net.getRow(e4)).thenReturn(r4);
		when(r4.get(CyNetwork.SUID, Long.class)).thenReturn(4L);
		when(e4.getSUID()).thenReturn(4L);
		when(net.getNode(4L)).thenReturn(e4);
		when(nodeTable.getRow(4L)).thenReturn(r4);

		var nl = new ArrayList<CyNode>();
		nl.add(e3);
		nl.add(e4);
		when(net.getNodeList()).thenReturn(nl);
		
		// Annotations
		a1 = mock(Annotation.class);
		a2 = mock(Annotation.class);
		a3 = mock(Annotation.class);
		when(a3.isSelected()).thenReturn(true);
		a4 = mock(Annotation.class);
		when(a4.isSelected()).thenReturn(true);
		
		var al1 = new ArrayList<Annotation>(); // all annotations
		al1.add(a1);
		al1.add(a2);
		al1.add(a3);
		al1.add(a4);
		when(annotationManager.getAnnotations(view)).thenReturn(al1);
		
		var al2 = new ArrayList<Annotation>(); // selected annotations
		al2.add(a3);
		al2.add(a4);
		when(annotationManager.getSelectedAnnotations(view)).thenReturn(al2);
	}
}
