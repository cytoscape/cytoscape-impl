package org.cytoscape.spacial.internal.dummy;

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

import java.util.Collection;
import java.util.List;

import org.cytoscape.model.CyNode;
import org.cytoscape.view.model.View;

import org.cytoscape.spacial.SpacialEntry2DEnumerator;
import org.cytoscape.spacial.SpacialIndex2D;

import org.cytoscape.ding.NodeView;
import org.cytoscape.ding.impl.DGraphView;
import org.cytoscape.ding.impl.DNodeView;

public class DummySpacial implements SpacialIndex2D {
	DGraphView networkView;
	List<NodeView> nodeViews;

	public DummySpacial(DGraphView networkView) {
		this.networkView = networkView;
		nodeViews = networkView.getNodeViewsList();
	}

	public int size() {
		return nodeViews.size();
	}

	public boolean exists(long objKey, float[] extentsArr, int offset) {
		// We actually ignore everything but the objKey
		CyNode node = networkView.getModel().getNode(objKey);
		if (node == null)
			return false;
		if (extentsArr == null) return true;

		DNodeView nodeView = (DNodeView)networkView.getNodeView(node);
		return nodeView.getExtents(extentsArr, offset);
	}


	public SpacialEntry2DEnumerator queryOverlap(float xMin, float yMin, float xMax, float yMax,
	                                             float[] extentsArr, int offset, boolean reverse) {
		return new NetworkEnumerator(nodeViews);
	}

	public void empty() {}

	public void insert(long objKey, float xMin, float yMin, float xMax, float yMax, double z) {} 

	public boolean delete(long objKey) { return true; }

	public void setZOrder(long objKey, double z) {}

	public double getZOrder(long objKey) {
		CyNode node = networkView.getModel().getNode(objKey);
		if (node == null)
			return 0.0;

		DNodeView nodeView = (DNodeView)networkView.getNodeView(node);
		return nodeView.getZPosition();
	}

	private final class NetworkEnumerator implements SpacialEntry2DEnumerator {
		int index = 0;
		List<NodeView> nodeViews;

		NetworkEnumerator(List<NodeView> nodeViews) {
			this.nodeViews = nodeViews;
		}

		public final int numRemaining() {
			return nodeViews.size() - index;
		}

		public final long nextExtents(final float[] extentsArr, final int offset) {
			DNodeView nodeView = (DNodeView)nodeViews.get(index++);
			nodeView.getExtents(extentsArr, offset);
			return nodeView.getModel().getSUID();
		}

		public final long nextLong() {
			return ((DNodeView)nodeViews.get(index++)).getModel().getSUID();
		}

	}
}
