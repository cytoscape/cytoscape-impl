package org.cytoscape.filter.internal.work;

import static org.cytoscape.view.presentation.property.BasicVisualLexicon.EDGE_VISIBLE;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NODE_VISIBLE;

import java.util.ArrayList;
import java.util.List;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.filter.internal.MemoizableTransformer;
import org.cytoscape.filter.internal.view.FilterElement;
import org.cytoscape.filter.internal.view.TransformerPanel;
import org.cytoscape.filter.internal.view.TransformerPanelController;
import org.cytoscape.filter.model.CompositeFilter;
import org.cytoscape.filter.model.Transformer;
import org.cytoscape.filter.model.TransformerSink;
import org.cytoscape.filter.model.TransformerSource;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.task.hide.HideTaskFactory;
import org.cytoscape.task.hide.UnHideTaskFactory;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.work.SynchronousTaskManager;
import org.cytoscape.work.TaskIterator;

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

public class TransformerWorker extends AbstractWorker<TransformerPanel, TransformerPanelController> {
	
	// TODO add progress monitoring to the TransformerManager API, must use impl for now
	private TransformerManagerImpl transformerManager;
	
	public TransformerWorker(LazyWorkQueue queue, TransformerManagerImpl transformerManager,
			final CyServiceRegistrar serviceRegistrar) {
		super(queue, serviceRegistrar);
		this.transformerManager = transformerManager;
	}
	
	@Override
	public void doWork(ProgressMonitor monitor) {
		if (controller == null) {
			return;
		}
		
		final CyApplicationManager applicationManager = serviceRegistrar.getService(CyApplicationManager.class);
		CyNetworkView networkView = applicationManager.getCurrentNetworkView();
		CyNetwork network;
		
		if (networkView != null) {
			network = networkView.getModel();
		} else {
			network = applicationManager.getCurrentNetwork();
		}
		
		if (network == null) {
			return;
		}
		
		ProgressMonitor filterMonitor = new SubProgressMonitor(monitor, 0.0, 0.4);
		ProgressMonitor chainMonitor  = new SubProgressMonitor(monitor, 0.4, 0.98);
		
		monitor.setProgress(0.0);
		monitor.setStatusMessage(null);
		
		Sink sink;
		if(applyAction == ApplyAction.SHOW) {
			sink = new ShowSink(networkView);
		} else {
			sink = new SelectSink(network);
		}
		
		long startTime = System.currentTimeMillis();
		try {
			List<Transformer<CyNetwork, CyIdentifiable>> transformers = controller.getTransformers(view);
			FilterElement selected = (FilterElement) controller.getStartWithComboBoxModel().getSelectedItem();
			TransformerSource<CyNetwork, CyIdentifiable> source = createSource(network, selected, filterMonitor);
			
			for(Transformer<?,?> transformer : transformers) {
				if(transformer instanceof MemoizableTransformer) {
					((MemoizableTransformer) transformer).startCaching();
				}
			}
			try {
				transformerManager.execute(network, source, transformers, sink, chainMonitor);
			}
			finally {
				for(Transformer<?,?> transformer : transformers) {
					if(transformer instanceof MemoizableTransformer) {
						((MemoizableTransformer) transformer).clearCache();
					}
				}
			}
			
			sink.done();
			
			if (networkView != null) {
				networkView.updateView();
			}
		} finally {
			long duration = System.currentTimeMillis() - startTime;
			monitor.setProgress(1.0);
			monitor.setStatusMessage(String.format("Selected %d %s and %d %s in %dms",
					sink.getNodeCount(),
					sink.getNodeCount() == 1 ? "node" : "nodes",
					sink.getEdgeCount(),
					sink.getEdgeCount() == 1 ? "edge" : "edges",
					duration));
		}
	}

	private TransformerSource<CyNetwork, CyIdentifiable> createSource(CyNetwork network, FilterElement selected, ProgressMonitor monitor) {
		// The progress monitor should really be passed to getElementList(), but oh well
		if (selected.getFilter() == null) {
			return new SelectionSource(monitor);
		} else {
			return new FilterSource(selected.getFilter(), applyAction, monitor);
		}
	}
	
	private static abstract class AbstractSource implements TransformerSource<CyNetwork, CyIdentifiable> {
		@Override
		public Class<CyNetwork> getContextType() {
			return CyNetwork.class;
		}
		
		@Override
		public Class<CyIdentifiable> getElementType() {
			return CyIdentifiable.class;
		}

		@Override
		public int getElementCount(CyNetwork context) {
			// Just a hint
			return context.getNodeCount() + context.getEdgeCount();
		}
	}
	
	private static class SelectionSource extends AbstractSource {
		
		private ProgressMonitor monitor;
		
		SelectionSource(ProgressMonitor monitor) {
			this.monitor = monitor;
		}
		
		@Override
		public List<CyIdentifiable> getElementList(CyNetwork context) {
			monitor.start();
			
			int maximum = getElementCount(context);
			ArrayList<CyIdentifiable> elements = new ArrayList<CyIdentifiable>(maximum);
			DiscreteProgressMonitor discreteMonitor = new DiscreteProgressMonitor(monitor);
			discreteMonitor.setTotalWork(maximum);
			
			// Clear selection state while collecting elements
			List<CyNode> nodes = context.getNodeList();
			
			for (CyNode node : nodes) {
				CyRow row = context.getRow(node);
				if (row.get(CyNetwork.SELECTED, Boolean.class)) {
					row.set(CyNetwork.SELECTED, false);
					elements.add(node);
				}
				discreteMonitor.addWork(1);
			}
			
			List<CyEdge> edges = context.getEdgeList();
			for (CyEdge edge : edges) {
				CyRow row = context.getRow(edge);
				if (row.get(CyNetwork.SELECTED, Boolean.class)) {
					row.set(CyNetwork.SELECTED, false);
					elements.add(edge);
				}
				discreteMonitor.addWork(1);
			}
			
			monitor.done();
			return elements;
		}
	}
	
