
/*
 Copyright (c) 2008, The Cytoscape Consortium (www.cytoscape.org)

 The Cytoscape Consortium is:
 - Institute for Systems Biology
 - University of California San Diego
 - Memorial Sloan-Kettering Cancer Center
 - Institut Pasteur
 - Agilent Technologies

 This library is free software; you can redistribute it and/or modify it
 under the terms of the GNU Lesser General Public License as published
 by the Free Software Foundation; either version 2.1 of the License, or
 any later version.

 This library is distributed in the hope that it will be useful, but
 WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
 MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
 documentation provided hereunder is on an "as is" basis, and the
 Institute for Systems Biology and the Whitehead Institute
 have no obligations to provide maintenance, support,
 updates, enhancements or modifications.  In no event shall the
 Institute for Systems Biology and the Whitehead Institute
 be liable to any party for direct, indirect, special,
 incidental or consequential damages, including lost profits, arising
 out of the use of this software and its documentation, even if the
 Institute for Systems Biology and the Whitehead Institute
 have been advised of the possibility of such damage.  See
 the GNU Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public License
 along with this library; if not, write to the Free Software Foundation,
 Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
*/

package org.cytoscape.model.internal;

import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNode;

import java.util.Map;


class CyEdgeImpl extends CyIdentifiableImpl implements CyEdge {
	final private CyNode source;
	final private CyNode target;
	final private int index;
	final private boolean directed;

	CyEdgeImpl(long suid, CyNode src, CyNode tgt, boolean dir, int ind) {
		super(suid);
		source = src;
		target = tgt;
		directed = dir;
		index = ind;
	}

	/**
	 * @see org.cytoscape.model.CyEdge#getIndex()
	 */
	@Override
	public int getIndex() {
		return index;
	}

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
		sb.append("  index: ");
		sb.append(Integer.toString(index));

		return sb.toString();
	}
}
