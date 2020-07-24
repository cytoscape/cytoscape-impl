package org.cytoscape.view.vizmap.gui.internal.view.editor.mappingeditor;

import java.awt.Color;
import java.awt.Paint;

import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyTable;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.gui.editor.EditorManager;
import org.cytoscape.view.vizmap.gui.internal.CurrentTableService;
import org.cytoscape.view.vizmap.gui.internal.util.ServicesUtil;
import org.cytoscape.view.vizmap.mappings.ContinuousMapping;

/*
 * #%L
 * Cytoscape VizMap GUI Impl (vizmap-gui-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2019 The Cytoscape Consortium
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

public class GradientEditor extends AbstractContinuousMappingEditor<Double, Color> {

	public GradientEditor(final EditorManager editorManager, final ServicesUtil servicesUtil) {
		super(editorManager, servicesUtil);
	}

	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void setValue(final Object value) {
		if (value instanceof ContinuousMapping == false)
			throw new IllegalArgumentException("Value should be ContinuousMapping: this is " + value);
		

		mapping = (ContinuousMapping<Double, Color>) value;
		Class<? extends CyIdentifiable> type = (Class<? extends CyIdentifiable>) mapping.getVisualProperty().getTargetDataType();
		
		CyTable attr = servicesUtil.get(CurrentTableService.class).getCurrentTable(type);
		if(attr == null)
			return;
		
		VisualMappingManager vmMgr = servicesUtil.get(VisualMappingManager.class);
		editorPanel = new GradientEditorPanel(vmMgr.getCurrentVisualStyle(), mapping, attr, editorManager,
				editorManager.getValueEditor(Paint.class), servicesUtil);
	}
}
