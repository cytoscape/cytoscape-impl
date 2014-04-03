package org.cytoscape.filter.internal.topology;

import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.Map;

import org.cytoscape.filter.internal.view.FilterElement;
import org.cytoscape.filter.internal.view.FilterPanelController;
import org.cytoscape.filter.model.AbstractTransformer;
import org.cytoscape.filter.model.Filter;
import org.cytoscape.filter.model.HolisticTransformer;
import org.cytoscape.filter.model.TransformerSink;
import org.cytoscape.filter.model.TransformerSource;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyEdge.Type;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.work.Tunable;

public class TopologyTransformer  extends AbstractTransformer<CyNetwork, CyIdentifiable> implements HolisticTransformer<CyNetwork, CyIdentifiable> {
	static final String ID = "org.cytoscape.TopologyTransformer";
	
	private Integer distance;
	private Integer threshold;
	private String filterName;

	private FilterPanelController filterPanelController;

	public TopologyTransformer(FilterPanelController filterPanelController) {
		this.filterPanelController = filterPanelController;
	}

	@Tunable
	public Integer getDistance() {
		return distance;
	}
	
	public void setDistance(Integer distance) {
		this.distance = distance;
		notifyListeners();
	}
	
	@Tunable
	public Integer getThreshold() {
		return threshold;
	}
	
	public void setThreshold(Integer threshold) {
		this.threshold = threshold;
		notifyListeners();
	}
	
	@Tunable
	public String getFilterName() {
		return filterName;
	}
	
	public void setFilterName(String name) {
		filterName = name;
		notifyListeners();
	}
	
	@Override
	public String getName() {
		return "Topology Transformer";
	}

	@Override
	public String getId() {
		return ID;
	}

	@Override
	public Class<CyNetwork> getContextType() {
		return CyNetwork.class;
	}

	@Override
	public Class<CyIdentifiable> getElementType() {
		return CyIdentifiable.class;
	}

	@Override
	public void apply(CyNetwork network,
			TransformerSource<CyNetwork, CyIdentifiable> source,
			TransformerSink<CyIdentifiable> sink) {

		Filter<CyNetwork, CyIdentifiable> filter = null;
		if (filterName != null) {
			FilterElement element = filterPanelController.getElementByName(filterName);
			if (element != null) {
				filter = element.filter;
			}
		}
		
		Map<CyNode, Object> results = new IdentityHashMap<CyNode, Object>();
		Map<CyNode, Object> seen = new IdentityHashMap<CyNode, Object>();
		for (CyIdentifiable element: source.getElementList(network)) {
			try {
				if (!(element instanceof CyNode)) {
					continue;
				}
				
				if (distance == null || threshold == null) {
					continue;
				}
				
				// Fetch the whole neighbourhood
				getNeighbours(network, (CyNode) element, distance, seen);
				seen.remove(element);
				
				if (filter != null) {
					// Remove any nodes that don't match the filter
					Iterator<CyNode> iterator = seen.keySet().iterator();
					while (iterator.hasNext()) {
						CyNode node = iterator.next();
						if (!filter.accepts(network, node)) {
							iterator.remove();
						}
					}
				}
				if (seen.size() < threshold) {
					continue;
				}
				
				results.putAll(seen);
			} finally {
				seen.clear();
			}
		}
		
		for (CyNode node : results.keySet()) {
			sink.collect(node);
		}
	}
	
	private static void getNeighbours(CyNetwork network, CyNode node, int distance, Map<CyNode, Object> seen) {
		if (distance < 0 || seen.containsKey(node)) {
			return;
		}
		
		seen.put(node, null);
		for (CyEdge edge : network.getAdjacentEdgeIterable(node, Type.ANY)) {
			getNeighbours(network, edge.getTarget(), distance - 1, seen);
		}
	}
}
