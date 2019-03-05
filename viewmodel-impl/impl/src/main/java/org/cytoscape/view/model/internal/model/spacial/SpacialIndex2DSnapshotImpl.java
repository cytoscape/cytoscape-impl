package org.cytoscape.view.model.internal.model.spacial;

import com.github.davidmoten.rtree.RTree;
import com.github.davidmoten.rtree.geometry.Rectangle;

import io.vavr.collection.Map;

public class SpacialIndex2DSnapshotImpl extends SpacialIndex2DBase<Long> {
	
	private final RTree<Long,Rectangle> rtree;
	private final Map<Long,Rectangle> geometries;
	
	public SpacialIndex2DSnapshotImpl(RTree<Long,Rectangle> rtree, Map<Long,Rectangle> geometries) {
		this.rtree = rtree;
		this.geometries = geometries;
	}

	@Override
	protected RTree<Long, Rectangle> getRTree() {
		return rtree;
	}

	@Override
	protected Map<Long, Rectangle> getGeometries() {
		return geometries;
	}

	@Override
	public void put(Long suid, float xMin, float yMin, float xMax, float yMax) {
		throw new UnsupportedOperationException("Cannot modify view snapshot");
	}
	
	@Override
	public void delete(Long suid) {
		throw new UnsupportedOperationException("Cannot modify view snapshot");
	}
	
}
