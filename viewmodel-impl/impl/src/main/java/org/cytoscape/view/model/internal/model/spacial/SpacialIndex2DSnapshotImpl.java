package org.cytoscape.view.model.internal.model.spacial;

import java.util.Comparator;
import java.util.List;

import org.cytoscape.view.model.internal.model.snapshot.CyNetworkViewSnapshotImpl;

import com.github.davidmoten.rtree.Entry;
import com.github.davidmoten.rtree.RTree;
import com.github.davidmoten.rtree.geometry.Rectangle;

import io.vavr.collection.Map;

public class SpacialIndex2DSnapshotImpl extends SpacialIndex2DBase<Long> {
	
	private final RTree<Long,Rectangle> rtree;
	private final Map<Long,Rectangle> geometries;
	
	private CyNetworkViewSnapshotImpl snapshot = null;
	private Comparator<Entry<Long,?>> zOrderComparator;
	
	public SpacialIndex2DSnapshotImpl(RTree<Long,Rectangle> rtree, Map<Long,Rectangle> geometries) {
		this.rtree = rtree;
		this.geometries = geometries;
		this.zOrderComparator = createComparator();
	}

	public void setSnapshot(CyNetworkViewSnapshotImpl snapshot) {
		this.snapshot = snapshot;
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
	
	@Override
	protected List<Entry<Long,Rectangle>> maybeSort(List<Entry<Long,Rectangle>> list) {
		if(snapshot == null)
			return super.maybeSort(list);
		list.sort(zOrderComparator);
		return list;
	}
	
	private Comparator<Entry<Long,?>> createComparator() {
		Comparator<Entry<Long,?>> comparator;
		comparator = Comparator.comparing(entry -> snapshot.getZ(entry.value()));
//		comparator = comparator.thenComparing(entry -> entry.value()); // if z-order is the same break the tie using suid
		return comparator;
	}
}
