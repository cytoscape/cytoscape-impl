package org.cytoscape.model.internal;

/*
 * #%L
 * Cytoscape Model Impl (model-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2008 - 2013 The Cytoscape Consortium
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

import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNode;

import java.util.Map;


class CyEdgeImpl extends CyIdentifiableImpl implements CyEdge {
	final private CyNode source;
	final private CyNode target;
	final private boolean directed;

	CyEdgeImpl(long suid, CyNode src, CyNode tgt, boolean dir, long ind) {
		super(suid);
		source = src;
		target = tgt;
		directed = dir;
	}

	/**
	 * @see org.cytoscape.model.CyEdge#getIndex()
	public long getIndex() {
		return getSUID().longValue();
	}
	 */

	/**
	 * @see org.cytoscape.model.CyEdge#getSource()
	 */
	@Override
	public CyNode getSource() {
		return source;
	}

	/**
	 * @see org.cytoscape.model.CyEdge#getTarget()
	 */
	@Override
	public CyNode getTarget() {
		return target;
	}

	/**
	 * @see org.cytoscape.model.CyEdge#isDirected()
	 */
	@Override
	public boolean isDirected() {
		return directed;
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("source: ");
		sb.append(source.toString());
		sb.append("  target: ");
		sb.append(target.toString());
		sb.append("  directed: ");
		sb.append(Boolean.toString(directed));

		return sb.toString();
	}
}
