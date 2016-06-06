package org.cytoscape.view.vizmap.gui.internal.view.editor.propertyeditor;

import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.JTextField;

import org.cytoscape.view.model.VisualProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.l2fprod.common.beans.editor.StringPropertyEditor;

/*
 * #%L
 * Cytoscape VizMap GUI Impl (vizmap-gui-impl)
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

public class CyNumberPropertyEditor<T extends Number> extends StringPropertyEditor {

	private static final Logger logger = LoggerFactory.getLogger(CyNumberPropertyEditor.class);

	private Object currentValue;
	private VisualProperty<T> visualProperty;
	private final Class<T> type;
	
	public CyNumberPropertyEditor(final Class<T> type) {
		this.type = type;
		((JTextField) editor).setHorizontalAlignment(JTextField.RIGHT);

		((JTextField) editor).addFocusListener(new FocusListener() {
			@Override
			public void focusGained(FocusEvent e) {
				setCurrentValue();
			}
			@Override
			public void focusLost(final FocusEvent e) {
				checkChange();
			}
		});
	}

	private void setCurrentValue() {
		this.currentValue = getValue();
	}

	@SuppressWarnings("unchecked")
	private void checkChange() {
		String s = (String) getValue();
		Number newValue = parse(s);

		if (newValue != null) {
			if (visualProperty != null) {
				final boolean isInRange = visualProperty.getRange().inRange((T) newValue);
				
				if (!isInRange) {
					newValue = visualProperty.getDefault();
					((JTextField) editor).setText(newValue.toString());
				}
			} else {
				if (newValue.doubleValue() <= 0) {
					newValue = parse("0");
					((JTextField) editor).setText(newValue.toString());
				}
			}
		} else {
			((JTextField) editor).setText("");
		}
		
		editor.repaint();

		if ((currentValue == null && newValue != null) || 
				(currentValue != null && !currentValue.equals(newValue)))
			firePropertyChange(currentValue, newValue);
	}

	public void setVisualProperty(final VisualProperty<T> visualProperty) {
		this.visualProperty = visualProperty;
	}
	
	private Number parse(String s) {
		Number value = null;
		
		try {
			if (s != null) {
				s = s.trim();
				
				if (type == Double.class)
					value = Double.parseDouble(s);
				else if (type == Float.class)
					value = Float.parseFloat(s);
				else if (type == Long.class)
					value = Long.parseLong(s);
				else if (type == Integer.class)
					value = Integer.parseInt(s);
				else if (type == Short.class)
					value = Short.parseShort(s);
				else if (type == Byte.class)
					value = Byte.parseByte(s);
			}
		} catch (Exception e) {
		}
		
		return value;
	}
}
