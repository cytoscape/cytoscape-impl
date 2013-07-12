package org.cytoscape.view.vizmap.gui.internal.view.editor.valueeditor;

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

import java.awt.Color;
import java.awt.Component;
import java.awt.Paint;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JColorChooser;
import javax.swing.JDialog;

import org.cytoscape.view.vizmap.gui.editor.ValueEditor;

/**
 * This is an annoying re-implementation of JColorChooser.showDialog() that
 * remembers recently used colors between invocations of the chooser dialog.
 */
public class CyColorChooser implements ValueEditor<Paint> {

	protected JColorChooser chooser = new JColorChooser();
	protected ColorListener listener = new ColorListener();
	protected Paint color = Color.white;

	@Override
	public Paint showEditor(Component parent, Paint initialValue) {
		color = initialValue;
		JDialog dialog = JColorChooser.createDialog(parent, "Please pick a color", true, chooser, listener, null);
		dialog.setVisible(true);

		return color;
	}

	class ColorListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			color = chooser.getColor();
		}
	}

	@Override
	public Class<Paint> getValueType() {
		return Paint.class;
	}
}
