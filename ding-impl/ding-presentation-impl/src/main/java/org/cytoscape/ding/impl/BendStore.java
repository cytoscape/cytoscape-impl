package org.cytoscape.ding.impl;

import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.cytoscape.graph.render.stateful.EdgeDetails;
import org.cytoscape.model.CyEdge;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewSnapshot;
import org.cytoscape.view.model.SnapshotEdgeInfo;
import org.cytoscape.view.model.View;
import org.cytoscape.view.presentation.property.values.Bend;
import org.cytoscape.view.presentation.property.values.Handle;
import org.cytoscape.view.presentation.property.values.HandleFactory;

public class BendStore {

	// Size of square for moving handle
	public static final float HANDLE_SIZE = 12.0f;
	private static final float HALF_SIZE = HANDLE_SIZE / 2.0f;
	
	private final DRenderingEngine re;
	private final HandleFactory handleFactory;
	
	private Map<Long,List<HandleKey>> selectedEdges = new HashMap<>();
	private Set<HandleKey> selectedHandles = new HashSet<>();
	private Map<HandleKey,Rectangle2D> handleIndex = new HashMap<>();
	
	
	public BendStore(DRenderingEngine re, HandleFactory handleFactory) {
		this.re = re;
		this.handleFactory = handleFactory;
	}
	
	
	/**
	 * Each edge can have multiple anchors (handles). To identify an 
	 * anchor we use the SUID of the edge and the index of the anchor.
	 */
	public static class HandleKey {
		
		private final long suid;
		private final int index;
		
		public HandleKey(long suid, int index) {
			this.suid = suid;
			this.index = index;
		}

		@Override
		public int hashCode() {
			return Objects.hash(index, suid);
		}

		@Override
		public boolean equals(Object obj) {
			HandleKey other = (HandleKey) obj;
			return index == other.index && suid == other.suid;
		}

		public long getEdgeSuid() {
			return suid;
		}

		public int getHandleIndex() {
			return index;
		}
	}


	private View<CyEdge> getEdge(HandleKey key) {
		return re.getViewModelSnapshot().getEdgeView(key.getEdgeSuid());
	}
	
	/**
	 * This should be called by DRenderingEngine whenever the snapshot changes.
	 */
	public void updateSelectedEdges(Collection<View<CyEdge>> selectedEdgeViews) {
		Set<Long> oldSelectedEdges = new HashSet<>(this.selectedEdges.keySet());
		
		for(View<CyEdge> edge : selectedEdgeViews) {
			oldSelectedEdges.remove(edge.getSUID());
		}
		for(Long suid : oldSelectedEdges) {
			unselectEdge(suid);
		}
		for(View<CyEdge> edge : selectedEdgeViews) {
			selectEdge(edge);
		}
	}
	
	
	public HandleKey pickHandle(float x, float y) {
		for(var entry : handleIndex.entrySet()) {
			Rectangle2D area = entry.getValue();
			if(area.contains(x, y)) {
				return entry.getKey();
			}
		}
		return null;
	}
	
	public List<HandleKey> queryOverlap(Rectangle2D selectionRectangle) {
		if(handleIndex.isEmpty())
			return Collections.emptyList();
		
		List<HandleKey> handles = new ArrayList<>();
		for(var entry : handleIndex.entrySet()) {
			Rectangle2D area = entry.getValue();
			if(area.intersects(selectionRectangle)) {
				handles.add(entry.getKey());
			}
		}
		return handles;
	}
	
	public List<HandleKey> queryOverlap(GeneralPath path) {
		if(handleIndex.isEmpty())
			return Collections.emptyList();
		
		List<HandleKey> handles = new ArrayList<>();
		for(var entry : handleIndex.entrySet()) {
			Rectangle2D handleArea = entry.getValue();
			if(path.intersects(handleArea)) {
				handles.add(entry.getKey());
			}
		}
		return handles;
	}
	
	
	public boolean isHandleSelected(HandleKey key) {
		return selectedHandles.contains(key);
	}
	
	public void selectHandle(HandleKey key) {
		selectedHandles.add(key);
	}
	
