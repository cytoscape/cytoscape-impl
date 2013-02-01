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

import javax.swing.JDialog;
import javax.swing.JOptionPane;

import org.cytoscape.view.vizmap.gui.editor.ValueEditor;

public abstract class AbstractValueEditor<V> implements ValueEditor<V> {

	protected Class<V> type;
	
	protected final JOptionPane pane;
	protected JDialog editorDialog;
	
	public AbstractValueEditor(final Class<V> type) {
		this.type = type;
		
		pane = new JOptionPane();
		pane.setMessageType(JOptionPane.QUESTION_MESSAGE);
		pane.setOptionType(JOptionPane.OK_CANCEL_OPTION);
	}

	@Override public Class<V> getValueType() {
		return type;
	}
}
