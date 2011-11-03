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
package org.cytoscape.ding.impl.visualproperty;

import java.util.HashSet;
import java.util.Set;

import org.cytoscape.ding.DArrowShape;
import org.cytoscape.model.CyEdge;
import org.cytoscape.view.model.AbstractVisualProperty;
import org.cytoscape.view.model.DiscreteRange;
import org.cytoscape.view.model.Range;

public class ArrowShapeTwoDVisualProperty extends AbstractVisualProperty<DArrowShape> {

	private static final Range<DArrowShape> ARROW_SHAPE_RANGE;

	static {
		final Set<DArrowShape> arrowSet = new HashSet<DArrowShape>();
		
		for (final DArrowShape arrow : DArrowShape.values())
			arrowSet.add(arrow);
		
		ARROW_SHAPE_RANGE = new DiscreteRange<DArrowShape>(DArrowShape.class, arrowSet);
	}

	public ArrowShapeTwoDVisualProperty(final DArrowShape def, final String id,
			final String name) {
		super(def, ARROW_SHAPE_RANGE, id, name, CyEdge.class);
	}

	public String toSerializableString(final DArrowShape value) {
		return value.toString();
	}

	public DArrowShape parseSerializableString(final String text) {
		DArrowShape shape = DArrowShape.parseArrowText(text);

		return shape;
	}
}
