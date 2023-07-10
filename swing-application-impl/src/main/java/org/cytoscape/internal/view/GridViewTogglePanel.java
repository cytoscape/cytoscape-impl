package org.cytoscape.internal.view;

import static javax.swing.GroupLayout.DEFAULT_SIZE;
import static javax.swing.GroupLayout.PREFERRED_SIZE;
import static javax.swing.GroupLayout.Alignment.CENTER;
import static org.cytoscape.internal.view.util.ViewUtil.styleToolBarButton;
import static org.cytoscape.util.swing.IconManager.ICON_SHARE_ALT_SQUARE;
import static org.cytoscape.util.swing.IconManager.ICON_TH;
import static org.cytoscape.util.swing.LookAndFeelUtil.equalizeSize;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.ButtonGroup;
import javax.swing.GroupLayout;
import javax.swing.JPanel;
import javax.swing.JToggleButton;

import org.cytoscape.internal.view.GridViewToggleModel.Mode;
import org.cytoscape.internal.view.util.ViewUtil;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.util.swing.IconManager;

/*
 * #%L
 * Cytoscape Swing Application Impl (swing-application-impl)
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
public class GridViewTogglePanel extends JPanel {
	
	private JToggleButton gridModeButton;
	private JToggleButton viewModeButton;
	private ButtonGroup modeButtonGroup;
	
	private final GridViewToggleModel model;
	
	private final CyServiceRegistrar serviceRegistrar;
	
	public GridViewTogglePanel(final GridViewToggleModel model, final CyServiceRegistrar serviceRegistrar) {
		this.model = model;
		this.serviceRegistrar = serviceRegistrar;
		
		init();
	}

	public GridViewToggleModel getModel() {
		return model;
	}
	
	void update() {
		final Mode mode = model.getMode();
		final JToggleButton btn = mode == Mode.GRID ? getGridModeButton() : getViewModeButton();
		getModeButtonGroup().setSelected(btn.getModel(), true);
		
		ViewUtil.updateToolBarStyle(getGridModeButton());
		ViewUtil.updateToolBarStyle(getViewModeButton());
	}
	
	private void init() {
		modeButtonGroup = new ButtonGroup();
		modeButtonGroup.add(getGridModeButton());
		modeButtonGroup.add(getViewModeButton());
		
		final GroupLayout layout = new GroupLayout(this);
		setLayout(layout);
		layout.setAutoCreateContainerGaps(false);
		layout.setAutoCreateGaps(false);
		
		layout.setHorizontalGroup(layout.createSequentialGroup()
				.addComponent(getGridModeButton(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
				.addComponent(getViewModeButton(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
		);
		layout.setVerticalGroup(layout.createParallelGroup(CENTER, true)
				.addComponent(getGridModeButton(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
				.addComponent(getViewModeButton(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
		);
		
		equalizeSize(getGridModeButton(), getViewModeButton());
		
		model.addPropertyChangeListener("mode", modePropertyListener);
		update();
	}
	
	
	private PropertyChangeListener modePropertyListener = (PropertyChangeEvent evt) -> {
		update();
	};
	
	
	JToggleButton getGridModeButton() {
		if (gridModeButton == null) {
			gridModeButton = new JToggleButton(ICON_TH);
			gridModeButton.setToolTipText("Show Grid");
			styleToolBarButton(gridModeButton, serviceRegistrar.getService(IconManager.class).getIconFont(22.0f));
			
			gridModeButton.addActionListener(evt -> {
				model.setMode(Mode.GRID);
			});
		}
		
		return gridModeButton;
	}
	
	JToggleButton getViewModeButton() {
		if (viewModeButton == null) {
			viewModeButton = new JToggleButton(ICON_SHARE_ALT_SQUARE);
			viewModeButton.setToolTipText("Show View");
			styleToolBarButton(viewModeButton, serviceRegistrar.getService(IconManager.class).getIconFont(22.0f));
			
			viewModeButton.addActionListener(evt -> {
				model.setMode(Mode.VIEW);
			});
		}
		
		return viewModeButton;
	}
	
	ButtonGroup getModeButtonGroup() {
		return modeButtonGroup;
	}
	
	void dispose() {
		// prevent memory leak
		model.removePropertyChangeListener("mode", modePropertyListener);
	}
}
