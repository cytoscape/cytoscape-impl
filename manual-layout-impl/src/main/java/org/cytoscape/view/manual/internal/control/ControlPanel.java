package org.cytoscape.view.manual.internal.control;

/*
 * #%L
 * Cytoscape Manual Layout Impl (manual-layout-impl)
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

import java.awt.GridLayout;

import javax.swing.BorderFactory;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.util.swing.LookAndFeelUtil;
import org.cytoscape.view.manual.internal.common.AbstractManualPanel;
import org.cytoscape.view.manual.internal.control.view.AlignPanel;
import org.cytoscape.view.manual.internal.control.view.DistPanel;
import org.cytoscape.view.manual.internal.control.view.StackPanel;

/**
 *
 * GUI for Align and Distribute of manualLayout
 *
 *      Rewrite based on the class ControlAction       9/13/2006        Peng-Liang Wang
 *
 */
public class ControlPanel extends AbstractManualPanel {
	
	private static final long serialVersionUID = -2098655182032300315L;

	/**
	 * Creates a new ControlPanel object.
	 */
	public ControlPanel(CyApplicationManager app) {
		super("Align and Distribute");
		
		if (LookAndFeelUtil.isAquaLAF())
			setOpaque(false);
		
		setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));
		setLayout(new GridLayout(3,1));

		add(new AlignPanel(app));
		add(new DistPanel(app));
		add(new StackPanel(app));
	} 
} 
