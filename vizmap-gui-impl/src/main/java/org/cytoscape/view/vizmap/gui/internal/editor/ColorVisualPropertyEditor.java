package org.cytoscape.view.vizmap.gui.internal.editor;

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
import java.awt.Paint;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.model.CyNetworkTableManager;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.gui.editor.ContinuousEditorType;
import org.cytoscape.view.vizmap.gui.editor.ContinuousMappingCellRendererFactory;
import org.cytoscape.view.vizmap.gui.editor.EditorManager;
import org.cytoscape.view.vizmap.gui.internal.editor.propertyeditor.CyColorPropertyEditor;

/**
 * Manages editors for Color Visual Properties. This object can be used with any
 * VisualProperty using Color as its type.
 * 
 */
public class ColorVisualPropertyEditor extends BasicVisualPropertyEditor<Paint> {

	/**
	 * Constructor. Should instantiate one editor per VisualProperty.
	 * @param cellRendererFactory 
	 */
	public ColorVisualPropertyEditor(final Class<Paint> type, final CyNetworkTableManager manager,
			final CyApplicationManager appManager, final EditorManager editorManager, final VisualMappingManager vmm, final CyColorPropertyEditor colorPropEditor, ContinuousMappingCellRendererFactory cellRendererFactory) {
		super(type, colorPropEditor, ContinuousEditorType.COLOR, cellRendererFactory);

		this.discreteTableCellRenderer = REG.getRenderer(Color.class);
	}
}