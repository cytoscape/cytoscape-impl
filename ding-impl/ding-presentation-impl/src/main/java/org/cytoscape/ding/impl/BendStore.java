package org.cytoscape.ding.impl;

import static org.cytoscape.view.presentation.property.BasicVisualLexicon.EDGE_BEND;

import java.awt.geom.Point2D;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.cytoscape.ding.impl.DRenderingEngine.UpdateType;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.model.CyEdge;
import org.cytoscape.view.model.CyNetworkViewSnapshot;
import org.cytoscape.view.model.SnapshotEdgeInfo;
import org.cytoscape.view.model.View;
import org.cytoscape.view.model.events.ViewChangeRecord;
import org.cytoscape.view.model.events.ViewChangedEvent;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.presentation.property.values.Bend;
import org.cytoscape.view.presentation.property.values.Handle;
import org.cytoscape.view.presentation.property.values.HandleFactory;

public class BendStore {

	private final DRenderingEngine re;
	private final HandleFactory handleFactory;
	private final CyEventHelper eventHelper;
	
	private Map<Long,Set<HandleInfo>> selectedHandles = new HashMap<>();
	
	public BendStore(DRenderingEngine re, CyEventHelper eventHelper, HandleFactory handleFactory) {
		this.re = re;
		this.eventHelper = eventHelper;
		this.handleFactory = handleFactory;
	}
	
	
	public boolean isHandleSelected(HandleInfo key) {
		Set<HandleInfo> handles = selectedHandles.get(key.getSUID());
		if(handles == null)
			return false;
		return handles.contains(key);
	}
	
	public void selectHandle(HandleInfo key) {
		if(key == null)
			return;
		selectedHandles.computeIfAbsent(key.getSUID(), k->new HashSet<>()).add(key);
	}
	
	public void unselectHandle(HandleInfo key) {
		Set<HandleInfo> handles = selectedHandles.get(key.getSUID());
		if(handles == null)
			return;
		handles.remove(key);
		if(handles.isEmpty())
			selectedHandles.remove(key.getSUID());
	}
	
	public void unselectAllHandles() {
		selectedHandles.clear();
	}
	
	public boolean areHandlesSelected() {
		return !selectedHandles.isEmpty();
	}
	
//	public Set<HandleInfo> getSelectedHandles() {
//		// MKTODO only do this on content changed?
//		Iterator<HandleInfo> iter = selectedHandles.iterator();
//		var snapshot = re.getViewModelSnapshot();
//		while(iter.hasNext()) {
//			var handle = iter.next();
//			var ev = snapshot.getEdgeView(handle.getSUID());
//			if(!re.getEdgeDetails().isSelected(ev)) {
//				iter.remove();
//			}
//		}
//		
//		return Collections.unmodifiableSet(selectedHandles);
//	}
	
	
//	for(HandleInfo handleKey : anchorsToMove) {
//	Bend bend = handleKey.getBend();
//
//	//This test is necessary because in some instances, an anchor can still be present in the selected
//	//anchor list, even though the anchor has been removed. A better fix would be to remove the
//	//anchor from that list before this code is ever reached. However, this is not currently possible
//	//under the present API, so for now we just detect this situation and continue.
//	if(bend == null || bend.getAllHandles().isEmpty())
//		continue;
//	
//	Handle handle = handleKey.getHandle();
//	var ev = snapshot.getMutableEdgeView(handleKey.getSUID());
//	Point2D newPoint = handle.calculateHandleLocation(re.getViewModel(), ev);
//	
//	float x = (float) newPoint.getX();
//	float y = (float) newPoint.getY();
//	
//	re.getBendStore().moveHandle(handleKey, x + (float)deltaX, y + (float)deltaY);
//}
	
	
	
	
	public void moveSelectedHandles(float dx, float dy) {
		bypassAllSelectedBends();
		
		for(Map.Entry<Long,Set<HandleInfo>> entry : selectedHandles.entrySet()) {
			View<CyEdge> mutableEdgeView = re.getViewModelSnapshot().getMutableEdgeView(entry.getKey());
			for(HandleInfo handleInfo : entry.getValue()) {
				Handle handle = handleInfo.getHandle();
				Point2D newPoint = handle.calculateHandleLocation(re.getViewModel(), mutableEdgeView);
				handle.defineHandle(re.getViewModel(), mutableEdgeView, newPoint.getX() + dx, newPoint.getY() + dy);
			}
		}
	}
	
	
	private void bypassAllSelectedBends() {
		for(long suid : new HashSet<>(selectedHandles.keySet())) {
			View<CyEdge> mutableEdgeView = re.getViewModelSnapshot().getMutableEdgeView(suid);
			if(mutableEdgeView == null) {
				continue;
			}
			
			// create a bypass
			Bend lockedBend = createLockedBend(mutableEdgeView);
			if(lockedBend != null) {
				// update all the selected handles to use the bypass bend
				Set<HandleInfo> newHandleKeys = new HashSet<>();
				for(HandleInfo key : selectedHandles.get(suid)) {
					List<Handle> allHandles = lockedBend.getAllHandles();
					int index = key.getHandleIndex();
					if(index >= 0 && index < allHandles.size()) {
						Handle newHandle = allHandles.get(key.getHandleIndex());
						HandleInfo newKey = new HandleInfo(suid, lockedBend, newHandle);
						newHandleKeys.add(newKey);
					}
				}
				
				selectedHandles.put(suid, newHandleKeys);
			}
		}
	}
	

