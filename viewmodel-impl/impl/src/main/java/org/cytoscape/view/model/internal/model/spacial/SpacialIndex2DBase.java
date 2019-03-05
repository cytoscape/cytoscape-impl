package org.cytoscape.view.model.internal.model.spacial;

import java.util.Iterator;

import org.cytoscape.view.model.spacial.SpacialIndex2D;
import org.cytoscape.view.model.spacial.SpacialIndex2DEnumerator;

import com.github.davidmoten.guavamini.Optional;
import com.github.davidmoten.rtree.Entry;
import com.github.davidmoten.rtree.RTree;
import com.github.davidmoten.rtree.geometry.Rectangle;
import com.github.davidmoten.rtree.geometry.internal.RectangleFloat;

import io.vavr.Tuple2;
import io.vavr.collection.Map;
import io.vavr.control.Option;
import rx.Observable;

abstract class SpacialIndex2DBase<T> implements SpacialIndex2D<T> {
	
	// MKTODO what to return if the rtree is empty????
	private static final Rectangle EMPTY_MBR = RectangleFloat.create(0, 0, 0, 0);
	
	protected abstract RTree<T,Rectangle> getRTree();
	protected abstract Map<T,Rectangle> getGeometries();
	
	@Override
	public void getMBR(float[] extents) {
		Optional<Rectangle> mbrOpt = getRTree().mbr();
		Rectangle mbr =  mbrOpt.isPresent() ? mbrOpt.get() : EMPTY_MBR;
		copyExtents(mbr, extents);
	}
	
	@Override
	public void getMBR(double[] extents) {
		Optional<Rectangle> mbrOpt = getRTree().mbr();
		Rectangle mbr =  mbrOpt.isPresent() ? mbrOpt.get() : EMPTY_MBR;
		copyExtents(mbr, extents);
	}

	@Override
	public boolean exists(T suid) {
		return getGeometries().containsKey(suid);
	}
	
	@Override
	public boolean get(T suid, float[] extents) {
		Option<Rectangle> r = getGeometries().get(suid);
		if(r.isDefined()) {
			copyExtents(r.get(), extents);
			return true;
		}
		return false;
	}
	
	@Override
	public boolean get(T suid, double[] extents) {
		Option<Rectangle> r = getGeometries().get(suid);
		if(r.isDefined()) {
			copyExtents(r.get(), extents);
			return true;
		}
		return false;
	}
	
	@Override
	public int size() {
		return getGeometries().size();
	}
	
	@Override
	public SpacialIndex2DEnumerator<T> queryAll() {
		return new AllEntriesEnumeratorImpl();
	}
	
	@Override
	public SpacialIndex2DEnumerator<T> queryOverlap(float xMin, float yMin, float xMax, float yMax) {
		Observable<Entry<T,Rectangle>> overlap = getRTree().search(RectangleFloat.create(xMin, yMin, xMax, yMax));
		return new SearchResultEnumeratorImpl(overlap);
	}
	
	
	private static void copyExtents(Rectangle r, float[] extents) {
		if(extents != null) {
			extents[X_MIN] = (float) r.x1();
			extents[Y_MIN] = (float) r.y1();
			extents[X_MAX] = (float) r.x2();
			extents[Y_MAX] = (float) r.y2();
		}
	}
	
	private static void copyExtents(Rectangle r, double[] extents) {
		if(extents != null) {
			extents[X_MIN] = r.x1();
			extents[Y_MIN] = r.y1();
			extents[X_MAX] = r.x2();
			extents[Y_MAX] = r.y2();
		}
	}
	
	private class AllEntriesEnumeratorImpl implements SpacialIndex2DEnumerator<T> {

		private final io.vavr.collection.Iterator<Tuple2<T,Rectangle>> iterator;
		private final int size;
		
		public AllEntriesEnumeratorImpl() {
			Map<T, Rectangle> geometries = getGeometries();
			iterator = geometries.iterator();
			size = geometries.size();
		}
		
		@Override
		public int size() {
			return size;
		}

		@Override
		public boolean hasNext() {
			return iterator.hasNext();
		}

		@Override
		public T nextExtents(float[] extents) {
			Tuple2<T,Rectangle> next = iterator.next();
			copyExtents(next._2(), extents);
			return next._1();
		}
	}
	
	
	private static class SearchResultEnumeratorImpl<T> implements SpacialIndex2DEnumerator<T> {
		
		private final int size;
		private final Iterator<Entry<T,Rectangle>> overlap;
		
		public SearchResultEnumeratorImpl(Observable<Entry<T, Rectangle>> overlap) {
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
		public T nextExtents(float[] extents) {
			Entry<T,Rectangle> entry = overlap.next();
			copyExtents(entry.geometry(), extents);
			return entry.value();
		}
	}
	
}
