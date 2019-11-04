package org.cytoscape.ding.impl;

import java.awt.geom.Point2D;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.cytoscape.model.CyEdge;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewSnapshot;
import org.cytoscape.view.model.SnapshotEdgeInfo;
import org.cytoscape.view.model.View;
import org.cytoscape.view.presentation.property.values.Bend;
import org.cytoscape.view.presentation.property.values.Handle;
import org.cytoscape.view.presentation.property.values.HandleFactory;

public class BendStore {

	private final DRenderingEngine re;
	private final HandleFactory handleFactory;
	
	private Set<HandleInfo> selectedHandles = new HashSet<>();
	
	public BendStore(DRenderingEngine re, HandleFactory handleFactory) {
		this.re = re;
		this.handleFactory = handleFactory;
	}
	
	
	public boolean isHandleSelected(HandleInfo key) {
		return selectedHandles.contains(key);
	}
	
	public void selectHandle(HandleInfo key) {
		selectedHandles.add(key);
	}
	
	public void unselectHandle(HandleInfo key) {
		selectedHandles.remove(key);
	}
	
	public void unselectAllHandles() {
		selectedHandles.clear();
	}
	
	public Set<HandleInfo> getSelectedHandles() {
		return Collections.unmodifiableSet(selectedHandles);
	}
	
	
	public void moveHandle(HandleInfo handleInfo, float x, float y) {
		Handle handle = handleInfo.getHandle();
		View<CyEdge> edge = handleInfo.getEdge();
		handle.defineHandle(re.getViewModelSnapshot(), edge, x, y);
	}
	
	
	public HandleInfo addHandle(View<CyEdge> edge, Point2D pt) {
		int index = getBestHandleIndex(edge, pt);
		if(index < 0) {
			index = 0; // Index of this handle, which is first (0)
		}
		
		// Add handle to the bend
		Bend bend = re.getEdgeDetails().getBend(edge, true);
		CyNetworkView netView = re.getViewModelSnapshot();
		Handle handle = handleFactory.createHandle(netView, edge, pt.getX(), pt.getY());
		bend.insertHandleAt(index, handle);
		re.setContentChanged();
		
		return new HandleInfo(edge, bend, handle);
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

	
	public void removeHandle(HandleInfo key) {
		selectedHandles.remove(key);
		Bend bend = key.getBend();
		int index = bend.getIndex(key.getHandle());
		bend.removeHandleAt(index);
	}
	
}
