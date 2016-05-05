package org.cytoscape.filter.internal.work;

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
import org.cytoscape.view.model.CyNetworkView;

public class TransformerWorker extends AbstractWorker<TransformerPanel, TransformerPanelController> {
	
	// TODO add progress monitoring to the TransformerManager API, must use impl for now
	private TransformerManagerImpl transformerManager;
	
	public TransformerWorker(LazyWorkQueue queue, CyApplicationManager applicationManager, TransformerManagerImpl transformerManager) {
		super(queue, applicationManager);
		this.transformerManager = transformerManager;
		
	}
	
	@Override
	public void doWork(ProgressMonitor monitor) {
		if (controller == null) {
			return;
		}
		
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
		
		Sink sink = new Sink(network);
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
			
			if (networkView != null) {
				networkView.updateView();
			}
		} finally {
			long duration = System.currentTimeMillis() - startTime;
			monitor.setProgress(1.0);
			monitor.setStatusMessage(String.format("Selected %d %s and %d %s in %dms",
					sink.nodeCount,
					sink.nodeCount == 1 ? "node" : "nodes",
					sink.edgeCount,
					sink.edgeCount == 1 ? "edge" : "edges",
					duration));
		}
	}

	private TransformerSource<CyNetwork, CyIdentifiable> createSource(CyNetwork network, FilterElement selected, ProgressMonitor monitor) {
		// The progress monitor should really be passed to getElementList(), but oh well
		if (selected.getFilter() == null) {
			return new SelectionSource(monitor);
		} else {
			return new FilterSource(selected.getFilter(), monitor);
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
			ArrayList<CyIdentifiable> elements = new ArrayList<>(maximum);
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
		private CompositeFilter<CyNetwork, CyIdentifiable> filter;
		private ProgressMonitor monitor;

		FilterSource(CompositeFilter<CyNetwork, CyIdentifiable> filter, ProgressMonitor monitor) {
			this.filter = filter;
			this.monitor = monitor;
		}
		
		@Override
		public List<CyIdentifiable> getElementList(CyNetwork context) {
			monitor.start();
			monitor.setStatusMessage("Filtering");
			
			int maximum = getElementCount(context);
			ArrayList<CyIdentifiable> elements = new ArrayList<>(maximum);
			DiscreteProgressMonitor discreteMonitor = new DiscreteProgressMonitor(monitor);
			discreteMonitor.setTotalWork(maximum);
			
			if(filter instanceof MemoizableTransformer) {
				((MemoizableTransformer) filter).startCaching();
			}
			try {
				// Clear selection state while collecting elements
				for (CyNode node : context.getNodeList()) {
					CyRow row = context.getRow(node);
					if (row.get(CyNetwork.SELECTED, Boolean.class)) {
						row.set(CyNetwork.SELECTED, false);
					}
					if (filter.accepts(context, node)) {
						elements.add(node);
					}
					discreteMonitor.addWork(1);
				}
				for (CyEdge edge : context.getEdgeList()) {
					CyRow row = context.getRow(edge);
					if (row.get(CyNetwork.SELECTED, Boolean.class)) {
						row.set(CyNetwork.SELECTED, false);
					}
					if (filter.accepts(context, edge)) {
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
	
	static class Sink implements TransformerSink<CyIdentifiable> {
		private final CyNetwork network;
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
			
			network.getRow(element).set(CyNetwork.SELECTED, true);
		}
		
	}
}
