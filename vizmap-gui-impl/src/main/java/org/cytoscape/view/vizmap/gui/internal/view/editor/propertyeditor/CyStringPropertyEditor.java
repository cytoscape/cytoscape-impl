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

import com.l2fprod.common.beans.editor.StringPropertyEditor;
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
		final Object newValue = super.getValue();
		
		if ((currentValue == null && newValue != null) || 
				(currentValue != null && !currentValue.equals(newValue)))
			firePropertyChange(currentValue, newValue);
	}
}
