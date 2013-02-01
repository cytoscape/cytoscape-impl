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

import javax.swing.table.TableCellRenderer;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.model.CyNetworkTableManager;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.gui.editor.ContinuousEditorType;
import org.cytoscape.view.vizmap.gui.editor.ContinuousMappingCellRendererFactory;
import org.cytoscape.view.vizmap.gui.editor.ContinuousMappingEditor;
import org.cytoscape.view.vizmap.gui.editor.EditorManager;
import org.cytoscape.view.vizmap.gui.internal.cellrenderer.NumberContinuousCellRenderer;
import org.cytoscape.view.vizmap.gui.internal.editor.propertyeditor.CyNumberPropertyEditor;

public class NumberVisualPropertyEditor<T extends Number> extends BasicVisualPropertyEditor<T> {

	/**
	 * Creates a new DiscreteNumber object.
	 * @param cellRendererFactory 
	 */
	public NumberVisualPropertyEditor(Class<T> type, final CyNetworkTableManager manager,
			final CyApplicationManager appManager, final EditorManager editorManager, final VisualMappingManager vmm, ContinuousMappingCellRendererFactory cellRendererFactory) {
		super(type, new CyNumberPropertyEditor<T>(type, null), ContinuousEditorType.CONTINUOUS, cellRendererFactory);
		discreteTableCellRenderer = REG.getRenderer(type);
	}

	@Override
	public TableCellRenderer getContinuousTableCellRenderer(
			ContinuousMappingEditor<? extends Number, T> continuousMappingEditor) {
		return new NumberContinuousCellRenderer((ContinuousMappingEditor<?, ?>) continuousMappingEditor);
	}

}
