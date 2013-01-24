package org.cytoscape.view.vizmap.gui.internal.editor.propertyeditor;

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
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.cytoscape.view.vizmap.gui.internal.cellrenderer.CyColorCellRenderer;
import org.cytoscape.view.vizmap.gui.internal.editor.valueeditor.CyColorChooser;

import com.l2fprod.common.beans.editor.AbstractPropertyEditor;
import com.l2fprod.common.beans.editor.ColorPropertyEditor;
import com.l2fprod.common.beans.editor.FilePropertyEditor;
import com.l2fprod.common.swing.ComponentFactory;
import com.l2fprod.common.swing.PercentLayout;
import com.l2fprod.common.util.ResourceManager;

/**
 * ColorPropertyEditor. <br>
 * 
 */
public class CyColorPropertyEditor extends AbstractPropertyEditor {

	private CyColorCellRenderer label;

	private JButton button;
	private Color color;

	private final CyColorChooser chooser;

	/**
	 * Creates a new CyColorPropertyEditor object.
	 */
	public CyColorPropertyEditor(final CyColorChooser chooser) {
		color = Color.white;
		this.chooser = chooser;

		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				editor = new JPanel(new PercentLayout(PercentLayout.HORIZONTAL, 0));
				((JPanel) editor).add("*", label = new CyColorCellRenderer());
				label.setOpaque(false);
				((JPanel) editor).add(button = ComponentFactory.Helper.getFactory().createMiniButton());
				button.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						selectColor();
					}
				});
				((JPanel) editor).add(button = ComponentFactory.Helper.getFactory().createMiniButton());
				button.setText("X");
				button.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						selectNull();
					}
				});
				((JPanel) editor).setOpaque(false);
			}
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
		ResourceManager rm = ResourceManager.all(FilePropertyEditor.class);
		String title = rm.getString("ColorPropertyEditor.title");
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

	public static class AsInt extends ColorPropertyEditor {
		public void setValue(Object arg0) {
			if (arg0 instanceof Integer)
				super.setValue(new Color(((Integer) arg0).intValue()));
			else
				super.setValue(arg0);
		}

		public Object getValue() {
			Object value = super.getValue();

			if (value == null)
				return null;
			else

				return Integer.valueOf(((Color) value).getRGB());
		}

		protected void firePropertyChange(Object oldValue, Object newValue) {
			if (oldValue instanceof Color)
				oldValue = Integer.valueOf(((Color) oldValue).getRGB());

			if (newValue instanceof Color)
				newValue = Integer.valueOf(((Color) newValue).getRGB());

			super.firePropertyChange(oldValue, newValue);
		}
	}
}
