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
package org.cytoscape.io.webservice.biomart.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;

import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import org.cytoscape.io.webservice.biomart.BiomartClient;
import org.cytoscape.io.webservice.biomart.task.LoadRepositoryResult;

/**
 * BioMart client main GUI.
 */
public final class BiomartMainDialog extends JDialog {

	private static final long serialVersionUID = 2382157952635589843L;
		
	/**
	 * Basic GUI to access BioMart services.
	 * 
	 * @param client
	 * @param taskManager
	 * @param appManager
	 * @param tblManager
	 */
	public BiomartMainDialog(final BiomartAttrMappingPanel panel, final BiomartClient client, final LoadRepositoryResult result) {
		super();
		setTitle("BioMart Web Service Client");

		// Create a tabbed pane
		final JTabbedPane tabs = new JTabbedPane();
		final JPanel tunablePanel = new JPanel();
		tunablePanel.setBackground(Color.white);
		
		final JPanel tPanel = new JPanel();
		final Dimension panelSize = new Dimension(220, 250);
		tPanel.setMinimumSize(panelSize);
		tPanel.setMaximumSize(panelSize);
		tPanel.setSize(panelSize);

		tPanel.setBackground(Color.white);
		tPanel.setLayout(new GridLayout(0, 1));

		tunablePanel.add(tPanel);

		client.setGUI(panel);
		panel.initDataSources(result);
		panel.setParent(this);
		
		tabs.addTab("Query", panel);

		add(tabs);

		pack();
	}
}
