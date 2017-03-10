package org.cytoscape.view.vizmap.gui.internal.view.editor.propertyeditor;

/*
 * #%L
 * Cytoscape VizMap GUI Impl (vizmap-gui-impl)
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

import java.awt.Color;
import java.awt.Paint;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import org.cytoscape.util.swing.IconManager;
import org.cytoscape.view.vizmap.gui.internal.util.ServicesUtil;
import org.cytoscape.view.vizmap.gui.internal.view.cellrenderer.CyColorCellRenderer;
import org.cytoscape.view.vizmap.gui.internal.view.editor.valueeditor.CyColorChooser;

import com.l2fprod.common.beans.editor.AbstractPropertyEditor;
import com.l2fprod.common.swing.ComponentFactory;
import com.l2fprod.common.swing.PercentLayout;


public class CyColorPropertyEditor extends AbstractPropertyEditor {

	private CyColorCellRenderer label;

	private JButton editBtn;
	private JButton removeBtn;
	private Color color = Color.WHITE;

	private final CyColorChooser chooser;

	public CyColorPropertyEditor(final CyColorChooser chooser, final ServicesUtil servicesUtil) {
		this.chooser = chooser;

		SwingUtilities.invokeLater(() -> {
			final IconManager iconManager = servicesUtil.get(IconManager.class);
			
			editor = new JPanel(new PercentLayout(PercentLayout.HORIZONTAL, 0));
			((JPanel) editor).add("*", label = new CyColorCellRenderer());
			label.setOpaque(false);
			
			((JPanel) editor).add(editBtn = ComponentFactory.Helper.getFactory().createMiniButton());
			editBtn.setText(IconManager.ICON_ELLIPSIS_H);
			editBtn.setToolTipText("Edit color");
			editBtn.setFont(iconManager.getIconFont(14.0f));
			editBtn.addActionListener((ActionEvent e) -> {
				selectColor();
			});
			
			((JPanel) editor).add(removeBtn = ComponentFactory.Helper.getFactory().createMiniButton());
			removeBtn.setText(IconManager.ICON_TRASH_O);
			removeBtn.setToolTipText("Remove color");
			removeBtn.setFont(iconManager.getIconFont(14.0f));
			removeBtn.addActionListener((ActionEvent e) -> {
				selectNull();
			});
			((JPanel) editor).setOpaque(false);
			
			setKeyBindings((JPanel) editor);
		});
	}

	@Override
	public Object getValue() {
		return color;
	}

	@Override
	public void setValue(Object value) {
		color = (Color) value;
		label.setValue(color);
	}

	private void selectColor() {
		Paint selectedColor = chooser.showEditor(editor, color);

		if (selectedColor instanceof Color == false)
			return;

		if (selectedColor != null) {
			Color oldColor = color;
			Color newColor = (Color) selectedColor;
			label.setValue(newColor);
			color = newColor;
			firePropertyChange(oldColor, newColor);
		}
	}

	protected void selectNull() {
		Color oldColor = color;
		label.setValue(null);
		color = null;
		firePropertyChange(oldColor, null);
	}

	private void setKeyBindings(final JPanel panel) {
		final ActionMap actionMap = panel.getActionMap();
		final InputMap inputMap = panel.getInputMap(JComponent.WHEN_FOCUSED);

		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0), KeyAction.VK_SPACE);
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), KeyAction.VK_DELETE);
		actionMap.put(KeyAction.VK_SPACE, new KeyAction(KeyAction.VK_SPACE));
		actionMap.put(KeyAction.VK_DELETE, new KeyAction(KeyAction.VK_DELETE));
	}

	@SuppressWarnings("serial")
	private class KeyAction extends AbstractAction {

		final static String VK_SPACE = "VK_SPACE";
		final static String VK_DELETE = "VK_DELETE";
		
		KeyAction(final String actionCommand) {
			putValue(ACTION_COMMAND_KEY, actionCommand);
		}

		@Override
		public void actionPerformed(final ActionEvent e) {
			final String cmd = e.getActionCommand();
			
			if (cmd.equals(VK_SPACE)) {
				selectColor();
			} else if (cmd.equals(VK_DELETE)) {
				selectNull();
			}
		}
	}
}
