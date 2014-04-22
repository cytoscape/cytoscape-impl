package org.cytoscape.filter.internal.topology;

import org.cytoscape.filter.internal.predicates.NumericPredicateDelegate;
import org.cytoscape.filter.internal.predicates.PredicateDelegates;
import org.cytoscape.filter.model.AbstractTransformer;
import org.cytoscape.filter.model.Filter;
import org.cytoscape.filter.predicates.Predicate;
import org.cytoscape.filter.transformers.Transformers;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyEdge.Type;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.work.Tunable;

import cern.colt.map.tlong.OpenLongIntHashMap;

public class TopologyFilter  extends AbstractTransformer<CyNetwork, CyIdentifiable> implements Filter<CyNetwork, CyIdentifiable> {
	private Integer distance;
	private Integer threshold;
	private Predicate predicate;
	private NumericPredicateDelegate delegate;

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
	public Predicate getPredicate() {
		return predicate;
	}
	
	public void setPredicate(Predicate predicate) {
		this.predicate = predicate;
		this.delegate = PredicateDelegates.getNumericDelegate(predicate);
		notifyListeners();
	}
	
	@Override
	public String getName() {
		return "Topology Filter";
	}

	@Override
	public String getId() {
		return Transformers.TOPOLOGY_FILTER;
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
	public boolean accepts(CyNetwork network, CyIdentifiable element) {
		if (!(element instanceof CyNode)) {
			return false;
		}
		
		if (distance == null || threshold == null) {
			return false;
		}
		
		OpenLongIntHashMap seen = new OpenLongIntHashMap();
		countNeighbours(network, (CyNode) element, distance, seen);
		
		seen.removeKey(element.getSUID());
		int count = seen.size();
		
		return delegate.accepts(threshold, threshold, count);
	}

	private static void countNeighbours(CyNetwork network, CyNode node, int distance, OpenLongIntHashMap seen) {
		if (distance == 0) {
			seen.put(node.getSUID(), 1);
			return;
		}
		
		for (CyEdge edge : network.getAdjacentEdgeIterable(node, Type.ANY)) {
			CyNode source = edge.getSource();
			CyNode target = edge.getTarget();
			if (source == node && target == node) {
				// Self edge
				seen.put(node.getSUID(), 1);
				countNeighbours(network, node, distance - 1, seen);
			} else if (source == node) {
				seen.put(target.getSUID(), 1);
				countNeighbours(network, target, distance - 1, seen);
			} else {
				seen.put(source.getSUID(), 1);
				countNeighbours(network, source, distance - 1, seen);
			}
		}
	}
}
