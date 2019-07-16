package org.cytoscape.view.model.internal.model.spacial;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.cytoscape.model.CyNode;
import org.cytoscape.view.model.View;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.model.internal.model.snapshot.CyNetworkViewSnapshotImpl;
import org.cytoscape.view.model.spacial.SpacialIndex2D;
import org.cytoscape.view.model.spacial.SpacialIndex2DEnumerator;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;

public class SimpleSpacialIndex2DSnapshotImpl implements SpacialIndex2D<Long> {

	private final CyNetworkViewSnapshotImpl snapshot;
	
	// The snapshot is immutable so we can cache whatever we want
	private double[] mbr = null;
	private Comparator<View<CyNode>> zOrderComparator;
	
	public SimpleSpacialIndex2DSnapshotImpl(CyNetworkViewSnapshotImpl snapshot) {
		this.snapshot = snapshot;
		this.zOrderComparator = createComparator();
	}
	
	@Override
	public void put(Long suid, float xMin, float yMin, float xMax, float yMax) {
		throw new UnsupportedOperationException("Cannot modify view snapshot");
	}
	
	@Override
	public void delete(Long suid) {
		throw new UnsupportedOperationException("Cannot modify view snapshot");
	}

	@Override
	public void getMBR(float[] extents) {
		initMBR();
		if(extents != null) {
			extents[X_MIN] = (float) mbr[X_MIN];
			extents[Y_MIN] = (float) mbr[Y_MIN];
			extents[X_MAX] = (float) mbr[X_MAX];
			extents[Y_MAX] = (float) mbr[Y_MAX];
		}
	}

	@Override
	public void getMBR(double[] extents) {
		initMBR();
		if(extents != null) {
			extents[X_MIN] = mbr[X_MIN];
			extents[Y_MIN] = mbr[Y_MIN];
			extents[X_MAX] = mbr[X_MAX];
			extents[Y_MAX] = mbr[Y_MAX];
		}
	}
	
	private static double get(View<CyNode> node, VisualProperty<?> vp) {
		return ((Number)node.getVisualProperty(vp)).doubleValue();
	}

	private void initMBR() {
		if(mbr == null) {
			Iterator<View<CyNode>> iter = snapshot.getNodeViewsIterable().iterator();
			if(!iter.hasNext()) {
				mbr = new double[] {0, 0, 0, 0};
			} else {
				View<CyNode> node = iter.next();
				double x = get(node, BasicVisualLexicon.NODE_X_LOCATION);
				double y = get(node, BasicVisualLexicon.NODE_Y_LOCATION);
				double h = get(node, BasicVisualLexicon.NODE_HEIGHT);
				double w = get(node, BasicVisualLexicon.NODE_WIDTH);
				
				double xMin = x - (w/2);
				double xMax = x + (w/2);
				double yMin = y - (h/2);
				double yMax = y + (h/2);
				
				while(iter.hasNext()) {
					node = iter.next();
					x = get(node, BasicVisualLexicon.NODE_X_LOCATION);
					y = get(node, BasicVisualLexicon.NODE_Y_LOCATION);
					h = get(node, BasicVisualLexicon.NODE_HEIGHT);
					w = get(node, BasicVisualLexicon.NODE_WIDTH);
					
					xMin = Math.min(xMin, x - (w/2));
					xMax = Math.max(xMax, x + (w/2));
					yMin = Math.min(yMin, y - (h/2));
					yMax = Math.max(yMax, y + (h/2));
				}
				
				mbr = new double[4];
				mbr[X_MIN] = xMin;
				mbr[X_MAX] = xMax;
				mbr[Y_MIN] = yMin;
				mbr[Y_MAX] = yMax;
			}
		}
	}
	
	@Override
	public boolean exists(Long suid) {
		return snapshot.getNodeView(suid) != null;
	}

	@Override
	public boolean get(Long suid, float[] extents) {
		View<CyNode> node = snapshot.getNodeView(suid);
		if(node == null)
			return false;
		copyExtents(node, extents);
		return true;
	}

	@Override
	public boolean get(Long suid, double[] extents) {
		View<CyNode> node = snapshot.getNodeView(suid);
		if(node == null)
			return false;
		copyExtents(node, extents);
		return true;
	}

	private static void copyExtents(View<CyNode> node, float[] extents) {
		if(extents != null) {
			double x = get(node, BasicVisualLexicon.NODE_X_LOCATION);
			double y = get(node, BasicVisualLexicon.NODE_Y_LOCATION);
			double h = get(node, BasicVisualLexicon.NODE_HEIGHT);
			double w = get(node, BasicVisualLexicon.NODE_WIDTH);
			extents[X_MIN] = (float) (x - (w/2));
			extents[X_MAX] = (float) (x + (w/2));
			extents[Y_MIN] = (float) (y - (h/2));
			extents[Y_MAX] = (float) (y + (h/2));
		}
	}
	
	private static void copyExtents(View<CyNode> node, double[] extents) {
		if(extents != null) {
			double x = get(node, BasicVisualLexicon.NODE_X_LOCATION);
			double y = get(node, BasicVisualLexicon.NODE_Y_LOCATION);
			double h = get(node, BasicVisualLexicon.NODE_HEIGHT);
			double w = get(node, BasicVisualLexicon.NODE_WIDTH);
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

	@Override
	public SpacialIndex2DEnumerator<Long> queryAll() {
		return new SimpleSpacialIndex2DEnumerator(snapshot.getNodeViews());
	}
	
	@Override
	public SpacialIndex2DEnumerator<Long> queryOverlap(float xMin, float yMin, float xMax, float yMax) {
		initMBR();
		if(xMin <= mbr[X_MIN] && yMin <= mbr[Y_MIN] && xMax >= mbr[X_MAX] && yMax >= mbr[Y_MAX])
			return queryAll();
		
		List<View<CyNode>> overlapNodes = new ArrayList<>();
		for(View<CyNode> node : snapshot.getNodeViews()) {
			double x = get(node, BasicVisualLexicon.NODE_X_LOCATION);
			double y = get(node, BasicVisualLexicon.NODE_Y_LOCATION);
			double h = get(node, BasicVisualLexicon.NODE_HEIGHT);
			double w = get(node, BasicVisualLexicon.NODE_WIDTH);
			double aMin = x - (w/2);
			double aMax = x + (w/2);
			double bMin = y - (h/2);
			double bMax = y + (h/2);
			
			if(intersects(xMin,yMin,xMax,yMax,  aMin,bMin,aMax,bMax)) {
				overlapNodes.add(node);
			}
		}
		return new SimpleSpacialIndex2DEnumerator(overlapNodes);
	}

	private static boolean intersects(float x1, float y1, float x2, float y2, 
			double a1, double b1, double a2, double b2) {
		return x1 <= a2 && a1 <= x2 && y1 <= b2 && b1 <= y2;
	}
	
	private Comparator<View<CyNode>> createComparator() {
		Comparator<View<CyNode>> comparator;
		comparator = Comparator.comparing(node -> snapshot.getZ(node.getSUID()));
		return comparator;
	}
	
	
	private class SimpleSpacialIndex2DEnumerator implements SpacialIndex2DEnumerator<Long> {

		private final int size;
		private final Iterator<View<CyNode>> iter;
		
		public SimpleSpacialIndex2DEnumerator(List<View<CyNode>> nodes) {
			Collections.sort(nodes, zOrderComparator);
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
			View<CyNode> node = iter.next();
			copyExtents(node, extents);
			return node.getSUID();
		}
	}

}
