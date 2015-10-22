package org.cytoscape.filter.internal.filters.topology;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.cytoscape.filter.internal.AbstractMemoizableTransformer;
import org.cytoscape.filter.internal.filters.composite.CompositeFilterImpl;
import org.cytoscape.filter.internal.predicates.NumericPredicateDelegate;
import org.cytoscape.filter.internal.predicates.PredicateDelegates;
import org.cytoscape.filter.model.CompositeFilter;
import org.cytoscape.filter.model.Filter;
import org.cytoscape.filter.predicates.Predicate;
import org.cytoscape.filter.transformers.Transformers;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.work.Tunable;

public class TopologyFilter extends AbstractMemoizableTransformer<CyNetwork,CyIdentifiable> 
                            implements CompositeFilter<CyNetwork,CyIdentifiable> {
	
	private Integer distance;
	private Integer threshold;
	private Predicate predicate;
	private NumericPredicateDelegate delegate;
	
	// Internally use a compositeFilter to store child filters
	private final CompositeFilter<CyNetwork,CyIdentifiable> neighbourFilter;

	public TopologyFilter() {
		neighbourFilter = new CompositeFilterImpl<>(CyNetwork.class,CyIdentifiable.class);
		neighbourFilter.setType(CompositeFilter.Type.ALL); // ALL accepts if empty
		neighbourFilter.addListener(this::notifyListeners);
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
	protected CompositeFilter<CyNetwork, CyIdentifiable> getCompositeFilter() {
		return neighbourFilter;
	}

	@Override
	public boolean accepts(CyNetwork network, CyIdentifiable element) {
		if (!(element instanceof CyNode) || distance == null || threshold == null) {
			return false;
		}
		
		Set<Long> counted  = new HashSet<>();
		// keep track of which edges have already been traversed, and the distance value
		Map<Long,Integer> sourceToTarget = new HashMap<>();
		Map<Long,Integer> targetToSource = new HashMap<>();
		
		traverse(network, (CyNode) element, distance, counted, sourceToTarget, targetToSource);
		
		counted.remove(element.getSUID());
		int count = counted.size();
		
		return delegate.accepts(threshold, threshold, count);
	}
	
	
	private void traverse(CyNetwork network, CyNode node, int distance, Set<Long> counted, Map<Long,Integer> sourceToTarget, Map<Long,Integer> targetToSource) {
		if(memoizedFilter.accepts(network, node))
			counted.add(node.getSUID());
		
		if(distance == 0) 
			return;
		
		for(CyEdge edge : network.getAdjacentEdgeIterable(node, CyEdge.Type.ANY)) {
			// short circut if we've already found enough nodes
			if(counted.size() > threshold) // 'greater than' because the start node is included in the count
				return;
			
			// figure out which direction we are going along the edge
			CyNode next = edge.getTarget();
			Map<Long,Integer> map = sourceToTarget;
			if(next == node) {
				next = edge.getSource();
				map = targetToSource;
			}
			
			if(traverseEdge(edge, distance, map)) {
				traverse(network, next, distance - 1, counted, sourceToTarget, targetToSource);
			}
		}
	}
	
	private boolean traverseEdge(CyEdge edge, int distance, Map<Long,Integer> edgeTraversal) {
		Integer prevDistance = edgeTraversal.get(edge.getSUID());
		if(prevDistance == null || prevDistance < distance) {
			edgeTraversal.put(edge.getSUID(), distance);
			return true;
		}
		return false;
	}
	
	@Override
	public void append(Filter<CyNetwork, CyIdentifiable> filter) {
		neighbourFilter.append(filter);
	}

	@Override
	public void insert(int index, Filter<CyNetwork, CyIdentifiable> filter) {
		neighbourFilter.insert(index, filter);
	}

	@Override
	public Filter<CyNetwork, CyIdentifiable> get(int index) {
		return neighbourFilter.get(index);
	}

	@Override
	public Filter<CyNetwork, CyIdentifiable> remove(int index) {
		return neighbourFilter.remove(index);
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
	public CompositeFilter.Type getType() {
		return neighbourFilter.getType();
	}

	@Override
	public void setType(CompositeFilter.Type type) {
		neighbourFilter.setType(type);
	}
	
}
