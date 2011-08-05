
/*
 Copyright (c) 2010, The Cytoscape Consortium (www.cytoscape.org)

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

package org.cytoscape.internal.view;

import org.cytoscape.application.swing.CytoPanel;
import org.cytoscape.application.swing.CytoPanelState;
import org.cytoscape.application.swing.events.CytoPanelStateChangedEvent;
import org.cytoscape.application.swing.events.CytoPanelStateChangedListener;

import javax.swing.JLabel;


/**
 * This class handles the embedding of the Tools CytoPanel within the Control
 * CytoPanel. For all other cytopanels this is handled by CytoPanelAction,
 * but because tools panel is within another cytopanel, we have to handle things
 * separately.
 */
public class ToolCytoPanelListener implements CytoPanelStateChangedListener {

	BiModalJSplitPane split;
	CytoPanel southWest;
	CytoPanelImp west;
	
	public ToolCytoPanelListener(BiModalJSplitPane split, CytoPanelImp west, CytoPanel southWest) {
		this.split = split;
		this.west = west;
		this.southWest = southWest;
	}


    public void handleEvent(CytoPanelStateChangedEvent e) {
		if ( e.getCytoPanel() != southWest )
			return;

		CytoPanelState newState = e.getNewState();

		if (newState == CytoPanelState.DOCK) 
			west.addComponentToSouth(split);
		else 
			// removeComponentAtSouth() doesn't work properly, so we do this...
			west.addComponentToSouth(new JLabel());

		west.validate();
	}
}
