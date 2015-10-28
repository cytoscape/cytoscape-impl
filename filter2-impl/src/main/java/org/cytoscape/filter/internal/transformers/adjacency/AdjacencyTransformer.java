package org.cytoscape.filter.internal.transformers.adjacency;

import org.cytoscape.filter.internal.AbstractMemoizableTransformer;
import org.cytoscape.filter.internal.filters.composite.CompositeFilterImpl;
import org.cytoscape.filter.model.CompositeFilter;
import org.cytoscape.filter.model.ElementTransformer;
import org.cytoscape.filter.model.Filter;
import org.cytoscape.filter.model.SubFilterTransformer;
import org.cytoscape.filter.model.TransformerSink;
import org.cytoscape.filter.transformers.Transformers;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.work.Tunable;

public class AdjacencyTransformer extends AbstractMemoizableTransformer<CyNetwork,CyIdentifiable> 
                                  implements ElementTransformer<CyNetwork,CyIdentifiable>, 
                                             SubFilterTransformer<CyNetwork,CyIdentifiable> {

	public static enum Action {
		ADD, REPLACE
	}
	
	public static enum What {
		NODES, EDGES, NODES_AND_EDGES;
	}
	
	public static enum EdgesAre {
		INCOMING, OUTGOING, INCOMING_AND_OUTGOING;
		
		public CyEdge.Type type() {
			switch(this) {
			default:
			case INCOMING: return CyEdge.Type.INCOMING;
			case OUTGOING: return CyEdge.Type.OUTGOING;
			case INCOMING_AND_OUTGOING: return CyEdge.Type.ANY;
			}
		}
	}
	
	
	
	// Internally use a compositeFilter to store child filters
	private final CompositeFilter<CyNetwork,CyIdentifiable> adjacentElementFilter;
	
	// defaults
	private Action action = Action.ADD;
	private EdgesAre edgesAre = EdgesAre.INCOMING_AND_OUTGOING;
	private What filterTarget = What.NODES;
	private What output = What.NODES_AND_EDGES;
	

	public AdjacencyTransformer() {
		adjacentElementFilter = new CompositeFilterImpl<>(CyNetwork.class,CyIdentifiable.class);
		adjacentElementFilter.setType(CompositeFilter.Type.ALL); // ALL accepts if empty
		adjacentElementFilter.addListener(this::notifyListeners);
	}
	
	
	@Override
	public CompositeFilter<CyNetwork,CyIdentifiable> getCompositeFilter() {
		return adjacentElementFilter;
	}

	public boolean hasSubfilters() {
		return adjacentElementFilter.getLength() > 0;
	}
	
	@Override
	public String getName() {
		return "Node Adjacency Transformer";
	}

	@Override
	public String getId() {
		return Transformers.ADJACENCY_TRANSFORMER; 
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
	public void apply(CyNetwork network, CyIdentifiable element, TransformerSink<CyIdentifiable> sink) {
		if(action == Action.ADD)
			sink.collect(element);
		
		if(element instanceof CyNode) {
			CyNode currentNode = (CyNode) element;
			Iterable<CyEdge> adjacentEdges  = network.getAdjacentEdgeIterable(currentNode, edgesAre.type());
			Filter<CyNetwork,CyIdentifiable> memoizedFilter = super.getMemoizedFilter();
			
			for(CyEdge edge : adjacentEdges) {
				CyNode node = otherNode(currentNode, edge);
				
				boolean pass;
				if(filterTarget == What.NODES) {
					pass = memoizedFilter.accepts(network, node);
				}
				else if(filterTarget == What.EDGES) {
					pass = memoizedFilter.accepts(network, edge);
				}
				else { //filterTarget == What.NODES_AND_EDGES
					pass = memoizedFilter.accepts(network, node) && memoizedFilter.accepts(network, edge);
				}
				
				if(pass) {
					if(output == What.NODES || output == What.NODES_AND_EDGES) {
						sink.collect(node);
					}
					if(output == What.EDGES || output == What.NODES_AND_EDGES) {
						sink.collect(edge);
					}
				}
			}
		}
	}
	
	
	private static CyNode otherNode(CyNode node, CyEdge edge) {
		if(node != edge.getSource() && node != edge.getTarget())
			throw new RuntimeException("hey, this node is not the source or the target!");
		
		CyNode other = edge.getSource();
		if(other == node)
			other = edge.getTarget();
		return other;
	}
	
	public int getFilterCount() {
		return adjacentElementFilter.getLength();
	}

	@Tunable
	public Action getAction() {
		return action;
	}

	public void setAction(Action action) {
		this.action = action;
		notifyListeners();
	}

	@Tunable
	public What getOutput() {
		return output;
	}

	public void setOutput(What output) {
		this.output = output;
		notifyListeners();
	}

	@Tunable
	public EdgesAre getEdgesAre() {
		return edgesAre;
	}

	public void setEdgesAre(EdgesAre edgesAre) {
		this.edgesAre = edgesAre;
		notifyListeners();
	}

	@Tunable
	public What getFilterTarget() {
		return filterTarget;
	}

	public void setFilterTarget(What filterTarget) {
		this.filterTarget = filterTarget;
		notifyListeners();
	}
}
