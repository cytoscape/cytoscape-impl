package org.cytoscape.io.webservice.biomart.ui;

/*
 * #%L
 * Cytoscape Biomart Webservice Impl (webservice-biomart-client-impl)
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
		
		final JPanel tPanel = new JPanel();
		final Dimension panelSize = new Dimension(220, 250);
		tPanel.setMinimumSize(panelSize);
		tPanel.setMaximumSize(panelSize);
		tPanel.setSize(panelSize);

		tPanel.setLayout(new GridLayout(0, 1));

		tunablePanel.add(tPanel);

		panel.initDataSources(result);
		
		tabs.addTab("Query", panel);

		add(tabs);

		pack();
	}
}
