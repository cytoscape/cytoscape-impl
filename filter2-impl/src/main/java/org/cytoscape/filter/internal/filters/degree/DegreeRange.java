package org.cytoscape.filter.internal.filters.degree;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyEdge.Type;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;


/**
 * Will compute the degree range for INCOMING, OUTGOING and ANY.
 */
public class DegreeRange {
	
	private boolean updated = false;
	
	private final RangeImpl incoming = new RangeImpl();
	private final RangeImpl outgoing = new RangeImpl();
	private final RangeImpl any = new RangeImpl();
	
	public interface Range {
		int getLow();
		int getHigh();
	}
	
	private static class RangeImpl implements Range {
		int low  = Integer.MAX_VALUE;
		int high = Integer.MIN_VALUE;
		
		public int getLow() { 
			return low == Integer.MAX_VALUE ? 0 : low; 
		}
		public int getHigh() { 
			return high == Integer.MIN_VALUE ? 0 : high; 
		}
		
		void update(int degree) {
			this.low  = Integer.min(this.low,  degree);
			this.high = Integer.max(this.high, degree);
		}
	}

	public void update(CyNetwork network, CyEdge edge) {
		update(network, edge.getSource());
		update(network, edge.getTarget());
		updated = true;
	}
	
	public void update(CyNetwork network) {
		for(var node : network.getNodeList()) {
			update(network, node);
		}
		updated = true;
	}
	
	private void update(CyNetwork network, CyNode node) {
		int inDegree = 0, outDegree = 0;
		long nodeSuid = node.getSUID();
		
		for(var edge : network.getAdjacentEdgeIterable(node, Type.ANY)) {
			var targetSuid = edge.getTarget().getSUID();
			if(nodeSuid == targetSuid)
				inDegree++;
			else
				outDegree++;
		}
		
		incoming.update(inDegree);
		outgoing.update(outDegree);
		any.update(inDegree + outDegree);
	}
	
	
	public boolean isUpdated() {
		return updated;
	}
	
	public Range getInRange() {
		return incoming;
	}
	
	public Range getOutRange() {
		return outgoing;
	}
	
	public Range getAnyRange() {
		return any;
	}
	
	public Range getRange(CyEdge.Type type) {
		switch(type) {
			case ANY:      return getAnyRange();
			case INCOMING: return getInRange();
			case OUTGOING: return getOutRange();
			default: throw new IllegalArgumentException();
		}
	}
	
}
