package org.cytoscape.view.vizmap.gui.internal.view.editor.valueeditor;

import java.awt.Component;
import java.awt.Window;

import javax.swing.SwingUtilities;

import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.presentation.property.table.CellFormat;
import org.cytoscape.view.vizmap.gui.editor.VisualPropertyValueEditor;

/*
 * #%L
 * Cytoscape VizMap GUI Impl (vizmap-gui-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2018 The Cytoscape Consortium
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

public class CellFormatValueEditor implements VisualPropertyValueEditor<CellFormat> {

	private final CyServiceRegistrar registrar;
	
	public CellFormatValueEditor(CyServiceRegistrar registrar) {
		this.registrar = registrar;
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public <S extends CellFormat> CellFormat showEditor(Component parent, S initialValue, VisualProperty<S> vp) {
		return showSimpleTextDialog(parent, initialValue, (VisualProperty<CellFormat>) vp);
	}

	@Override
	public Class<CellFormat> getValueType() {
		return CellFormat.class;
	}

	private CellFormat showSimpleTextDialog(Component parent, CellFormat initialValue, VisualProperty<CellFormat> vp) {
		Window window = SwingUtilities.getWindowAncestor(parent);
		SetColumnFormatDialog dialog = new SetColumnFormatDialog(window, null, initialValue, registrar);
		dialog.setVisible(true);
		return dialog.getResult();
	}
}
