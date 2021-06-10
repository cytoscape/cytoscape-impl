package org.cytoscape.view.vizmap.gui.internal.view.editor.valueeditor;

import java.awt.Color;
import java.awt.Component;
import java.awt.Paint;

import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.util.color.BrewerType;
import org.cytoscape.util.swing.CyColorPaletteChooserFactory;
import org.cytoscape.view.vizmap.gui.editor.ValueEditor;

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

/**
 * This is an annoying re-implementation of JColorChooser.showDialog() that
 * remembers recently used colors between invocations of the chooser dialog.
 */
public class CyColorChooser implements ValueEditor<Paint> {

	private Paint color = Color.WHITE;
	
	private final CyServiceRegistrar serviceRegistrar;

	public CyColorChooser(CyServiceRegistrar serviceRegistrar) {
		this.serviceRegistrar = serviceRegistrar;
	}

	@Override
	public Paint showEditor(Component parent, Paint initialValue) {
		color = initialValue;
		
		var chooserFactory = serviceRegistrar.getService(CyColorPaletteChooserFactory.class);
		var chooser = chooserFactory.getColorPaletteChooser(BrewerType.ANY, false);
		color = chooser.showDialog(parent, "Colors", null, (Color) initialValue, 8);

		return color;
	}

	@Override
	public Class<Paint> getValueType() {
		return Paint.class;
	}
}
