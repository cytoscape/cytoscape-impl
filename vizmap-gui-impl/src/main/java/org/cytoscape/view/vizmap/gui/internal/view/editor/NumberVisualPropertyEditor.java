package org.cytoscape.view.vizmap.gui.internal.view.editor;

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

import org.cytoscape.view.vizmap.gui.editor.ContinuousEditorType;
import org.cytoscape.view.vizmap.gui.editor.ContinuousMappingCellRendererFactory;
import org.cytoscape.view.vizmap.gui.editor.ContinuousMappingEditor;
import org.cytoscape.view.vizmap.gui.internal.view.cellrenderer.NumberContinuousCellRenderer;
import org.cytoscape.view.vizmap.gui.internal.view.editor.propertyeditor.CyNumberPropertyEditor;
import org.cytoscape.view.vizmap.gui.editor.VisualPropertyEditor2;
import org.cytoscape.view.model.VisualProperty;
import java.beans.PropertyEditor;

public class NumberVisualPropertyEditor<T extends Number> extends BasicVisualPropertyEditor<T> implements VisualPropertyEditor2<T> {

	/**
	 * Creates a new DiscreteNumber object.
	 * @param cellRendererFactory 
	 */
	public NumberVisualPropertyEditor(final Class<T> type,
									  final ContinuousMappingCellRendererFactory cellRendererFactory) {
		super(type, new CyNumberPropertyEditor<T>(type), ContinuousEditorType.CONTINUOUS, cellRendererFactory);
		discreteTableCellRenderer = REG.getRenderer(type);
	}

	@Override
	public TableCellRenderer getContinuousTableCellRenderer(
			final ContinuousMappingEditor<? extends Number, T> continuousMappingEditor) {
		return new NumberContinuousCellRenderer((ContinuousMappingEditor<?, ?>) continuousMappingEditor);
	}

	public PropertyEditor getPropertyEditor() {
		final CyNumberPropertyEditor<T> propertyEditor = (CyNumberPropertyEditor<T>) super.getPropertyEditor();
		propertyEditor.setVisualProperty(null);
		return propertyEditor;
	}

	public PropertyEditor getPropertyEditor(VisualProperty<T> vizProp) {
		final CyNumberPropertyEditor<T> propertyEditor = (CyNumberPropertyEditor<T>) super.getPropertyEditor();
		propertyEditor.setVisualProperty(vizProp);
		return propertyEditor;
	}
}
