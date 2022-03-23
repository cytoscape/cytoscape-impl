package org.cytoscape.ding.impl;

/*
 * #%L
 * Cytoscape Ding View/Presentation Impl (ding-presentation-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2021 The Cytoscape Consortium
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

import org.cytoscape.graph.render.stateful.GraphLOD;

public class DingGraphLODAll implements GraphLOD {
	
	private static final DingGraphLODAll instance = new DingGraphLODAll();
	
	public static DingGraphLODAll instance() {
		return instance;
	}
	
	
	@Override
	public boolean isEdgeBufferPanEnabled() {
		return false;
	}
	
	@Override
	public boolean isLabelCacheEnabled() {
		return true;
	}
	
	@Override
	public boolean isHidpiEnabled() {
		return true;
	}	
	
	@Override
	public RenderEdges renderEdges(int visibleNodeCount, int totalNodeCount, int totalEdgeCount) {
		return RenderEdges.TOUCHING_VISIBLE_NODES;
	}

	@Override
	public boolean detail(int renderNodeCount, int renderEdgeCount) {
		return true;
	}

	@Override
	public boolean nodeBorders(int renderNodeCount, int renderEdgeCount) {
		return true;
	}

	@Override
	public boolean nodeLabels(int renderNodeCount, int renderEdgeCount) {
		return true;
	}

	@Override
	public boolean customGraphics(int renderNodeCount, int renderEdgeCount) {
		return true;
	}

	@Override
	public boolean edgeArrows(int renderNodeCount, int renderEdgeCount) {
		return true;
	}

	@Override
	public boolean dashedEdges(int renderNodeCount, int renderEdgeCount) {
		return true;
	}

	@Override
	public boolean edgeAnchors(int renderNodeCount, int renderEdgeCount) {
		return true;
	}

	@Override
	public boolean edgeLabels(int renderNodeCount, int renderEdgeCount) {
		return true;
	}

	@Override
	public boolean textAsShape(int renderNodeCount, int renderEdgeCount) {
		return true;
	}

	@Override
	public double getNestedNetworkImageScaleFactor() {
		return 1.0;
	}
	
}
