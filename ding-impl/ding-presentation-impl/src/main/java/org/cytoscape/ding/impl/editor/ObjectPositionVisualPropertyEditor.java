package org.cytoscape.ding.impl.editor;

import java.beans.PropertyEditor;

import org.cytoscape.ding.CyObjectPositionPropertyEditor;
import org.cytoscape.ding.ObjectPositionCellRenderer;
import org.cytoscape.ding.ObjectPositionValueEditor;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.presentation.property.values.ObjectPosition;
import org.cytoscape.view.vizmap.gui.editor.AbstractVisualPropertyEditor;
import org.cytoscape.view.vizmap.gui.editor.ContinuousEditorType;
import org.cytoscape.view.vizmap.gui.editor.ContinuousMappingCellRendererFactory;
import org.cytoscape.view.vizmap.gui.editor.VisualPropertyEditor2;

/*
 * #%L
 * Cytoscape Ding View/Presentation Impl (ding-presentation-impl)
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

public class ObjectPositionVisualPropertyEditor extends AbstractVisualPropertyEditor<ObjectPosition>
		implements VisualPropertyEditor2<ObjectPosition> {
	
	public ObjectPositionVisualPropertyEditor(
			ObjectPositionValueEditor valueEditor,
			ContinuousMappingCellRendererFactory cellRendererFactory,
			CyServiceRegistrar serviceRegistrar
	) {
		super(ObjectPosition.class, new CyObjectPositionPropertyEditor(valueEditor, serviceRegistrar),
				ContinuousEditorType.DISCRETE, cellRendererFactory);

		discreteTableCellRenderer = new ObjectPositionCellRenderer();
	}
	
	@Override
	public PropertyEditor getPropertyEditor() {
		var pe = (CyObjectPositionPropertyEditor) super.getPropertyEditor();
		pe.setVisualProperty(null);
		
		return pe;
	}

	@Override
	public PropertyEditor getPropertyEditor(VisualProperty<ObjectPosition> vp) {
		var pe = (CyObjectPositionPropertyEditor) super.getPropertyEditor();
		pe.setVisualProperty(vp);

		return pe;
	}
}
