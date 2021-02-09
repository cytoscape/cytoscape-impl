package org.cytoscape.view.model.internal.network.spacial;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNode;
import org.cytoscape.view.model.SnapshotEdgeInfo;
import org.cytoscape.view.model.View;
import org.cytoscape.view.model.internal.network.snapshot.CyNetworkViewSnapshotImpl;
import org.cytoscape.view.model.internal.network.snapshot.CyNodeViewSnapshotImpl;
import org.cytoscape.view.model.spacial.EdgeSpacialIndex2DEnumerator;
import org.cytoscape.view.model.spacial.NetworkSpacialIndex2D;
import org.cytoscape.view.model.spacial.NodeSpacialIndex2DEnumerator;
import org.cytoscape.view.model.spacial.SpacialIndex2DEnumerator;

public class SimpleSpacialIndex2DSnapshotImpl implements NetworkSpacialIndex2D {

	private final CyNetworkViewSnapshotImpl snapshot;
	
	// The snapshot is immutable so we can cache whatever we want
	private double[] mbrd = null;
	private float[]  mbrf = null;
	
	private Comparator<View<CyNode>> nodeZComparator;
	private Comparator<View<CyEdge>> edgeZComparator;
	
	private QueryResults cachedQueryResults;
	
	
	public SimpleSpacialIndex2DSnapshotImpl(CyNetworkViewSnapshotImpl snapshot) {
		this.snapshot = snapshot;
		this.nodeZComparator = Comparator.comparing(node -> snapshot.getNodeZ(node.getSUID()));
		this.edgeZComparator = Comparator.comparing(edge -> snapshot.getEdgeZ(edge.getSUID()));
	}
	

	@Override
	public void getMBR(float[] extents) {
		initMBR();
		if(extents != null) {
			extents[X_MIN] = mbrf[X_MIN];
			extents[Y_MIN] = mbrf[Y_MIN];
			extents[X_MAX] = mbrf[X_MAX];
			extents[Y_MAX] = mbrf[Y_MAX];
		}
	}

	@Override
	public void getMBR(double[] extents) {
		initMBR();
		if(extents != null) {
			extents[X_MIN] = mbrd[X_MIN];
			extents[Y_MIN] = mbrd[Y_MIN];
			extents[X_MAX] = mbrd[X_MAX];
			extents[Y_MAX] = mbrd[Y_MAX];
		}
	}
	

	private void initMBR() {
		if(mbrd == null || mbrf == null) {
			Iterator<CyNodeViewSnapshotImpl> iter = snapshot.getSnapshotNodeViews().iterator();
			if(!iter.hasNext()) {
				mbrd = new double[4];
				mbrf = new float[4];
			} else {
				CyNodeViewSnapshotImpl node = iter.next();
				double x = node.x;
				double y = node.y;
				double h = node.h;
				double w = node.w;
				
				double xMin = x - (w/2);
				double xMax = x + (w/2);
				double yMin = y - (h/2);
				double yMax = y + (h/2);
				
				while(iter.hasNext()) {
					node = iter.next();
					x = node.x;
					y = node.y;
					h = node.h;
					w = node.w;
					
					xMin = Math.min(xMin, x - (w/2));
					xMax = Math.max(xMax, x + (w/2));
					yMin = Math.min(yMin, y - (h/2));
					yMax = Math.max(yMax, y + (h/2));
				}
				
				mbrd = new double[4];
				mbrd[X_MIN] = xMin;
				mbrd[X_MAX] = xMax;
				mbrd[Y_MIN] = yMin;
				mbrd[Y_MAX] = yMax;
				
				mbrf = new float[4];
				mbrf[X_MIN] = (float) xMin;
				mbrf[X_MAX] = (float) xMax;
				mbrf[Y_MIN] = (float) yMin;
				mbrf[Y_MAX] = (float) yMax;
			}
		}
	}
	
	@Override
	public boolean exists(Long suid) {
		return snapshot.getNodeView(suid) != null;
	}

	@Override
	public boolean get(Long suid, float[] extents) {
		var node = snapshot.getNodeView(suid);
		if(node == null)
			return false;
		copyExtents(node, extents);
		return true;
	}