	public void unselectHandle(HandleKey key) {
		selectedHandles.remove(key);
	}
	
	public void unselectAllHandles() {
		selectedHandles.clear();
	}
	
	public Set<HandleKey> getSelectedHandles() {
		return Collections.unmodifiableSet(selectedHandles);
	}
	
	// Should synchronize around m_view.m_lock.
	private void selectEdge(View<CyEdge> edge) {
		Long suid = edge.getSUID();
		if(!selectedEdges.containsKey(suid)) {
			selectedEdges.put(suid, new ArrayList<>());

			List<Handle> handles = re.getEdgeDetails().getBend(edge).getAllHandles();
			
			for(int j = 0; j < handles.size(); j++) {
				Handle handle = handles.get(j);
				Point2D newPoint = handle.calculateHandleLocation(re.getViewModelSnapshot(), edge);
				float x = (float) newPoint.getX();
				float y = (float) newPoint.getY();
				
				HandleKey key = new HandleKey(suid, j);
				
				selectedEdges.get(suid).add(key);
				handleIndex.put(key, new Rectangle2D.Float(x - HALF_SIZE, y - HALF_SIZE, HANDLE_SIZE, HANDLE_SIZE));
			}
		}
	}
	
	private void unselectEdge(Long suid) {
		if(selectedEdges.containsKey(suid)) {
			List<HandleKey> keys = selectedEdges.remove(suid);
			for(HandleKey k : keys) {
				handleIndex.remove(k);
			}
		}
	}
	
	public List<HandleKey> getHandles(View<CyEdge> edge) {
		return selectedEdges.get(edge.getSUID());
	}
	
	public boolean hasHandles(View<CyEdge> edge) {
		return selectedEdges.containsKey(edge.getSUID());
	}
	
	public void moveHandle(HandleKey key, float x, float y) {
		if(handleIndex.containsKey(key)) {
			View<CyEdge> edge = getEdge(key);
			final Bend bend = re.getEdgeDetails().getBend(edge);
			final HandleImpl handle = (HandleImpl) bend.getAllHandles().get(key.getHandleIndex());
			handle.defineHandle(re.getViewModelSnapshot(), edge, x, y);
			
			handleIndex.put(key, new Rectangle2D.Float(x - HALF_SIZE, y - HALF_SIZE, HANDLE_SIZE, HANDLE_SIZE));
		}
	}
	
	
	public HandleKey addHandle(View<CyEdge> edge, Point2D pt) {
		int bestIndex = getBestHandleIndex(edge, pt);
		if(bestIndex < 0) {
			bestIndex = 0; // Index of this handle, which is first (0)
		}
		addHandleInternal(edge, pt, bestIndex);
		return new HandleKey(edge.getSUID(), bestIndex);
	}
	
	
	private Point2D getNodeOffset(long nodeViewSuid) {
		CyNetworkViewSnapshot snapshot = re.getViewModelSnapshot();
		double[] extentsBuff = new double[4];
		boolean exists = snapshot.getSpacialIndex2D().get(nodeViewSuid, extentsBuff);
		if (!exists)
			return null;
		double xCenter = (extentsBuff[0] + extentsBuff[2]) / 2.0d;
		double yCenter = (extentsBuff[1] + extentsBuff[3]) / 2.0d;
		return new Point2D.Double(xCenter, yCenter);
	}
	
	
	private int getBestHandleIndex(View<CyEdge> edge, Point2D pt) {
		Bend bend = re.getEdgeDetails().getBend(edge, true);
		List<Handle> handles = bend.getAllHandles();
		if(handles.isEmpty())
			return -1;
		
		CyNetworkViewSnapshot snapshot = re.getViewModelSnapshot();
		SnapshotEdgeInfo edgeInfo = snapshot.getEdgeInfo(edge);
		
		Point2D sourcePt = getNodeOffset(edgeInfo.getSourceViewSUID());
		Point2D targetPt = getNodeOffset(edgeInfo.getTargetViewSUID());
		
		Handle firstHandle = handles.get(0); 
		Point2D point = firstHandle.calculateHandleLocation(snapshot, edge);
		double bestDist = (pt.distance(sourcePt) + pt.distance(point)) - sourcePt.distance(point);
		int bestInx = 0;

		for (int i = 1; i < handles.size(); i++) {
			Handle handle1 = handles.get(i);
			Handle handle2 = handles.get(i-1);
			Point2D point1 = handle1.calculateHandleLocation(snapshot, edge);
			Point2D point2 = handle2.calculateHandleLocation(snapshot, edge);
			double distCand = (pt.distance(point2) + pt.distance(point1)) - point1.distance(point2);

			if (distCand < bestDist) {
				bestDist = distCand;
				bestInx = i;
			}
		}

		int lastIndex = handles.size() - 1;
		Handle lastHandle = handles.get(lastIndex);
		Point2D lastPoint = lastHandle.calculateHandleLocation(snapshot, edge);
		double lastCand = (pt.distance(targetPt) + pt.distance(lastPoint)) - targetPt.distance(lastPoint);

		if (lastCand < bestDist) {
			bestDist = lastCand;
			bestInx = handles.size();
		}
		
		return bestInx;
	}
	
