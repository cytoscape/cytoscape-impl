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

import java.awt.Component;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JPanel;

import org.cytoscape.view.vizmap.gui.internal.view.editor.valueeditor.FontEditor;

import com.l2fprod.common.beans.editor.AbstractPropertyEditor;
import com.l2fprod.common.beans.editor.ComboBoxPropertyEditor.Value;

public class CyFontPropertyEditor extends AbstractPropertyEditor {

	private Component parent;
	private FontEditor fontEditor;

	private Font currentValue;

	public CyFontPropertyEditor(final FontEditor fontEditor) {
		this.fontEditor = fontEditor;
		this.editor = new JPanel();
		editor.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseReleased(MouseEvent event) {
				selectFont();
			}
		});
	}

	public void setParent(Component parent) {
		this.parent = parent;
	}

	@Override
	public void setValue(Object value) {
		if (value == null)
			this.currentValue = null;
		else if (value instanceof Value)
			this.currentValue = (Font) value;
	}

	@Override
	public Object getValue() {
		return currentValue;
	}

	protected void selectFont() {
		final Font font = (Font) super.getValue();
		final Font selectedFont = fontEditor.showEditor(parent, font);

		if (selectedFont != null) {
			Font oldFont = font;
			Font newFont = selectedFont;

			super.setValue(newFont);
			this.currentValue = newFont;
			firePropertyChange(oldFont, newFont);
		}
	}
}
