package org.cytoscape.browser.internal.view.tools;

import static javax.swing.GroupLayout.DEFAULT_SIZE;
import static javax.swing.GroupLayout.PREFERRED_SIZE;
import static org.cytoscape.util.swing.LookAndFeelUtil.isWinLAF;
import static org.cytoscape.util.swing.LookAndFeelUtil.makeSmall;
import static org.cytoscape.view.presentation.property.table.BasicTableVisualLexicon.TABLE_ALTERNATE_ROW_COLORS;
import static org.cytoscape.view.presentation.property.table.BasicTableVisualLexicon.TABLE_GRID_VISIBLE;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JCheckBox;

import org.cytoscape.service.util.CyServiceRegistrar;

/*
 * #%L
 * Cytoscape Table Browser Impl (table-browser-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2021 The Cytoscape Consortium
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

@SuppressWarnings("serial")
public class GeneralOptionsControl extends AbstractToolBarControl {

	private JCheckBox showTableGridCheck;
	private JCheckBox altRowColorsCheck;
	
	public GeneralOptionsControl(CyServiceRegistrar serviceRegistrar) {
		super(serviceRegistrar);
		
		init();
	}

	@Override
	protected void update() {
		var tableView = getTableView();
		
		if (tableView != null) {
			var showGrid = tableView.getVisualProperty(TABLE_GRID_VISIBLE);
			getShowTableGridCheck().setSelected(showGrid);
			
			var altRowColors = tableView.getVisualProperty(TABLE_ALTERNATE_ROW_COLORS);
			getAltRowColorsCheck().setSelected(altRowColors);
		}
	}
	
	private void init() {
		var layout = new GroupLayout(getContentPane());
		getContentPane().setLayout(layout);
		layout.setAutoCreateContainerGaps(false);
		layout.setAutoCreateGaps(isWinLAF());
		
		layout.setHorizontalGroup(layout.createParallelGroup(Alignment.LEADING, true)
				.addComponent(getShowTableGridCheck(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
				.addComponent(getAltRowColorsCheck(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
		);
		layout.setVerticalGroup(layout.createSequentialGroup()
				.addGap(0, 0, Short.MAX_VALUE)
				.addComponent(getShowTableGridCheck(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
				.addComponent(getAltRowColorsCheck(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
				.addGap(0, 0, Short.MAX_VALUE)
		);
		
		makeSmall(getShowTableGridCheck(), getAltRowColorsCheck());
		setAquaStyle(getShowTableGridCheck(), getAltRowColorsCheck());
	}
	
	private JCheckBox getShowTableGridCheck() {
		if (showTableGridCheck == null) {
			showTableGridCheck = new JCheckBox("Show Table Grid");
			showTableGridCheck.setHorizontalAlignment(JCheckBox.LEFT);
			showTableGridCheck.addActionListener(evt -> apply(TABLE_GRID_VISIBLE, showTableGridCheck.isSelected()));
		}
		
		return showTableGridCheck;
	}
	
	private JCheckBox getAltRowColorsCheck() {
		if (altRowColorsCheck == null) {
			altRowColorsCheck = new JCheckBox("Alternate Row Colors");
			altRowColorsCheck.setHorizontalAlignment(JCheckBox.LEFT);
			altRowColorsCheck.addActionListener(evt -> apply(TABLE_ALTERNATE_ROW_COLORS, altRowColorsCheck.isSelected()));
		}
		
		return altRowColorsCheck;
	}
}