	private Bend createLockedBend(View<CyEdge> mutableEdgeView) {
		if(mutableEdgeView == null)
			return null;
		if(mutableEdgeView.isValueLocked(EDGE_BEND))
			return null;
		
		// must be the default or a mapping (probably created by the "bundle edges" option)
		final Bend bend = mutableEdgeView.getVisualProperty(EDGE_BEND);
		final Bend lockedBend;
		if(bend == EDGE_BEND.getDefault()) // The default Bend that's part of the lexicon is not an instance of BendImpl.
			lockedBend = new BendImpl();
		else
			lockedBend = new BendImpl((BendImpl)bend); // copy handles
		
		mutableEdgeView.setLockedValue(EDGE_BEND, lockedBend);
		return lockedBend;
	}
	
	private Bend getOrCreateLockedBend(View<CyEdge> mutableEdgeView) {
		if(mutableEdgeView == null)
			return null;
		if(mutableEdgeView.isValueLocked(EDGE_BEND))
			return mutableEdgeView.getVisualProperty(EDGE_BEND);
		
		return createLockedBend(mutableEdgeView);
	}
	
	public HandleInfo addHandle(View<CyEdge> edge, Point2D pt) {
		View<CyEdge> mutableEdgeView = re.getViewModelSnapshot().getMutableEdgeView(edge.getSUID());
		Bend bend = getOrCreateLockedBend(mutableEdgeView);
		if(mutableEdgeView != null && bend != null) {
			int index = getBestHandleIndex(bend, edge, pt);
			if(index < 0) {
				index = 0; // Index of this handle, which is first (0)
			}
			Handle handle = handleFactory.createHandle(re.getViewModel(), mutableEdgeView, pt.getX(), pt.getY());
			bend.insertHandleAt(index, handle);
			
			fireViewChangeEvent(mutableEdgeView, bend);
			
			re.updateView(UpdateType.ALL_FULL, true);
			
			return new HandleInfo(edge, bend, handle);
		}
		
		return null;
	}
	
	
	private Point2D getNodeOffset(long nodeViewSuid) {
		CyNetworkViewSnapshot snapshot = re.getViewModelSnapshot();
		double[] extentsBuff = new double[4];
		boolean exists = snapshot.getSpacialIndex2D().get(nodeViewSuid, extentsBuff);
		if(!exists)
			return null;
		double xCenter = (extentsBuff[0] + extentsBuff[2]) / 2.0d;
		double yCenter = (extentsBuff[1] + extentsBuff[3]) / 2.0d;
		return new Point2D.Double(xCenter, yCenter);
	}
	
	
	private int getBestHandleIndex(Bend bend, View<CyEdge> edge, Point2D pt) {
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
	
	public void removeHandle(HandleInfo key) {
		unselectHandle(key);
		View<CyEdge> mutableEdgeView = re.getViewModelSnapshot().getMutableEdgeView(key.getSUID());
		if(mutableEdgeView != null) {
			Bend bend = getOrCreateLockedBend(mutableEdgeView);
			if(bend != null) {
				int index = key.getHandleIndex();
				bend.removeHandleAt(index);
				fireViewChangeEvent(mutableEdgeView, bend);
			}
		}
	}
	
	
	@SuppressWarnings("unchecked")
	private void fireViewChangeEvent(View<CyEdge> mutableEdgeView, Bend bend) {
		var record = new ViewChangeRecord<>(mutableEdgeView, BasicVisualLexicon.EDGE_BEND, bend, true);
		eventHelper.addEventPayload(re.getViewModel(), record, ViewChangedEvent.class);
	}
	
}