	private void addHandleInternal(View<CyEdge> edge, Point2D handleLocation, int insertInx) {
		Long suid = edge.getSUID();
		Bend bend = re.getEdgeDetails().getBend(edge, true);
		CyNetworkView netView = re.getViewModelSnapshot();
		
		Handle handle = handleFactory.createHandle(netView, edge, handleLocation.getX(), handleLocation.getY());
		bend.insertHandleAt(insertInx, handle);

		selectedEdges.computeIfAbsent(suid, s -> new ArrayList<>()).add(insertInx, new HandleKey(suid, insertInx));
		
		if (re.getEdgeDetails().isSelected(edge)) {
			int n = bend.getAllHandles().size();
			// shift existing keys
			for (int j = n - 1; j > insertInx; j--) {
				HandleKey oldKey = new HandleKey(suid, j-1);
				HandleKey newKey = new HandleKey(suid, j);
				
				Rectangle2D area = handleIndex.remove(oldKey);
				handleIndex.put(newKey, area);
			}
			
			HandleKey key = new HandleKey(suid, insertInx);
			Rectangle2D area = new Rectangle2D.Float(
					(float) (handleLocation.getX() - HALF_SIZE),
					(float) (handleLocation.getY() - HALF_SIZE),
					HANDLE_SIZE, 
					HANDLE_SIZE);
					
			handleIndex.put(key, area);
		}

		re.setContentChanged();
	}
	
	
	public void getHandleLocation(HandleKey key, float[] pointBuff) {
		View<CyEdge> edge = getEdge(key);
		Bend bend = re.getEdgeDetails().getBend(edge, true);
		
		Handle handle;
		if (re.getEdgeDetails().getLineCurved(edge) == EdgeDetails.CURVED_LINES)
			handle = bend.getAllHandles().get(key.getHandleIndex());
		else
			handle = bend.getAllHandles().get(key.getHandleIndex()/2);

		Point2D newPoint = handle.calculateHandleLocation(re.getViewModelSnapshot(), edge);
		pointBuff[0] = (float) newPoint.getX();
		pointBuff[1] = (float) newPoint.getY();
	}
	
	
	public void removeHandle(HandleKey key) {
		View<CyEdge> edge = getEdge(key);
		
		Bend bend = re.getEdgeDetails().getBend(edge);
		bend.removeHandleAt(key.getHandleIndex());

		long suid = edge.getSUID();
		if(selectedEdges.containsKey(suid)) {
			handleIndex.remove(key);
			
			// shift all the anchors above idx down by 1
			// MKTODO really need a better way of doing this
			int n = bend.getAllHandles().size();

			List<HandleKey> keys = selectedEdges.get(suid);
			keys.subList(key.getHandleIndex(), n).clear();
			
			for(int j = key.getHandleIndex(); j < n; j++) {
				HandleKey prevKey = new HandleKey(suid, j+1);
				HandleKey newKey  = new HandleKey(suid, j);
				
				Rectangle2D area = handleIndex.remove(prevKey);
				handleIndex.put(newKey, area);
			}
		}
	}
	
}
