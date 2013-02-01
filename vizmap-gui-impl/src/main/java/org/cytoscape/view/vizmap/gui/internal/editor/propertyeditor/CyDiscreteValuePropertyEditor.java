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

import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JPanel;

import org.cytoscape.view.vizmap.gui.internal.editor.valueeditor.DiscreteValueEditor;

import com.l2fprod.common.beans.editor.AbstractPropertyEditor;

public class CyDiscreteValuePropertyEditor<T> extends AbstractPropertyEditor {

	private final DiscreteValueEditor<T> valEditor;
	private T currentValue;
	private Component parent;
	
	public CyDiscreteValuePropertyEditor(final DiscreteValueEditor<T> valEditor) {
		
		this.valEditor = valEditor;
		this.editor = new JPanel();
		editor.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseReleased(MouseEvent event) {
				selectValue();
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
		else
			this.currentValue = (T) value;
	}

	@Override
	public Object getValue() {
		return currentValue;
	}

	private final void selectValue() {
		final T val = (T) super.getValue();
		final T selectedVal = valEditor.showEditor(parent, val);

		if (selectedVal != null) {
			final T oldVal = val;
			final T newVal = selectedVal;

			super.setValue(newVal);
			this.currentValue = newVal;
			firePropertyChange(oldVal, newVal);
		}
	}
}
