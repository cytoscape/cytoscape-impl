package org.cytoscape.ding;

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

import org.cytoscape.graph.render.stateful.GraphLOD;


/**
 *
 */
public class PrintLOD implements GraphLOD {
	
	@Override
	public byte renderEdges(int visibleNodeCount, int totalNodeCount, int totalEdgeCount) {
		return 0;
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
		return exportTextAsShape;
	}

	private boolean exportTextAsShape = true;

	public void setPrintingTextAsShape(boolean pExportTextAsShape) {
		exportTextAsShape = pExportTextAsShape;
	}
}
