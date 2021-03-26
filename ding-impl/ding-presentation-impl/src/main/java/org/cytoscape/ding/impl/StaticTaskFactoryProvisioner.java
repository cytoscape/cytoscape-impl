package org.cytoscape.ding.impl;

import java.awt.geom.Point2D;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNode;
import org.cytoscape.task.EdgeViewTaskFactory;
import org.cytoscape.task.NetworkViewLocationTaskFactory;
import org.cytoscape.task.NetworkViewTaskFactory;
import org.cytoscape.task.NodeViewTaskFactory;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.Togglable;

/*
 * #%L
 * Cytoscape Ding View/Presentation Impl (ding-presentation-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2021 The Cytoscape Consortium
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

public class StaticTaskFactoryProvisioner {
	
	public NamedTaskFactory createFor(NetworkViewTaskFactory factory, CyNetworkView networkView) {
		return factory instanceof Togglable ?
				new DynamicTogglableTaskFactory(factory, networkView) :
				new DynamicTaskFactory(factory, networkView);
	}

	public NamedTaskFactory createFor(NetworkViewLocationTaskFactory factory, CyNetworkView networkView, Point2D point,
			Point2D transformedPoint) {
		return factory instanceof Togglable ?
				new DynamicTogglableTaskFactory(factory, networkView, point, transformedPoint) :
				new DynamicTaskFactory(factory, networkView, point, transformedPoint);
	}

	public NamedTaskFactory createFor(NodeViewTaskFactory factory, View<CyNode> nodeView, CyNetworkView networkView) {
		return factory instanceof Togglable ?
				new DynamicTogglableTaskFactory(factory, nodeView, networkView) :
				new DynamicTaskFactory(factory, nodeView, networkView);
	}

	public NamedTaskFactory createFor(EdgeViewTaskFactory factory, View<CyEdge> edgeView, CyNetworkView networkView) {
		return factory instanceof Togglable ?
				new DynamicTogglableTaskFactory(factory, edgeView, networkView) :
				new DynamicTaskFactory(factory, edgeView, networkView);
	}
	
	// ==[ CLASSES ]====================================================================================================
	
	private class DynamicTaskFactory implements NamedTaskFactory {

		protected final Object factory;
		protected Reference<CyNetworkView> netViewRef;
		protected Reference<View<CyNode>> nodeViewRef;
		protected Reference<View<CyEdge>> edgeViewRef;
		protected Point2D point;
		protected Point2D transformedPoint;

		DynamicTaskFactory(NetworkViewTaskFactory factory, CyNetworkView networkView) {
			this.factory = factory;
			this.netViewRef = new WeakReference<>(networkView);
		}
		
		DynamicTaskFactory(NetworkViewLocationTaskFactory factory, CyNetworkView networkView, Point2D point,
				Point2D transformedPoint) {
			this.factory = factory;
			this.netViewRef = new WeakReference<>(networkView);
			this.point = point;
			this.transformedPoint = transformedPoint;
		}
		
		DynamicTaskFactory(NodeViewTaskFactory factory, View<CyNode> nodeView, CyNetworkView networkView) {
			this.factory = factory;
			this.netViewRef = new WeakReference<>(networkView);
			this.nodeViewRef = new WeakReference<>(nodeView);
		}
		
		DynamicTaskFactory(EdgeViewTaskFactory factory, View<CyEdge> edgeView, CyNetworkView networkView) {
			this.factory = factory;
			this.netViewRef = new WeakReference<>(networkView);
			this.edgeViewRef = new WeakReference<>(edgeView);
		}
		
		@Override
		public TaskIterator createTaskIterator() {
			if (factory instanceof NetworkViewTaskFactory)
				return ((NetworkViewTaskFactory) factory).createTaskIterator(netViewRef.get());
			
			if (factory instanceof NetworkViewLocationTaskFactory)
				return ((NetworkViewLocationTaskFactory) factory).createTaskIterator(netViewRef.get(), point, transformedPoint);
			
			if (factory instanceof NodeViewTaskFactory)
				return ((NodeViewTaskFactory) factory).createTaskIterator(nodeViewRef.get(), netViewRef.get());
			
			if (factory instanceof EdgeViewTaskFactory)
				return ((EdgeViewTaskFactory) factory).createTaskIterator(edgeViewRef.get(), netViewRef.get());
			
			return new TaskIterator();
		}
		
		@Override
		public boolean isReady() {
			if (factory instanceof NetworkViewTaskFactory)
				return ((NetworkViewTaskFactory) factory).isReady(netViewRef.get());
			
			if (factory instanceof NetworkViewLocationTaskFactory)
				return ((NetworkViewLocationTaskFactory) factory).isReady(netViewRef.get(), point, transformedPoint);
			
			if (factory instanceof NodeViewTaskFactory)
				return ((NodeViewTaskFactory) factory).isReady(nodeViewRef.get(), netViewRef.get());
			
			if (factory instanceof EdgeViewTaskFactory)
				return ((EdgeViewTaskFactory) factory).isReady(edgeViewRef.get(), netViewRef.get());
			
			return false;
		}
		
		@Override
		public String getName() {
			return factory.getClass().getSimpleName();
		}
	}
	
	private class DynamicTogglableTaskFactory extends DynamicTaskFactory implements Togglable {
		
		DynamicTogglableTaskFactory(NetworkViewTaskFactory factory, CyNetworkView networkView) {
			super(factory, networkView);
		}
		
		DynamicTogglableTaskFactory(NetworkViewLocationTaskFactory factory, CyNetworkView networkView, Point2D point,
				Point2D transformedPoint) {
			super(factory, networkView, point, transformedPoint);
		}
		
		DynamicTogglableTaskFactory(NodeViewTaskFactory factory, View<CyNode> nodeView, CyNetworkView networkView) {
			super(factory, nodeView, networkView);
		}
		
		DynamicTogglableTaskFactory(EdgeViewTaskFactory factory, View<CyEdge> edgeView, CyNetworkView networkView) {
			super(factory, edgeView, networkView);
		}
		
		@Override
		public boolean isOn() {
			if (factory instanceof NetworkViewTaskFactory)
				return ((NetworkViewTaskFactory) factory).isOn(netViewRef.get());
			
			if (factory instanceof NetworkViewLocationTaskFactory)
				return ((NetworkViewLocationTaskFactory) factory).isOn(netViewRef.get(), point, transformedPoint);
			
			if (factory instanceof NodeViewTaskFactory)
				return ((NodeViewTaskFactory) factory).isOn(nodeViewRef.get(), netViewRef.get());
			
			if (factory instanceof EdgeViewTaskFactory)
				return ((EdgeViewTaskFactory) factory).isOn(edgeViewRef.get(), netViewRef.get());
			
			return false;
		}
	}
}
