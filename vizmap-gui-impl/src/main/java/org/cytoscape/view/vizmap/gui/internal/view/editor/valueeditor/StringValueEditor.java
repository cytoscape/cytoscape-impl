package org.cytoscape.view.vizmap.gui.internal.view.editor.valueeditor;

import java.awt.Component;

import javax.swing.JOptionPane;

import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.vizmap.gui.editor.VisualPropertyValueEditor;
import org.cytoscape.view.vizmap.gui.internal.view.util.ViewUtil;

/*
 * #%L
 * Cytoscape VizMap GUI Impl (vizmap-gui-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2021 The Cytoscape Consortium
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

public class StringValueEditor implements VisualPropertyValueEditor<String> {

	@Override
	@SuppressWarnings("unchecked")
	public <S extends String> String showEditor(Component parent, S initialValue, VisualProperty<S> vp) {
		if (vp == BasicVisualLexicon.NETWORK_TITLE) // Network Title should not have line breaks
			return showSimpleTextDialog(parent, (String) initialValue, (VisualProperty<String>) vp);
		
		// Labels and Tooltips can have multiple lines
		return ViewUtil.showMultiLineTextEditor(parent, (String) initialValue, (VisualProperty<String>) vp);
	}

	@Override
	public Class<String> getValueType() {
		return String.class;
	}

	private String showSimpleTextDialog(Component parent, String initialValue, VisualProperty<String> vp) {
		return (String) JOptionPane.showInputDialog(parent, ViewUtil.TEXT_EDITOR_LABEL, vp.getDisplayName(),
				JOptionPane.PLAIN_MESSAGE, null, null, initialValue);
	}
}
