package org.cytoscape.browser.internal.view.tools;

import static org.cytoscape.util.swing.LookAndFeelUtil.isWinLAF;
import static org.cytoscape.util.swing.LookAndFeelUtil.makeSmall;
import static org.cytoscape.view.presentation.property.table.BasicTableVisualLexicon.TABLE_VIEW_MODE;

import javax.swing.ButtonGroup;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JToggleButton;

import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.presentation.property.table.TableMode;
import org.cytoscape.view.presentation.property.table.TableModeVisualProperty;

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
public class ViewModeControl extends AbstractToolBarControl {

	private JToggleButton autoButton;
	private JToggleButton allButton;
	private JToggleButton selectedButton;
	private final ButtonGroup modeButtonGroup = new ButtonGroup();
	
	public ViewModeControl(CyServiceRegistrar serviceRegistrar) {
		super("Show Rows", serviceRegistrar);
		
		init();
	}

	@Override
	protected void update() {
		var tableView = getTableView();
		
		if (tableView != null) {
			var mode = tableView.getVisualProperty(TABLE_VIEW_MODE);
			var btn = getAutoButton();
			
			if (mode == TableModeVisualProperty.ALL)
				btn = getAllButton();
			else if (mode == TableModeVisualProperty.SELECTED)
				btn = getSelectedButton();
			
			modeButtonGroup.setSelected(btn.getModel(), true);
		}
	}
	
	private void init() {
		modeButtonGroup.add(getAutoButton());
		modeButtonGroup.add(getAllButton());
		modeButtonGroup.add(getSelectedButton());
		
		var layout = new GroupLayout(getContentPane());
		getContentPane().setLayout(layout);
		layout.setAutoCreateContainerGaps(false);
		layout.setAutoCreateGaps(isWinLAF());
		
		layout.setHorizontalGroup(layout.createSequentialGroup()
				.addComponent(getAutoButton())
				.addComponent(getAllButton())
				.addComponent(getSelectedButton())
		);
		layout.setVerticalGroup(layout.createSequentialGroup()
				.addGap(0, 0, Short.MAX_VALUE)
				.addGroup(layout.createParallelGroup(Alignment.CENTER, true)
						.addComponent(getAutoButton())
						.addComponent(getAllButton())
						.addComponent(getSelectedButton())
				)
				.addGap(0, 0, Short.MAX_VALUE)
		);
		
		makeSmall(getAutoButton(), getAllButton(), getSelectedButton());
		setAquaStyle(getAutoButton(), getAllButton(), getSelectedButton());
//		equalizeSize(getAutoButton(), getAllButton(), getSelectedButton());
	}
	
	private JToggleButton getAutoButton() {
		if (autoButton == null) {
			autoButton = new JToggleButton("Auto");
			autoButton.addActionListener(evt -> applyViewMode(TableModeVisualProperty.AUTO));
		}
		
		return autoButton;
	}
	
	private JToggleButton getAllButton() {
		if (allButton == null) {
			allButton = new JToggleButton("All");
			allButton.addActionListener(evt -> applyViewMode(TableModeVisualProperty.ALL));
		}
		
		return allButton;
	}
	
	private JToggleButton getSelectedButton() {
		if (selectedButton == null) {
			selectedButton = new JToggleButton("Selected");
			selectedButton.addActionListener(evt -> applyViewMode(TableModeVisualProperty.SELECTED));
		}
		
		return selectedButton;
	}

	private void applyViewMode(TableMode mode) {
		apply(TABLE_VIEW_MODE, mode);
	}
}
