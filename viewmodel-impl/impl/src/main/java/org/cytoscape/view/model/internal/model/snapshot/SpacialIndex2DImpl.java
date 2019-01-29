package org.cytoscape.view.model.internal.model.snapshot;

import java.util.Iterator;

import org.cytoscape.view.model.spacial.SpacialIndex2D;
import org.cytoscape.view.model.spacial.SpacialIndex2DEnumerator;

import com.github.davidmoten.guavamini.Optional;
import com.github.davidmoten.rtree.Entry;
import com.github.davidmoten.rtree.RTree;
import com.github.davidmoten.rtree.geometry.Rectangle;
import com.github.davidmoten.rtree.geometry.internal.RectangleFloat;

import io.vavr.collection.Map;
import rx.Observable;

public class SpacialIndex2DImpl implements SpacialIndex2D {

	// MKTODO what to return if the rtree is empty????
	private static final Rectangle EMPTY_MBR = RectangleFloat.create(0, 0, 0, 0);
	
	private final RTree<Long,Rectangle> rtree;
	private final Map<Long,Rectangle> geometries;
	
	
	public SpacialIndex2DImpl(RTree<Long,Rectangle> rtree, Map<Long,Rectangle> geometries) {
		this.rtree = rtree;
		this.geometries = geometries;
	}
	
	
	@Override
	public void getMBR(float[] extents) {
		Optional<Rectangle> mbrOpt = rtree.mbr();
		Rectangle mbr =  mbrOpt.isPresent() ? mbrOpt.get() : EMPTY_MBR;
		copyExtents(mbr, extents);
	}

	
	@Override
	public boolean exists(long suid) {
		return geometries.containsKey(suid);
	}
	
	
	@Override
	public SpacialIndex2DEnumerator queryOverlap(float xMin, float yMin, float xMax, float yMax) {
		Observable<Entry<Long,Rectangle>> overlap = rtree.search(RectangleFloat.create(xMin, yMin, xMax, yMax));
		return new SpacialIndex2DEnumeratorImpl(overlap);
	}
	
	
	private static void copyExtents(Rectangle r, float[] extents) {
		if(extents != null) {
			extents[X_MIN] = (float) r.x1();
			extents[Y_MIN] = (float) r.y1();
			extents[X_MAX] = (float) r.x2();
			extents[Y_MAX] = (float) r.y2();
		}
	}
	
	
	private class SpacialIndex2DEnumeratorImpl implements SpacialIndex2DEnumerator {
		
		private final Iterator<Entry<Long,Rectangle>> overlap;
		
		public SpacialIndex2DEnumeratorImpl(Observable<Entry<Long, Rectangle>> overlap) {
			this.overlap = overlap.toBlocking().getIterator();
		}

		@Override
		public boolean hasNext() {
			return overlap.hasNext();
		}
		
		@Override
		public long getNextExtents(float[] extents) {
			Entry<Long,Rectangle> entry = overlap.next();
			copyExtents(entry.geometry(), extents);
			return entry.value();
		}

	}
}
