package org.cytoscape.view.model.internal.model.spacial;

import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NODE_HEIGHT;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NODE_VISIBLE;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NODE_WIDTH;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NODE_X_LOCATION;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NODE_Y_LOCATION;

import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.model.internal.model.VPStore;

import com.github.davidmoten.rtree.RTree;
import com.github.davidmoten.rtree.geometry.Rectangle;
import com.github.davidmoten.rtree.geometry.internal.RectangleFloat;

import io.vavr.collection.HashMap;
import io.vavr.collection.Map;

/**
 * Mutable SpacialIndex specifically for use by CyNetworkViewImpl.
 */
public class SpacialIndexStore {
	
	private RTree<Long,Rectangle> rtree = RTree.create();
	private Map<Long,Rectangle> geometries = HashMap.empty();
	
	
	public SpacialIndex2DSnapshotImpl createSnapshot() {
		return new SpacialIndex2DSnapshotImpl(rtree, geometries);
	}
	
	public void addDefault(VPStore nodeVPs, Long suid) {
		Rectangle r = getDefaultGeometry(nodeVPs);
		rtree = rtree.add(suid, r);
		geometries = geometries.put(suid, r);
	}
	
	public void remove(Long suid) {
		Rectangle r = geometries.getOrElse(suid, null);
		rtree = rtree.delete(suid, r);
		geometries = geometries.remove(suid);
	}
	
	public <T, V extends T> void updateNodeGeometry(Long suid, VPStore nodeVPs, VisualProperty<? extends T> vp) {
		Rectangle r = geometries.getOrElse(suid, null);
		// need to look up the actual value because it might be locked
		Object value = nodeVPs.getVisualProperty(suid, vp);
		
		if(r != null) {
			Rectangle newGeom = null;
			if(vp == NODE_X_LOCATION) {
				float x = ((Number)value).floatValue();
				float wDiv2 = (float) (r.x2() - r.x1()) / 2.0f;
				float xMin = x - wDiv2;
				float xMax = x + wDiv2;
				newGeom = RectangleFloat.create(xMin, (float)r.y1(), xMax, (float)r.y2());
			} 
			else if(vp == NODE_Y_LOCATION) {
				float y = ((Number)value).floatValue();
				float hDiv2 = (float) (r.y2() - r.y1()) / 2.0f;
				float yMin = y - hDiv2;
				float yMax = y + hDiv2;
				newGeom = RectangleFloat.create((float)r.x1(), yMin, (float)r.x2(), yMax);
			}
			else if(vp == NODE_WIDTH) {
				float w = ((Number)value).floatValue();
				float xMid = (float) (r.x1() + r.x2()) / 2.0f;
				float wDiv2 = w / 2.0f;
				float xMin = xMid - wDiv2;
				float xMax = xMid + wDiv2;
				newGeom = RectangleFloat.create(xMin, (float)r.y1(), xMax, (float)r.y2());
			}
			else if(vp == NODE_HEIGHT) {
				float h = ((Number)value).floatValue();
				float yMid = (float) (r.y1() + r.y2()) / 2.0f;
				float hDiv2 = h / 2.0f;
				float yMin = yMid - hDiv2;
				float yMax = yMid + hDiv2;
				newGeom = RectangleFloat.create((float)r.x1(), yMin, (float)r.x2(), yMax);
			} 
			
			if(newGeom != null) {
				rtree = rtree.delete(suid, r).add(suid, newGeom);
				geometries = geometries.put(suid, newGeom);
			}
		}
		if(vp == NODE_VISIBLE) {
			if(Boolean.TRUE.equals(value)) {
				if(r == null) {
					float x = ((Number)nodeVPs.getVisualProperty(suid, NODE_X_LOCATION)).floatValue();
					float y = ((Number)nodeVPs.getVisualProperty(suid, NODE_Y_LOCATION)).floatValue();
					float w = ((Number)nodeVPs.getVisualProperty(suid, NODE_WIDTH)).floatValue();
					float h = ((Number)nodeVPs.getVisualProperty(suid, NODE_HEIGHT)).floatValue();
					r = vpToRTree(x, y, w, h);
					rtree = rtree.add(suid, r);
					geometries = geometries.put(suid, r);
				}
			} else {
				if(r != null) { // can be null if view is already hidden
					rtree = rtree.delete(suid, r);
					geometries = geometries.remove(suid);
				}
			}
		}
	}
	
	private Rectangle getDefaultGeometry(VPStore nodeVPs) {
		float x = nodeVPs.getViewDefault(NODE_X_LOCATION).floatValue();
		float y = nodeVPs.getViewDefault(NODE_Y_LOCATION).floatValue();
		float w = nodeVPs.getViewDefault(NODE_WIDTH).floatValue();
		float h = nodeVPs.getViewDefault(NODE_HEIGHT).floatValue();
		return vpToRTree(x, y, w, h);
	}

	/**
	 * Rtree stores (x1, x2, y1, y2) visual properties store (centerX, centerY, w, h)
	 */
	private static Rectangle vpToRTree(float x, float y, float w, float h) {
		float halfW = w / 2f;
		float halfH = h / 2f;
		return RectangleFloat.create(x - halfW, y - halfH, x + halfW, y + halfH);
	}
	
}
