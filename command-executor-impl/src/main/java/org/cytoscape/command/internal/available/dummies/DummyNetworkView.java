
package org.cytoscape.command.internal.available.dummies;

import org.cytoscape.view.model.*;
import org.cytoscape.model.*;
import java.util.Collection;


public class DummyNetworkView implements CyNetworkView {

	@Override public View<CyNode> getNodeView(final CyNode node) {return null;}
	@Override public Collection<View<CyNode>> getNodeViews() {return null;}
	@Override public View<CyEdge> getEdgeView(final CyEdge edge) {return null;}
	@Override public Collection<View<CyEdge>> getEdgeViews() {return null;}
	@Override public Collection<View<? extends CyIdentifiable>> getAllViews() {return null;}
	@Override public void fitContent() {}
	@Override public void fitSelected() {}
	@Override public void updateView() {}
	@Override public <T, V extends T> void setViewDefault(final VisualProperty<? extends T> vp, final V defaultValue) {}
	@Override public <T, V extends T> void setVisualProperty(VisualProperty<? extends T> vp, V value) {}
	@Override public <T> T getVisualProperty(VisualProperty<T> vp) { return null; }
	@Override public <T, V extends T> void setLockedValue(VisualProperty<? extends T> vp, V value) {}
	@Override public boolean isValueLocked(VisualProperty<?> vp) { return false; }
	@Override public void clearValueLock(VisualProperty<?> vp) {}
	@Override public CyNetwork getModel() { return null;}
	@Override public Long getSUID() { return null;}
	@Override public boolean isSet(VisualProperty<?> vp) { return false; }
	@Override public void dispose() {}
	@Override public boolean isDirectlyLocked(VisualProperty<?> vp) { return false; }
}
