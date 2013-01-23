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

import java.beans.PropertyEditor;

import org.cytoscape.view.vizmap.gui.editor.AbstractVisualPropertyEditor;
import org.cytoscape.view.vizmap.gui.editor.ContinuousEditorType;
import org.cytoscape.view.vizmap.gui.editor.ContinuousMappingCellRendererFactory;

import com.l2fprod.common.propertysheet.PropertyRendererRegistry;

/**
 * Basic 
 *
 * @param <T> Data type for this editor.
 */
public abstract class BasicVisualPropertyEditor<T> extends AbstractVisualPropertyEditor<T> {

	protected static final PropertyRendererRegistry REG = new PropertyRendererRegistry();

	static {
		REG.registerDefaults();
	}

	public BasicVisualPropertyEditor(Class<T> type, PropertyEditor propertyEditor, ContinuousEditorType editorType, ContinuousMappingCellRendererFactory cellRendererFactory) {
		super(type, propertyEditor, editorType, cellRendererFactory);
	}
}
