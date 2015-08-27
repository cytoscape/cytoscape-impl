package org.cytoscape.filter.internal.topology;

import org.cytoscape.filter.internal.composite.CompositeFilterImpl;
import org.cytoscape.filter.internal.predicates.NumericPredicateDelegate;
import org.cytoscape.filter.internal.predicates.PredicateDelegates;
import org.cytoscape.filter.model.AbstractTransformer;
import org.cytoscape.filter.model.CompositeFilter;
import org.cytoscape.filter.model.Filter;
import org.cytoscape.filter.model.NegatableFilter;
import org.cytoscape.filter.predicates.Predicate;
import org.cytoscape.filter.transformers.Transformers;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.work.Tunable;

import cern.colt.map.tlong.OpenLongIntHashMap;

public class TopologyFilter extends AbstractTransformer<CyNetwork,CyIdentifiable> implements CompositeFilter<CyNetwork,CyIdentifiable>, NegatableFilter {
	private Integer distance;
	private Integer threshold;
	private Predicate predicate;
	private NumericPredicateDelegate delegate;
	private boolean negated;
	
	// Internally use a compositeFilter to store child filters
	private final CompositeFilter<CyNetwork,CyIdentifiable> neighbourFilter;

	public TopologyFilter() {
		neighbourFilter = new CompositeFilterImpl<>(CyNetwork.class,CyIdentifiable.class);
		neighbourFilter.setType(CompositeFilter.Type.ALL); // ALL accepts if empty
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
		return negated ^ acceptsImpl(network, element);
	}
	
	private boolean acceptsImpl(CyNetwork network, CyIdentifiable element) {
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

	private void countNeighbours(CyNetwork network, CyNode node, int distance, OpenLongIntHashMap seen) {
		if (distance == 0) {
			mark(seen, network, node);
			return;
		}
		
		for (CyEdge edge : network.getAdjacentEdgeIterable(node, CyEdge.Type.ANY)) {
			CyNode source = edge.getSource();
			CyNode target = edge.getTarget();
			if (source == node && target == node) {
				// Self edge
				mark(seen, network, node);
				countNeighbours(network, node, distance - 1, seen);
			} else if (source == node) {
				mark(seen, network, target);
				countNeighbours(network, target, distance - 1, seen);
			} else {
				mark(seen, network, source);
				countNeighbours(network, source, distance - 1, seen);
			}
		}
	}

	private void mark(OpenLongIntHashMap seen, CyNetwork network, CyNode node) {
		if(neighbourFilter.accepts(network, node)) {
			seen.put(node.getSUID(), 1);
		}
	}
	
	@Override
	public void append(Filter<CyNetwork, CyIdentifiable> filter) {
		neighbourFilter.append(filter);
		notifyListeners();
	}

	@Override
	public void insert(int index, Filter<CyNetwork, CyIdentifiable> filter) {
		neighbourFilter.insert(index, filter);
		notifyListeners();
	}

	@Override
	public Filter<CyNetwork, CyIdentifiable> get(int index) {
		return neighbourFilter.get(index);
	}

	@Override
	public Filter<CyNetwork, CyIdentifiable> remove(int index) {
		try {
			return neighbourFilter.remove(index);
		} finally {
			notifyListeners();
		}
	}

	@Override
	public int indexOf(Filter<CyNetwork, CyIdentifiable> filter) {
		return neighbourFilter.indexOf(filter);
	}

	@Override
	public int getLength() {
		return neighbourFilter.getLength();
	}

	@Override
	@Tunable
	public boolean getNegated() {
		return negated;
	}

	@Override
	public void setNegated(boolean negated) {
		this.negated = negated;
		notifyListeners();
	}

	@Override
	@Tunable
	public CompositeFilter.Type getType() {
		return neighbourFilter.getType();
	}

	@Override
	public void setType(CompositeFilter.Type type) {
		neighbourFilter.setType(type);
		notifyListeners();
	}


}
