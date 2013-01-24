package org.cytoscape.view.vizmap.gui.internal.editor.valueeditor;

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

import javax.swing.Icon;
import javax.swing.JOptionPane;

import org.cytoscape.view.vizmap.gui.editor.ValueEditor;

public final class BooleanValueEditor implements ValueEditor<Boolean> {

	@Override
	public Boolean showEditor(final Component parent, final Boolean initialValue) {
		
		String message = "Please select new value:";
		String title = "Select True or False";
		int optionType = JOptionPane.DEFAULT_OPTION;
		int messageType = JOptionPane.QUESTION_MESSAGE;
		Icon icon = null;
		Boolean[] options = new Boolean[] {true, false};
		int result = JOptionPane.showOptionDialog(parent, message, title, optionType, messageType, icon, options, initialValue);
		if(result == JOptionPane.CLOSED_OPTION)
			return false;
		else
			return options[result];
	}

	@Override
	public Class<Boolean> getValueType() {
		return Boolean.class;
	}
}
