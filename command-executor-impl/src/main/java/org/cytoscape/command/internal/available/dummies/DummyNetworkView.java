package org.cytoscape.command.internal.available.dummies;

/*
 * #%L
 * Cytoscape Command Executor Impl (command-executor-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2013 The Cytoscape Consortium
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as 
 * published by the Free Software Foundation, either version 2.1 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */

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
	@Override public void clearVisualProperties() {}
}
