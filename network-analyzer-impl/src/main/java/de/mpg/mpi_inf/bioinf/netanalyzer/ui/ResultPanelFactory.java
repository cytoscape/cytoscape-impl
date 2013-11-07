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

import org.cytoscape.service.util.CyServiceRegistrar;

import javax.swing.*;
import java.awt.*;
import java.util.Properties;

public class ResultPanelFactory {

	private final CyServiceRegistrar registrar;

	public ResultPanelFactory(final CyServiceRegistrar registrar) {
		this.registrar = registrar;
	}

	public ResultPanel registerPanel(final Component panel, final String panelTitle) {
		final ResultPanel resPanel = new ResultPanel(panelTitle);
		JScrollPane pane = new JScrollPane(panel);
		//pane.setViewportView(panel);
		resPanel.setLayout(new BorderLayout());
		resPanel.add(pane, BorderLayout.CENTER);
		registrar.registerAllServices(resPanel, new Properties());
		
		return resPanel;
	}
	
	public void removePanel(ResultPanel panel) {
		if(panel != null) {
			registrar.unregisterAllServices(panel);
		}
	}

}
