
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

package org.cytoscape.view.manual.internal.control;

import org.cytoscape.view.manual.internal.control.view.AlignPanel;
import org.cytoscape.view.manual.internal.control.view.DistPanel;
import org.cytoscape.view.manual.internal.control.view.StackPanel;
import org.cytoscape.view.manual.internal.common.AbstractManualPanel;


import javax.swing.JPanel;
import javax.swing.BorderFactory;
import java.awt.GridLayout;

import org.cytoscape.session.CyApplicationManager;

/**
 *
 * GUI for Align and Distribute of manualLayout
 *
 *      Rewrite based on the class ControlAction       9/13/2006        Peng-Liang Wang
 *
 */
public class ControlPanel extends AbstractManualPanel {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -2098655182032300315L;

	/**
	 * Creates a new ControlPanel object.
	 */
	public ControlPanel(CyApplicationManager app) {
		super("Align and Distribute");
		setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));
		setLayout(new GridLayout(3,1));

		add(new AlignPanel(app));
		add(new DistPanel(app));
		add(new StackPanel(app));
	} 
} 
