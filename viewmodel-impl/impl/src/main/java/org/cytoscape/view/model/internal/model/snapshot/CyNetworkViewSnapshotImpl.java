package org.cytoscape.view.model.internal.model.snapshot;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.model.internal.model.CyViewImpl;

import io.vavr.collection.HashMap;
import io.vavr.collection.Map;

public class CyNetworkViewSnapshotImpl extends CyViewSnapshotBase<CyNetwork> implements CyNetworkView {
	
	private final String rendererId;

	// Key is SUID of underlying model object.
	private final Map<Long,CyViewImpl<CyNode>> nodeViewMap;
	private final Map<Long,CyViewImpl<CyEdge>> edgeViewMap;
		
	// Key is SUID of View object
	private final Map<Long,Map<VisualProperty<?>,Object>> visualProperties;
	private final Map<Long,Map<VisualProperty<?>,Object>> allLocks;
	private final Map<Long,Map<VisualProperty<?>,Object>> directLocks;
	private final Map<VisualProperty<?>,Object> defaultValues;
	
	public CyNetworkViewSnapshotImpl(
			Long suid,
			CyNetwork network,
			String rendererId, 
			Map<Long,CyViewImpl<CyNode>> nodeViewMap,
			Map<Long,CyViewImpl<CyEdge>> edgeViewMap,
			Map<VisualProperty<?>, Object> defaultValues,
			Map<Long, Map<VisualProperty<?>, Object>> visualProperties,
			Map<Long, Map<VisualProperty<?>, Object>> allLocks,
			Map<Long, Map<VisualProperty<?>, Object>> directLocks) 
	{
		super(network, suid);
		this.rendererId = rendererId;
		this.nodeViewMap = nodeViewMap;
		this.edgeViewMap = edgeViewMap;
		this.defaultValues = defaultValues;
		this.visualProperties = visualProperties;
		this.allLocks = allLocks;
		this.directLocks = directLocks;
	}

	
	@Override
	public CyNetworkViewSnapshotImpl getNetworkSnapshot() {
		return this;
	}
	

	public Map<VisualProperty<?>, Object> getVisualProperties(Long suid) {
		return visualProperties.getOrElse(suid, HashMap.empty());
	}
	
	public Map<VisualProperty<?>, Object> getAllLocks(Long suid) {
		return allLocks.getOrElse(suid, HashMap.empty());
	}
	
	public Map<VisualProperty<?>, Object> getDirectLocks(Long suid) {
		return directLocks.getOrElse(suid, HashMap.empty());
	}

	public Map<VisualProperty<?>,Object> getDefaultValues() {
		return defaultValues;
	}

	
	@Override
	public View<CyNode> getNodeView(CyNode node) {
		CyViewImpl<CyNode> view = nodeViewMap.getOrElse(node.getSUID(), null);
		if(view == null)
			return null;
		return new CyViewSnapshotImpl<CyNode>(this, view.getModel(), view.getSUID());
	}

	@Override
	public Collection<View<CyNode>> getNodeViews() {
		List<View<CyNode>> nodeViews = new ArrayList<>(nodeViewMap.size());
		for(CyViewImpl<CyNode> view : nodeViewMap.values()) {
			nodeViews.add(new CyViewSnapshotImpl<CyNode>(this, view.getModel(), view.getSUID()));
		}
		return nodeViews;
	}

	@Override
	public View<CyEdge> getEdgeView(CyEdge edge) {
		CyViewImpl<CyEdge> view = edgeViewMap.getOrElse(edge.getSUID(), null);
		if(view == null)
			return null;
		return new CyViewSnapshotImpl<CyEdge>(this, view.getModel(), view.getSUID());
	}

	@Override
	public Collection<View<CyEdge>> getEdgeViews() {
		List<View<CyEdge>> edgeViews = new ArrayList<>(edgeViewMap.size());
		for(CyViewImpl<CyEdge> view : edgeViewMap.values()) {
			edgeViews.add(new CyViewSnapshotImpl<CyEdge>(this, view.getModel(), view.getSUID()));
		}
		return edgeViews;
	}

	@Override
	public Collection<View<? extends CyIdentifiable>> getAllViews() {
		ArrayList<View<? extends CyIdentifiable>> list = new ArrayList<>();
		list.addAll(getNodeViews());
		list.addAll(getEdgeViews());
		list.add(this);
		return list;
	}


	@Override
	public void clearVisualProperties() {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public <T, V extends T> void setLockedValue(VisualProperty<? extends T> vp, V value) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public <T, V extends T> void setVisualProperty(VisualProperty<? extends T> vp, V value) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public <T, V extends T> void setViewDefault(VisualProperty<? extends T> vp, V defaultValue) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public void fitContent() {
	}

	@Override
	public void fitSelected() {
	}

	@Override
	public void updateView() {
	}

	@Override
	public String getRendererId() {
		return rendererId;
	}
	
	@Override
	public void dispose() {
	}

}
