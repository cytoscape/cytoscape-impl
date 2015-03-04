package org.cytoscape.ding.impl.editor;

/*
 * #%L
 * Cytoscape Ding View/Presentation Impl (ding-presentation-impl)
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

import java.beans.PropertyEditor;

import org.cytoscape.util.swing.IconManager;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.presentation.customgraphics.CyCustomGraphics;
import org.cytoscape.view.vizmap.gui.editor.AbstractVisualPropertyEditor;
import org.cytoscape.view.vizmap.gui.editor.ContinuousEditorType;
import org.cytoscape.view.vizmap.gui.editor.ContinuousMappingCellRendererFactory;
import org.cytoscape.view.vizmap.gui.editor.VisualPropertyEditor2;


@SuppressWarnings("rawtypes")
public class CustomGraphicsVisualPropertyEditor extends AbstractVisualPropertyEditor<CyCustomGraphics>
												implements VisualPropertyEditor2<CyCustomGraphics> {
	
	public CustomGraphicsVisualPropertyEditor(final Class<CyCustomGraphics> type,
											  final CyCustomGraphicsValueEditor valueEditor,
											  final ContinuousMappingCellRendererFactory cellRendererFactory,
											  final IconManager iconManager) {
		super(type, new CyCustomGraphicsPropertyEditor(valueEditor, iconManager), ContinuousEditorType.DISCRETE,
				cellRendererFactory);
		discreteTableCellRenderer = new CyCustomGraphicsCellRenderer();
	}

	@Override
	public PropertyEditor getPropertyEditor() {
		final CyCustomGraphicsPropertyEditor pe = (CyCustomGraphicsPropertyEditor) super.getPropertyEditor();
		pe.setVisualProperty(null);
		
		return pe;
	}
	
	@Override
	public PropertyEditor getPropertyEditor(final VisualProperty<CyCustomGraphics> vp) {
		final CyCustomGraphicsPropertyEditor pe = (CyCustomGraphicsPropertyEditor) super.getPropertyEditor();
		pe.setVisualProperty(vp);
		
		return pe;
	}
}
