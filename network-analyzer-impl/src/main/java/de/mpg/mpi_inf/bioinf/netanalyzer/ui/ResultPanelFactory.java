package de.mpg.mpi_inf.bioinf.netanalyzer.ui;

/*
 * #%L
 * Cytoscape NetworkAnalyzer Impl (network-analyzer-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2013 The Cytoscape Consortium
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

import java.awt.BorderLayout;
import java.util.Properties;

import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.util.swing.LookAndFeelUtil;

public class ResultPanelFactory {

	private final CyServiceRegistrar registrar;

	public ResultPanelFactory(final CyServiceRegistrar registrar) {
		this.registrar = registrar;
	}

	public ResultPanel registerPanel(final JPanel panel, final String panelTitle) {
		final ResultPanel resPanel = new ResultPanel(panelTitle);
		JScrollPane pane = new JScrollPane(panel);
		pane.setBorder(null);
		resPanel.setLayout(new BorderLayout());
		resPanel.add(pane, BorderLayout.CENTER);
		
		if (LookAndFeelUtil.isAquaLAF()) {
			resPanel.setOpaque(false);
			pane.setOpaque(false);
			pane.getViewport().setOpaque(false);
			panel.setOpaque(false);
		}
		
		registrar.registerAllServices(resPanel, new Properties());
		
		return resPanel;
	}
	
	public void removePanel(ResultPanel panel) {
		if (panel != null) {
			registrar.unregisterAllServices(panel);
		}
	}
}
