package org.cytoscape.view.model.internal;

import java.util.Collection;
import java.util.Collections;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.SUIDFactory;
import org.cytoscape.view.model.NullCyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.view.model.VisualProperty;

public class NullCyNetworkViewImpl implements NullCyNetworkView {
	private final Long suid;
	private final CyNetwork model;

	public NullCyNetworkViewImpl(CyNetwork model) {
		suid = SUIDFactory.getNextSUID();
		this.model = model;
	}
	
	@Override
	public View<CyNode> getNodeView(CyNode node) {
		return null;
	}

	@Override
	public Collection<View<CyNode>> getNodeViews() {
		return Collections.emptySet();
	}

	@Override
	public View<CyEdge> getEdgeView(CyEdge edge) {
		return null;
	}

	@Override
	public Collection<View<CyEdge>> getEdgeViews() {
		return Collections.emptySet();
	}

	@Override
	public Collection<View<? extends CyIdentifiable>> getAllViews() {
		return Collections.emptySet();
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
	public <T, V extends T> void setViewDefault(VisualProperty<? extends T> vp, V defaultValue) {
	}

	@Override
	public <T, V extends T> void setVisualProperty(VisualProperty<? extends T> vp, V value) {
	}

	@Override
	public <T> T getVisualProperty(VisualProperty<T> vp) {
		return null;
	}

	@Override
	public boolean isSet(VisualProperty<?> vp) {
		return false;
	}

	@Override
	public <T, V extends T> void setLockedValue(VisualProperty<? extends T> vp, V value) {
	}

	@Override
	public boolean isValueLocked(VisualProperty<?> vp) {
		return false;
	}

	@Override
	public void clearValueLock(VisualProperty<?> vp) {
	}

	@Override
	public void clearVisualProperties() {
	}
	
	@Override
	public CyNetwork getModel() {
		return model;
	}

	@Override
	public boolean isDirectlyLocked(VisualProperty<?> vp) {
		return false;
	}

	@Override
	public Long getSUID() {
		return suid;
	}

	@Override
	public void dispose() {
	}

	@Override
	public String getRendererId() {
		return null;
	}
	
}
