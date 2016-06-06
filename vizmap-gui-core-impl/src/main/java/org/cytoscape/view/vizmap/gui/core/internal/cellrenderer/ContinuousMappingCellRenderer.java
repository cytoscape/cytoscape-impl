package org.cytoscape.view.vizmap.gui.core.internal.cellrenderer;

/*
 * #%L
 * Cytoscape VizMap GUI Core Impl (vizmap-gui-core-impl)
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

import javax.swing.ImageIcon;
import javax.swing.JTable;

import org.cytoscape.view.vizmap.gui.editor.ContinuousMappingEditor;
import org.cytoscape.view.vizmap.mappings.ContinuousMapping;

import com.l2fprod.common.swing.renderer.DefaultCellRenderer;

public final class ContinuousMappingCellRenderer extends DefaultCellRenderer {

	private static final long serialVersionUID = -6734053848878359286L;

	private final ContinuousMappingEditor<?, ?> editor;

	public ContinuousMappingCellRenderer(final ContinuousMappingEditor<?, ?> editor) {
		if (editor == null)
			throw new NullPointerException("Editor object is null.");

		this.editor = editor;
	}

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
			int row, int column) {

		if (value == null || value instanceof ContinuousMapping == false) {
			this.setText("Unknown Mapping");
			return this;
		}

		if (isSelected) {
			setBackground(table.getSelectionBackground());
			setForeground(table.getSelectionForeground());
		} else {
			setBackground(table.getBackground());
			setForeground(table.getForeground());
		}

		final int height = table.getRowHeight(row);
		final int width = table.getColumnModel().getColumn(column).getWidth();
		final ImageIcon icon = editor.drawIcon(width, height - 2, false);
		this.setIcon(icon);

		return this;
	}
}
