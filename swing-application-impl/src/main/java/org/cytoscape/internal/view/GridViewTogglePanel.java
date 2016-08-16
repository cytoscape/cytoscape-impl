package org.cytoscape.internal.view;

import static javax.swing.GroupLayout.DEFAULT_SIZE;
import static javax.swing.GroupLayout.PREFERRED_SIZE;
import static javax.swing.GroupLayout.Alignment.CENTER;
import static org.cytoscape.internal.util.ViewUtil.styleToolBarButton;
import static org.cytoscape.util.swing.IconManager.ICON_SHARE_ALT_SQUARE;
import static org.cytoscape.util.swing.IconManager.ICON_TH;

import java.awt.Component;
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.ButtonGroup;
import javax.swing.ButtonModel;
import javax.swing.GroupLayout;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JToggleButton;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.text.JTextComponent;

import org.cytoscape.internal.view.GridViewToggleModel.Mode;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.util.swing.IconManager;

/*
 * #%L
 * Cytoscape Swing Application Impl (swing-application-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2016 The Cytoscape Consortium
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
		
		final ButtonModel selBtnModel = modeButtonGroup.getSelection();
		
		getGridModeButton().setForeground(UIManager
				.getColor(selBtnModel == getGridModeButton().getModel() ? "Focus.color" : "Button.foreground"));
		getViewModeButton().setForeground(UIManager
				.getColor(selBtnModel == getViewModeButton().getModel() ? "Focus.color" : "Button.foreground"));
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
		
		setKeyBindings(this);
		
		model.addPropertyChangeListener("mode", modePropertyListener);
		
		update();
	}
	
	
	private PropertyChangeListener modePropertyListener = (PropertyChangeEvent evt) -> {
		update();
	};
	
	
	JToggleButton getGridModeButton() {
		if (gridModeButton == null) {
			gridModeButton = new JToggleButton(ICON_TH);
			gridModeButton.setToolTipText("Show Grid (G)");
			styleToolBarButton(gridModeButton, serviceRegistrar.getService(IconManager.class).getIconFont(22.0f));
			
			gridModeButton.addActionListener((ActionEvent e) -> {
				model.setMode(Mode.GRID);
			});
		}
		
		return gridModeButton;
	}
	
	JToggleButton getViewModeButton() {
		if (viewModeButton == null) {
			viewModeButton = new JToggleButton(ICON_SHARE_ALT_SQUARE);
			viewModeButton.setToolTipText("Show View (V)");
			styleToolBarButton(viewModeButton, serviceRegistrar.getService(IconManager.class).getIconFont(22.0f));
			
			viewModeButton.addActionListener((ActionEvent e) -> {
				model.setMode(Mode.VIEW);
			});
		}
		
		return viewModeButton;
	}
	
	ButtonGroup getModeButtonGroup() {
		return modeButtonGroup;
	}
	
	private void setKeyBindings(final JComponent comp) {
		final ActionMap actionMap = comp.getActionMap();
		final InputMap inputMap = comp.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);

		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_G, 0), KeyAction.VK_G);
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_V, 0), KeyAction.VK_V);
		
		actionMap.put(KeyAction.VK_G, new KeyAction(KeyAction.VK_G));
		actionMap.put(KeyAction.VK_V, new KeyAction(KeyAction.VK_V));
	}
	
	private class KeyAction extends AbstractAction {

		final static String VK_G = "VK_G";
		final static String VK_V = "VK_V";
		
		KeyAction(final String actionCommand) {
			putValue(ACTION_COMMAND_KEY, actionCommand);
		}

		@Override
		public void actionPerformed(final ActionEvent e) {
			final Component focusOwner = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
			
			if (focusOwner instanceof JTextComponent || focusOwner instanceof JTable ||
					!GridViewTogglePanel.this.isVisible())
				return; // We don't want to steal the key event from these components
			
			final String cmd = e.getActionCommand();
			
			if (cmd.equals(VK_G))
				getGridModeButton().doClick();
			else if (cmd.equals(VK_V))
				getViewModeButton().doClick();
		}
	}

	void dispose() {
		// prevent memory leak
		model.removePropertyChangeListener("mode", modePropertyListener);
	}
}