	@Override
	public boolean get(Long suid, double[] extents) {
		var node = snapshot.getNodeView(suid);
		if(node == null)
			return false;
		copyExtents(node, extents);
		return true;
	}

	private static void copyExtents(CyNodeViewSnapshotImpl node, float[] extents) {
		if(extents != null) {
			double x = node.x;
			double y = node.y;
			double h = node.h;
			double w = node.w;
			extents[X_MIN] = (float) (x - (w/2));
			extents[X_MAX] = (float) (x + (w/2));
			extents[Y_MIN] = (float) (y - (h/2));
			extents[Y_MAX] = (float) (y + (h/2));
		}
	}
	
	private static void copyExtents(CyNodeViewSnapshotImpl node, double[] extents) {
		if(extents != null) {
			double x = node.x;
			double y = node.y;
			double h = node.h;
			double w = node.w;
			extents[X_MIN] = x - (w/2);
			extents[X_MAX] = x + (w/2);
			extents[Y_MIN] = y - (h/2);
			extents[Y_MAX] = y + (h/2);
		}
	}
	
	@Override
	public int size() {
		return snapshot.getNodeCount();
	}

	private QueryResults computeQueryOverlap(float xMin, float yMin, float xMax, float yMax) {
		initMBR();
		
		// Snap to MBR, if user zooms way out we can use cached MBR results.
		xMin = Math.max(xMin, mbrf[X_MIN]);
		yMin = Math.max(yMin, mbrf[Y_MIN]);
		xMax = Math.min(xMax, mbrf[X_MAX]);
		yMax = Math.min(yMax, mbrf[Y_MAX]);
		
		// This method is not synchronized, get a reference before doing the test just in case the reference changes
		var cache = cachedQueryResults;
		if(cache != null && cache.matches(xMin,yMin,xMax,yMax)) {
			return cache;
		}
		
		cachedQueryResults = null;
		
		List<CyNodeViewSnapshotImpl> overlapNodes = new ArrayList<>();
		for(var node : snapshot.getSnapshotNodeViews()) {
			double x = node.x;
			double y = node.y;
			double h = node.h;
			double w = node.w;
			double aMin = x - (w/2);
			double aMax = x + (w/2);
			double bMin = y - (h/2);
			double bMax = y + (h/2);
			
			if(intersects(xMin,yMin,xMax,yMax,  aMin,bMin,aMax,bMax)) {
				overlapNodes.add(node);
			}
		}
		
		Collections.sort(overlapNodes, nodeZComparator);
		cachedQueryResults = new QueryResults(xMin, yMin, xMax, yMax, overlapNodes);
		return cachedQueryResults;
	}
	
	@Override
	public SpacialIndex2DEnumerator<Long> queryAll() {
		initMBR();
		return queryOverlap(mbrf[X_MIN], mbrf[Y_MIN], mbrf[X_MAX], mbrf[Y_MAX]);
	}
	
	@Override
	public SpacialIndex2DEnumerator<Long> queryOverlap(float xMin, float yMin, float xMax, float yMax) {
		QueryResults cache = computeQueryOverlap(xMin, yMin, xMax, yMax);
		return new SimpleNodeEnumerator(cache.getOverlapNodes());
	}
	
	@Override
	public NodeSpacialIndex2DEnumerator queryAllNodes() {
		initMBR();
		return queryOverlapNodes(mbrf[X_MIN], mbrf[Y_MIN], mbrf[X_MAX], mbrf[Y_MAX]);
	}
	
	@Override
	public NodeSpacialIndex2DEnumerator queryOverlapNodes(float xMin, float yMin, float xMax, float yMax) {
		QueryResults cache = computeQueryOverlap(xMin, yMin, xMax, yMax);
		return new SimpleNodeEnumerator(cache.getOverlapNodes());
	}
	
	public EdgeSpacialIndex2DEnumerator queryAllEdges() {
		initMBR();
		return queryOverlapEdges(mbrf[X_MIN], mbrf[Y_MIN], mbrf[X_MAX], mbrf[Y_MAX]);
	}
	
	
	public EdgeSpacialIndex2DEnumerator queryOverlapEdges(float xMin, float yMin, float xMax, float yMax) {
		QueryResults cache = computeQueryOverlap(xMin, yMin, xMax, yMax);
		return new SimpleEdgeEnumerator(cache.getAdjacentEdges());
	}
	

