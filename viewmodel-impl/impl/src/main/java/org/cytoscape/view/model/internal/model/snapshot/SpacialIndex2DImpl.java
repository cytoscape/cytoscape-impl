package org.cytoscape.view.model.internal.model.snapshot;

import java.util.Iterator;

import org.cytoscape.view.model.spacial.SpacialIndex2D;
import org.cytoscape.view.model.spacial.SpacialIndex2DEnumerator;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;

import com.github.davidmoten.guavamini.Optional;
import com.github.davidmoten.rtree.Entry;
import com.github.davidmoten.rtree.RTree;
import com.github.davidmoten.rtree.geometry.Rectangle;
import com.github.davidmoten.rtree.geometry.internal.RectangleFloat;

import io.vavr.Tuple2;
import io.vavr.collection.Map;
import io.vavr.control.Option;
import rx.Observable;

public class SpacialIndex2DImpl implements SpacialIndex2D {

	// MKTODO what to return if the rtree is empty????
	private static final Rectangle EMPTY_MBR = RectangleFloat.create(0, 0, 0, 0);
	
	private final CyNetworkViewSnapshotImpl snapshot;
	private final RTree<Long,Rectangle> rtree;
	private final Map<Long,Rectangle> geometries;
	
	
	
	public SpacialIndex2DImpl(CyNetworkViewSnapshotImpl snapshot, RTree<Long,Rectangle> rtree, Map<Long,Rectangle> geometries) {
		this.snapshot = snapshot;
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
	public boolean get(long suid, float[] extents) {
		Option<Rectangle> r = geometries.get(suid);
		if(r.isDefined()) {
			copyExtents(r.get(), extents);
			return true;
		}
		return false;
	}
	
	@Override
	public SpacialIndex2DEnumerator queryAll() {
		return new AllEntriesEnumeratorImpl();
	}
	
	@Override
	public SpacialIndex2DEnumerator queryOverlap(float xMin, float yMin, float xMax, float yMax) {
		Observable<Entry<Long,Rectangle>> overlap = rtree.search(RectangleFloat.create(xMin, yMin, xMax, yMax));
		return new SearchResultEnumeratorImpl(overlap);
	}
	
	@Override
	public double getZOrder(long suid) {
		Number z = (Number) snapshot.getVisualProperties(suid).getOrElse(BasicVisualLexicon.NODE_Z_LOCATION, 0.0);
		return z == null ? 0.0 : z.doubleValue(); // being extra cautious
	}
	
	private static void copyExtents(Rectangle r, float[] extents) {
		if(extents != null) {
			extents[X_MIN] = (float) r.x1();
			extents[Y_MIN] = (float) r.y1();
			extents[X_MAX] = (float) r.x2();
			extents[Y_MAX] = (float) r.y2();
		}
	}
	
	
	
	private class AllEntriesEnumeratorImpl implements SpacialIndex2DEnumerator {

		private io.vavr.collection.Iterator<Tuple2<Long,Rectangle>> iterator;

		public AllEntriesEnumeratorImpl() {
			iterator = geometries.iterator();
		}
		
		@Override
		public int size() {
			return geometries.size();
		}

		@Override
		public boolean hasNext() {
			return iterator.hasNext();
		}

		@Override
		public long nextExtents(float[] extents) {
			Tuple2<Long,Rectangle> next = iterator.next();
			copyExtents(next._2(), extents);
			return next._1();
		}
		
	}
	
	
	private static class SearchResultEnumeratorImpl implements SpacialIndex2DEnumerator {
		
		private final int size;
		private final Iterator<Entry<Long,Rectangle>> overlap;
		
		public SearchResultEnumeratorImpl(Observable<Entry<Long, Rectangle>> overlap) {
			this.size = overlap.count().toBlocking().first();
			this.overlap = overlap.toBlocking().getIterator();
		}

		@Override
		public int size() {
			return size;
		}
		
		@Override
		public boolean hasNext() {
			return overlap.hasNext();
		}
		
		@Override
		public long nextExtents(float[] extents) {
			Entry<Long,Rectangle> entry = overlap.next();
			copyExtents(entry.geometry(), extents);
			return entry.value();
		}

	}
}
