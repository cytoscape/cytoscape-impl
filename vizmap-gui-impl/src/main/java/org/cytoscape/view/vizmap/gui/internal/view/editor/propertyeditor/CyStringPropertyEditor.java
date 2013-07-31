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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.swing.JTextField;

import com.l2fprod.common.beans.editor.StringPropertyEditor;
import com.l2fprod.common.propertysheet.PropertySheetTableModel.Item;
import com.l2fprod.common.swing.LookAndFeelTweaks;

public class CyStringPropertyEditor extends StringPropertyEditor {
	
	private Object currentValue;

	/**
	 * Creates a new CyStringPropertyEditor object.
	 */
	public CyStringPropertyEditor() {
		editor = new JTextField();
		((JTextField) editor).setBorder(LookAndFeelTweaks.EMPTY_BORDER);

		((JTextField) editor).addFocusListener(new FocusListener() {
			
			@Override
			public void focusGained(FocusEvent e) {
				Method getM = null;
				Object val = null;
				
				try {
					if (e.getOppositeComponent() != null)
						getM = e.getOppositeComponent().getClass().getMethod("getSelectedRow", new Class[] {});
				} catch (SecurityException e1) {
					e1.printStackTrace();
				} catch (NoSuchMethodException e1) {
					e1.printStackTrace();
				}

				try {
					if (getM != null)
						val = getM.invoke(e.getOppositeComponent(), new Object[] {});
				} catch (IllegalArgumentException e1) {
					e1.printStackTrace();
				} catch (IllegalAccessException e1) {
					e1.printStackTrace();
				} catch (InvocationTargetException e1) {
					e1.printStackTrace();
				}

				try {
					if (e.getOppositeComponent() != null)
						getM = e.getOppositeComponent().getClass().getMethod(
								"getValueAt", new Class[] { int.class, int.class });
				} catch (SecurityException e1) {
					e1.printStackTrace();
				} catch (NoSuchMethodException e1) {
					e1.printStackTrace();
				}

				Object val2 = null;

				try {
					if (getM != null && e.getOppositeComponent() != null)
						val2 = getM.invoke(e.getOppositeComponent(), new Object[] { (Integer) val, Integer.valueOf(0) });
				} catch (IllegalArgumentException e1) {
					e1.printStackTrace();
				} catch (IllegalAccessException e1) {
					e1.printStackTrace();
				} catch (InvocationTargetException e1) {
					e1.printStackTrace();
				}

				if (val2 instanceof Item)
					currentValue = ((Item) val2).getProperty().getDisplayName();
			}

			@Override
			public void focusLost(final FocusEvent e) {
				final String newValue = ((JTextField) editor).getText();
				
				if ((currentValue == null && newValue != null) || 
						(currentValue != null && !currentValue.equals(newValue)))
					firePropertyChange(currentValue, newValue);
			}
		});
	}
}
