package org.cytoscape.ding.impl;

/*
 * #%L
 * Cytoscape Ding View/Presentation Impl (ding-presentation-impl)
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

import java.util.Properties;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.graph.render.stateful.GraphLOD;
import org.cytoscape.property.CyProperty;
import org.cytoscape.property.PropertyUpdatedEvent;
import org.cytoscape.property.PropertyUpdatedListener;

/**
 * Level of Details object for Ding.
 * 
 * TODO: design and implement event/listeners for this.
 * 
 */
public class BirdsEyeViewLOD extends GraphLOD {
	private GraphLOD source;

	public BirdsEyeViewLOD(GraphLOD source) {
		this.source = source;
	}

	public boolean getDrawEdges() { 
		return source.getDrawEdges(); 
	}

	public void setDrawEdges(boolean drawEdges) { 
		source.setDrawEdges(drawEdges); 
	}

	public byte renderEdges(final int visibleNodeCount, final int totalNodeCount, final int totalEdgeCount) {
		return source.renderEdges(visibleNodeCount, totalNodeCount, totalEdgeCount);
	}

	public boolean detail(final int renderNodeCount, final int renderEdgeCount) {
		boolean sourceDetail = source.detail(renderNodeCount, renderEdgeCount);
		if (sourceDetail && (renderNodeCount + renderEdgeCount) < 10000) {
			return true;
		}
		return false;
	}

	public boolean nodeBorders(final int renderNodeCount, final int renderEdgeCount) {
		return source.nodeBorders(renderNodeCount, renderEdgeCount);
	}

	public boolean nodeLabels(final int renderNodeCount, final int renderEdgeCount) {
		return source.nodeLabels(renderNodeCount, renderEdgeCount);
	}

	public boolean customGraphics(final int renderNodeCount, final int renderEdgeCount) {
		return source.customGraphics(renderNodeCount, renderEdgeCount);
	}

	public boolean edgeArrows(final int renderNodeCount, final int renderEdgeCount) {
		return source.edgeArrows(renderNodeCount, renderEdgeCount);
	}

	public boolean dashedEdges(final int renderNodeCount, final int renderEdgeCount) {
		return source.dashedEdges(renderNodeCount, renderEdgeCount);
	}

	public boolean edgeAnchors(final int renderNodeCount, final int renderEdgeCount) {
		return source.edgeAnchors(renderNodeCount, renderEdgeCount);
	}

	public boolean edgeLabels(final int renderNodeCount, final int renderEdgeCount) {
		return source.edgeLabels(renderNodeCount, renderEdgeCount);
	}

	public boolean textAsShape(final int renderNodeCount, final int renderEdgeCount) {
		return false;
	}

	public double getNestedNetworkImageScaleFactor() {
		return source.getNestedNetworkImageScaleFactor();
	}

}
