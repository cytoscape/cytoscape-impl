
package org.cytoscape.internal.actions.dummies;

import org.cytoscape.view.model.*;
import org.cytoscape.model.*;
import java.util.Collection;
import java.util.List;


public class DummyNetworkView implements CyNetworkView {

	public View<CyNode> getNodeView(final CyNode node) {return null;}
	public Collection<View<CyNode>> getNodeViews() {return null;}
	public View<CyEdge> getEdgeView(final CyEdge edge) {return null;}
	public Collection<View<CyEdge>> getEdgeViews() {return null;}
	public Collection<View<? extends CyTableEntry>> getAllViews() {return null;}
	public void fitContent() {}
	public void fitSelected() {}
	public void updateView() {}
	public <T, V extends T> void setViewDefault(final VisualProperty<? extends T> vp, final V defaultValue) {}
	public <T, V extends T> void setVisualProperty(VisualProperty<? extends T> vp, V value) {}
	public <T> T getVisualProperty(VisualProperty<T> vp) { return null; }
	public <T, V extends T> void setLockedValue(VisualProperty<? extends T> vp, V value) {}
	public boolean isValueLocked(VisualProperty<?> vp) { return false; }
	public void clearValueLock(VisualProperty<?> vp) {}
	public CyNetwork getModel() { return null;}
	public Long getSUID() { return null;}
}
