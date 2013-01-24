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

import java.awt.Component;
import java.awt.Dimension;

import javax.swing.Icon;
import javax.swing.JPanel;

import org.cytoscape.application.swing.CytoPanelComponent;
import org.cytoscape.application.swing.CytoPanelName;

public class ResultPanel extends JPanel implements CytoPanelComponent {
	
	private static final Dimension DEF_PANEL_SIZE = new Dimension(650, 530);
	
	private final String panelTitle;
	
	public ResultPanel(final String panelTitle) {
		this.setPreferredSize(DEF_PANEL_SIZE);
		this.setSize(DEF_PANEL_SIZE);
		this.setMinimumSize(DEF_PANEL_SIZE);
		
		this.panelTitle = panelTitle;
	}
	/**
	 * 
	 */
	private static final long serialVersionUID = -7824516315016600756L;

	@Override
	public Component getComponent() {
		return this;
	}

	@Override
	public CytoPanelName getCytoPanelName() {
		return CytoPanelName.EAST;
	}

	@Override
	public String getTitle() {
		return panelTitle;
	}

	@Override
	public Icon getIcon() {
		return null;
	}


}
