package org.cytoscape.ding;

import org.cytoscape.ding.impl.DingGraphLODAll;

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


/**
 *
 */
public class PrintLOD implements GraphLOD {
	
	private final GraphLOD delegate;
	
	private final boolean exportTextAsShape;
	private final boolean exportLabels;
	
	
	public PrintLOD(GraphLOD delgate, boolean exportTextAsShape, boolean exportLabels) {
		this.delegate = delgate;
		this.exportTextAsShape = exportTextAsShape;
		this.exportLabels = exportLabels;
	}
	
	public PrintLOD() {
		this(DingGraphLODAll.instance(), true, true);
	}
	
	
	/**
	 * Always render in high detail, but things like labels can still be disabled based on number of visible nodes/edges.
	 */
	@Override
	public boolean detail(int renderNodeCount, int renderEdgeCount) {
		return true;
	}

	
	@Override
	public boolean isEdgeBufferPanEnabled() {
		return delegate.isEdgeBufferPanEnabled();
	}
	
	@Override
	public boolean isLabelCacheEnabled() {
		return delegate.isLabelCacheEnabled();
	}
	
	@Override
	public boolean isHidpiEnabled() {
		return delegate.isHidpiEnabled();
	}	
	
	@Override
	public RenderEdges renderEdges(int visibleNodeCount, int totalNodeCount, int totalEdgeCount) {
		return delegate.renderEdges(visibleNodeCount, totalNodeCount, totalEdgeCount);
	}

	@Override
	public boolean nodeBorders(int renderNodeCount, int renderEdgeCount) {
		return delegate.nodeBorders(renderNodeCount, renderEdgeCount);
	}

	@Override
	public boolean nodeLabels(int renderNodeCount, int renderEdgeCount) {
		return exportLabels && delegate.nodeLabels(renderNodeCount, renderEdgeCount);
	}

	@Override
	public boolean customGraphics(int renderNodeCount, int renderEdgeCount) {
		return delegate.customGraphics(renderNodeCount, renderEdgeCount);
	}

	@Override
	public boolean edgeArrows(int renderNodeCount, int renderEdgeCount) {
		return delegate.edgeArrows(renderNodeCount, renderEdgeCount);
	}

	@Override
	public boolean dashedEdges(int renderNodeCount, int renderEdgeCount) {
		return delegate.dashedEdges(renderNodeCount, renderEdgeCount);
	}

	@Override
	public boolean edgeAnchors(int renderNodeCount, int renderEdgeCount) {
		return delegate.edgeAnchors(renderNodeCount, renderEdgeCount);
	}

	@Override
	public boolean edgeLabels(int renderNodeCount, int renderEdgeCount) {
		return exportLabels && delegate.edgeLabels(renderNodeCount, renderEdgeCount);
	}

	@Override
	public boolean textAsShape(int renderNodeCount, int renderEdgeCount) {
		return exportTextAsShape;
	}

	@Override
	public double getNestedNetworkImageScaleFactor() {
		return delegate.getNestedNetworkImageScaleFactor();
	}
	
}
