package org.cytoscape.view.vizmap.gui.core.internal.cellrenderer;

/*
 * #%L
 * Cytoscape VizMap GUI Core Impl (vizmap-gui-core-impl)
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

import javax.swing.table.TableCellRenderer;

import org.cytoscape.view.vizmap.gui.editor.ContinuousMappingCellRendererFactory;
import org.cytoscape.view.vizmap.gui.editor.ContinuousMappingEditor;

public class ContinuousMappingCellRendererFactoryImpl implements ContinuousMappingCellRendererFactory {

	@Override
	public TableCellRenderer createTableCellRenderer(ContinuousMappingEditor<? extends Number, ?> editor) {
		return new ContinuousMappingCellRenderer(editor);
	}
}
