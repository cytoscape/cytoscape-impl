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

import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.JTextField;

import org.cytoscape.view.model.VisualProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.l2fprod.common.beans.editor.NumberPropertyEditor;

/**
 *
 */
public class CyNumberPropertyEditor<T extends Number> extends NumberPropertyEditor {

	private static final Logger logger = LoggerFactory.getLogger(CyNumberPropertyEditor.class);

	private Object currentValue;
	private VisualProperty<T> visualProperty;

	/**
	 * Creates a new CyStringPropertyEditor object.
	 */
	public CyNumberPropertyEditor(final Class<T> type) {
		super(type);

		((JTextField) editor).addFocusListener(new FocusListener() {
			
			@Override
			public void focusGained(final FocusEvent e) {
				setCurrentValue();
			}
			
			@Override
			public void focusLost(final FocusEvent e) {
				checkChange();
			}
		});
	}

	private void setCurrentValue() {
		this.currentValue = super.getValue();
	}

	private void checkChange() {
		Number newValue = (Number) super.getValue();

		if (visualProperty != null) {
			final boolean isInRange = visualProperty.getRange()
					.inRange(visualProperty.getRange().getType().cast(newValue));
			
			if (!isInRange) {
				newValue = visualProperty.getDefault();
				((JTextField) editor).setText(newValue.toString());
				editor.repaint();
			}
		} else {
			if (newValue.doubleValue() <= 0) {
				newValue = 0;
				currentValue = 0;
				((JTextField) editor).setText("0");
				editor.repaint();
			}
		}

		if ((currentValue == null && newValue != null) || 
				(currentValue != null && !currentValue.equals(newValue)))
			firePropertyChange(currentValue, newValue);
	}

	public void setVisualProperty(final VisualProperty<T> visualProperty) {
		this.visualProperty = visualProperty;
	}
}