	private static class FilterSource extends AbstractSource {
		private final ApplyAction applyAction;
		private final CompositeFilter<CyNetwork, CyIdentifiable> filter;
		private final ProgressMonitor monitor;

		FilterSource(CompositeFilter<CyNetwork, CyIdentifiable> filter, ApplyAction applyAction, ProgressMonitor monitor) {
			this.filter = filter;
			this.monitor = monitor;
			this.applyAction = applyAction;
		}
		
		@Override
		public List<CyIdentifiable> getElementList(CyNetwork context) {
			monitor.start();
			monitor.setStatusMessage("Filtering");
			
			int maximum = getElementCount(context);
			ArrayList<CyIdentifiable> elements = new ArrayList<CyIdentifiable>(maximum);
			DiscreteProgressMonitor discreteMonitor = new DiscreteProgressMonitor(monitor);
			discreteMonitor.setTotalWork(maximum);
			
			if(filter instanceof MemoizableTransformer) {
				((MemoizableTransformer) filter).startCaching();
			}
			try {
				// Clear selection state while collecting elements
				for (CyNode node : context.getNodeList()) {
					
					boolean accepted;
					if(applyAction == ApplyAction.SHOW) {
						accepted = !filter.appliesTo(context, node) || filter.accepts(context, node); 
					} else {
						CyRow row = context.getRow(node);
						if (row.get(CyNetwork.SELECTED, Boolean.class)) {
							row.set(CyNetwork.SELECTED, false);
						}
						accepted = filter.accepts(context, node);
					}
					if (accepted) {
						elements.add(node);
					}
					discreteMonitor.addWork(1);
				}
				for (CyEdge edge : context.getEdgeList()) {
					
					boolean accepted;
					if(applyAction == ApplyAction.SHOW) {
						accepted = !filter.appliesTo(context, edge) || filter.accepts(context, edge); 
					} else {
						CyRow row = context.getRow(edge);
						if (row.get(CyNetwork.SELECTED, Boolean.class)) {
							row.set(CyNetwork.SELECTED, false);
						}
						accepted = filter.accepts(context, edge);
					}
					if (accepted) {
						elements.add(edge);
					}
					discreteMonitor.addWork(1);
				}
			}
			finally {
				if(filter instanceof MemoizableTransformer) {
					((MemoizableTransformer) filter).clearCache();
				}
			}
			
			monitor.done();
			return elements;
		}
	}
	
	private abstract class Sink implements TransformerSink<CyIdentifiable> {
		final CyNetwork network;
		int nodeCount;
		int edgeCount;
		
		Sink(CyNetwork network) {
			this.network = network;
		}
		
		@Override
		public void collect(CyIdentifiable element) {
			if (element instanceof CyNode) {
				nodeCount++;
			} else if (element instanceof CyEdge) {
				edgeCount++;
			}
		}
		
		public int getNodeCount() {
			return nodeCount;
		}
		
		public int getEdgeCount() {
			return edgeCount;
		}
		
		void done() {}
	}
	
	
	class SelectSink extends Sink {
		
		SelectSink(CyNetwork network) {
			super(network);
		}
		
		@Override
		public void collect(CyIdentifiable element) {
			super.collect(element);
			network.getRow(element).set(CyNetwork.SELECTED, true);
		}
		
	}
	
	
	class ShowSink extends Sink {

		private final CyNetworkView networkView;
		private List<CyNode> selectedNodes = new ArrayList<>();
		private List<CyEdge> selectedEdges = new ArrayList<>();
		
		
		ShowSink(CyNetworkView networkView) {
			super(networkView.getModel());
			this.networkView = networkView;
		}
		
		@Override
		public void collect(CyIdentifiable element) {
			super.collect(element);
			if (element instanceof CyNode) {
				selectedNodes.add((CyNode)element);
			} else if (element instanceof CyEdge) {
				selectedEdges.add((CyEdge)element);
			}
		}
		
		@Override
		public void done() {
			if(networkView != null) {
				for(CyNode node : selectedNodes) {
					networkView.getNodeView(node).clearValueLock(NODE_VISIBLE);
				}
				for(CyNode node : unselectedNodes) {
					networkView.getNodeView(node).setLockedValue(NODE_VISIBLE, false);
				}
				for(CyEdge edge : selectedEdges) {
					networkView.getEdgeView(edge).clearValueLock(EDGE_VISIBLE);
				}
				for(CyEdge edge : unselectedEdges) {
					networkView.getEdgeView(edge).setLockedValue(EDGE_VISIBLE, false);
				}
				
				HideTaskFactory hideFactory = serviceRegistrar.getService(HideTaskFactory.class);
				TaskIterator hideTasks = hideFactory.createTaskIterator(networkView, network.getNodeList(), network.getEdgeList());
				
				UnHideTaskFactory unhideFactory = serviceRegistrar.getService(UnHideTaskFactory.class);
				TaskIterator unhideTasks = unhideFactory.createTaskIterator(networkView, selectedNodes, selectedEdges);
				
				TaskIterator taskIterator = new TaskIterator();
				taskIterator.append(hideTasks);
				taskIterator.append(unhideTasks);
				
				SynchronousTaskManager<?> taskManager = serviceRegistrar.getService(SynchronousTaskManager.class);
				taskManager.execute(taskIterator);
			}
		}
	}
}
