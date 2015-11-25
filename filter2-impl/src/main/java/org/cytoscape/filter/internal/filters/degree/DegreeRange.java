package org.cytoscape.filter.internal.filters.degree;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;


/**
 * Will compute the degree range for INCOMING, OUTGOING and ANY.
 */
public class DegreeRange {
	
	private boolean updated = false;
	
	private final PairImpl incoming = new PairImpl();
	private final PairImpl outgoing = new PairImpl();

	
	public interface Pair {
		int getLow();
		int getHigh();
	}
	
	private static class PairImpl implements Pair {
		int low = Integer.MAX_VALUE;
		int high = Integer.MIN_VALUE;
		public int getLow() { return low == Integer.MAX_VALUE ? 0 : low; }
		public int getHigh() { return high == Integer.MIN_VALUE ? 0 : high; }
	}

	public void update(CyNetwork network, CyEdge edge) {
		update(incoming, network, edge.getSource(), CyEdge.Type.INCOMING);
		update(outgoing, network, edge.getSource(), CyEdge.Type.OUTGOING);
		update(incoming, network, edge.getTarget(), CyEdge.Type.INCOMING);
		update(outgoing, network, edge.getTarget(), CyEdge.Type.OUTGOING);
	}
	
	public void update(CyNetwork network) {
		for(CyNode node : network.getNodeList()) {
			update(incoming, network, node, CyEdge.Type.INCOMING);
			update(outgoing, network, node, CyEdge.Type.OUTGOING);
		}
	}
	
	private void update(PairImpl pair, CyNetwork network, CyNode node, CyEdge.Type type) {
		int degree = computeDegree(network, node, type);
		pair.low = Integer.min(pair.low, degree);
		pair.high = Integer.max(pair.high, degree);
		updated = true;
	}
	
	private static int computeDegree(CyNetwork network, CyNode node, CyEdge.Type type) {
		int degree = 0;
		for(@SuppressWarnings("unused") CyEdge edge : network.getAdjacentEdgeIterable(node, type)) {
			degree++;
		}
		return degree;
	}
	
	
	public boolean isUpdated() {
		return updated;
	}
	
	public Pair getInRange() {
		return incoming;
	}
	
	public Pair getOutRange() {
		return outgoing;
	}
	
	public Pair getAnyRange() {
		return new Pair() {
			public int getLow() {
				return incoming.low + outgoing.low;
			}
			public int getHigh() {
				return incoming.high + outgoing.high;
			}
		};
	}
	
	public Pair getRange(CyEdge.Type type) {
		switch(type) {
			case ANY:      return getAnyRange();
			case INCOMING: return getInRange();
			case OUTGOING: return getOutRange();
			default: throw new IllegalArgumentException();
		}
	}
	
}
