package org.cytoscape.ding.impl;

import static org.cytoscape.view.presentation.property.BasicVisualLexicon.EDGE_BEND;

import java.awt.geom.Point2D;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

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
	
	private Set<HandleInfo> selectedHandles = new HashSet<>();
	
	public BendStore(DRenderingEngine re, CyEventHelper eventHelper, HandleFactory handleFactory) {
		this.re = re;
		this.eventHelper = eventHelper;
		this.handleFactory = handleFactory;
	}
	
	
	public boolean isHandleSelected(HandleInfo key) {
		return selectedHandles.contains(key);
	}
	
	public void selectHandle(HandleInfo key) {
		if(key != null)
			selectedHandles.add(key);
	}
	
	public void unselectHandle(HandleInfo key) {
		selectedHandles.remove(key);
	}
	
	public void unselectAllHandles() {
		selectedHandles.clear();
	}
	
	public Set<HandleInfo> getSelectedHandles() {
		// MKTODO only do this on content changed?
		Iterator<HandleInfo> iter = selectedHandles.iterator();
		var snapshot = re.getViewModelSnapshot();
		while(iter.hasNext()) {
			var handle = iter.next();
			var ev = snapshot.getEdgeView(handle.getSUID());
			if(!re.getEdgeDetails().isSelected(ev)) {
				iter.remove();
			}
		}
		
		return Collections.unmodifiableSet(selectedHandles);
	}
	
	
	public void moveHandle(HandleInfo handleInfo, float x, float y) {
		View<CyEdge> mutableEdgeView = re.getViewModelSnapshot().getMutableEdgeView(handleInfo.getSUID());
		if(mutableEdgeView != null) {
			Handle handle = handleInfo.getHandle();
			handle.defineHandle(re.getViewModel(), mutableEdgeView, x, y);
		}
	}
	
	
	private Bend getOrCreateLockedBend(View<CyEdge> mutableEdgeView) {
		if(mutableEdgeView == null)
			return null;
		if(mutableEdgeView.isValueLocked(EDGE_BEND))
			return mutableEdgeView.getVisualProperty(EDGE_BEND);
			
		Bend defaultBend = re.getViewModelSnapshot().getViewDefault(EDGE_BEND);
		Bend bend;
		if(defaultBend == EDGE_BEND.getDefault())
			bend = new BendImpl();
		else
			bend = new BendImpl((BendImpl)defaultBend); // copy handles from default bend
		
		mutableEdgeView.setLockedValue(EDGE_BEND, bend);
		return bend;
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
			
			re.setContentChanged();
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
		selectedHandles.remove(key);
		View<CyEdge> mutableEdgeView = re.getViewModelSnapshot().getMutableEdgeView(key.getSUID());
		if(mutableEdgeView != null) {
			Bend bend = getOrCreateLockedBend(mutableEdgeView);
			if(bend != null) {
				int index = bend.getIndex(key.getHandle());
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