	private static boolean intersects(float x1, float y1, float x2, float y2, 
			double a1, double b1, double a2, double b2) {
		return x1 <= a2 && a1 <= x2 && y1 <= b2 && b1 <= y2;
	}
	
	
	private class QueryResults {
		
		private final float xMin;
		private final float yMin;
		private final float xMax;
		private final float yMax;
		private final List<CyNodeViewSnapshotImpl> overlapNodes;
		private List<View<CyEdge>> edges;
		
		public QueryResults(float xMin, float yMin, float xMax, float yMax, List<CyNodeViewSnapshotImpl> overlapNodes) {
			this.xMin = xMin;
			this.yMin = yMin;
			this.xMax = xMax;
			this.yMax = yMax;
			this.overlapNodes = overlapNodes;
		}
		
		public boolean matches(float xMin, float yMin, float xMax, float yMax) {
			return this.xMin == xMin && this.yMin == yMin && this.xMax == xMax && this.yMax == yMax;
		}
		
		public List<CyNodeViewSnapshotImpl> getOverlapNodes() {
			return overlapNodes;
		}
		
		public List<View<CyEdge>> getAdjacentEdges() {
			if(edges == null) {
				edges = new ArrayList<>();
				
				Set<Long> visitedNodes = new HashSet<>();
				for(var node : overlapNodes) {
					long nodeSuid = node.getSUID();
					Iterable<View<CyEdge>> touchingEdges = snapshot.getAdjacentEdgeIterable(nodeSuid);
					for(View<CyEdge> edge : touchingEdges) {
						SnapshotEdgeInfo edgeInfo = snapshot.getEdgeInfo(edge);
						long otherNode = nodeSuid ^ edgeInfo.getSourceViewSUID() ^ edgeInfo.getTargetViewSUID();
						if(!visitedNodes.contains(otherNode)) {
							edges.add(edge);
						}
					}
					visitedNodes.add(nodeSuid);
				}
				
				if(snapshot.hasEdgeZ()) {
					edges.sort(edgeZComparator);
				}
			}
			return edges;
		}
	}

	
	private class SimpleNodeEnumerator implements NodeSpacialIndex2DEnumerator {

		private final int size;
		private final Iterator<CyNodeViewSnapshotImpl> iter;
		
		public SimpleNodeEnumerator(List<CyNodeViewSnapshotImpl> nodes) {
			this.size = nodes.size();
			this.iter = nodes.iterator();
		}
		
		@Override
		public int size() {
			return size;
		}

		@Override
		public boolean hasNext() {
			return iter.hasNext();
		}

		@Override
		public Long nextExtents(float[] extents) {
			var node = iter.next();
			copyExtents(node, extents);
			return node.getSUID();
		}

		@Override
		public View<CyNode> nextNode() {
			return iter.next();
		}

		@Override
		public View<CyNode> nextNodeExtents(float[] extents) {
			var node = iter.next();
			copyExtents(node, extents);
			return node;
		}
	}
	
	
	private class SimpleEdgeEnumerator implements EdgeSpacialIndex2DEnumerator {

		private final int size;
		private final Iterator<View<CyEdge>> iter;
		
		public SimpleEdgeEnumerator(List<View<CyEdge>> edges) {
			this.size = edges.size();
			this.iter = edges.iterator();
		}
		
		@Override
		public int size() {
			return size;
		}

		@Override
		public boolean hasNext() {
			return iter.hasNext();
		}

		@Override
		public View<CyEdge> nextEdge() {
			return iter.next();
		}
		
		@Override
		public View<CyEdge> nextEdgeWithNodeExtents(float[] sourceExtents, float[] targetExtents, View<CyNode>[] nodes) {
			var edge = iter.next();
			var edgeInfo = snapshot.getEdgeInfo(edge);
			var sourceNode = snapshot.getNodeView(edgeInfo.getSourceViewSUID());
			var targetNode = snapshot.getNodeView(edgeInfo.getTargetViewSUID());
			copyExtents(sourceNode, sourceExtents);
			copyExtents(targetNode, targetExtents);
			if(nodes != null && nodes.length >= 2) {
				nodes[0] = sourceNode;
				nodes[1] = targetNode;
			}
			return edge;
		}
	}

}
