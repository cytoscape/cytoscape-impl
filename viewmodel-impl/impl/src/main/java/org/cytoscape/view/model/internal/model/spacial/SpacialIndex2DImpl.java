package org.cytoscape.view.model.internal.model.spacial;

import com.github.davidmoten.rtree.RTree;
import com.github.davidmoten.rtree.geometry.Rectangle;
import com.github.davidmoten.rtree.geometry.internal.RectangleFloat;

import io.vavr.collection.HashMap;
import io.vavr.collection.Map;

public class SpacialIndex2DImpl<T> extends SpacialIndex2DBase<T> {

	private RTree<T,Rectangle> rtree = RTree.create();
	private Map<T,Rectangle> geometries = HashMap.empty();
	

	@Override
	protected RTree<T, Rectangle> getRTree() {
		return rtree;
	}

	@Override
	protected Map<T, Rectangle> getGeometries() {
		return geometries;
	}
	
	@Override
	public synchronized void put(T suid, float xMin, float yMin, float xMax, float yMax) {
		delete(suid);
		Rectangle r = RectangleFloat.create(xMin, yMin, xMax, yMax);
		rtree = rtree.add(suid, r);
		geometries = geometries.put(suid, r);
	}
	
	@Override
	public synchronized void delete(T suid) {
		Rectangle r = geometries.getOrElse(suid, null);
		if(r != null) {
			rtree = rtree.delete(suid, r);
			geometries = geometries.remove(suid);
		}
	}
	
}
