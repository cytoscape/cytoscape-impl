
/*
 Copyright (c) 2006, 2007, The Cytoscape Consortium (www.cytoscape.org)

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

package org.cytoscape.view.manual.internal.control.actions.align;

import java.util.List;

import javax.swing.Icon;

import org.cytoscape.model.CyNode;
import org.cytoscape.session.CyApplicationManager;
import org.cytoscape.view.manual.internal.control.actions.AbstractControlAction;
import org.cytoscape.view.model.View;
import org.cytoscape.view.presentation.property.MinimalVisualLexicon;
/**
 *
 */
public class HAlignRight extends AbstractControlAction {

	private static final long serialVersionUID = -2582880158463407206L;

	public HAlignRight(Icon i,CyApplicationManager appMgr) {
		super("Horizontal Align Right",i,appMgr);
	}

	protected void control(List<View<CyNode>> nodes) {
		for ( View<CyNode> n : nodes ) {
			final double w = n.getVisualProperty(MinimalVisualLexicon.NODE_WIDTH) / 2;
			n.setVisualProperty(MinimalVisualLexicon.NODE_X_LOCATION, X_max - w);
		}
	}

	protected double getX(View<CyNode> n) {
		final double x = n.getVisualProperty(MinimalVisualLexicon.NODE_X_LOCATION);
		final double w = n.getVisualProperty(MinimalVisualLexicon.NODE_WIDTH) / 2;

		return x + w;
	}
}
