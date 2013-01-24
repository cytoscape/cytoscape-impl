package org.cytoscape.view.vizmap.gui.internal;

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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.swing.JDialog;

import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.vizmap.gui.editor.EditorManager;

import com.l2fprod.common.propertysheet.Property;
import com.l2fprod.common.propertysheet.PropertySheetPanel;

public class EditorWindowManager {

	private Map<VisualProperty, JDialog> editorWindowMap;

	private EditorManager editorFactory;
	private PropertySheetPanel propertySheetPanel;

	public EditorWindowManager(
			EditorManager editorFactory, PropertySheetPanel propertySheetPanel) {
		this.editorFactory = editorFactory;
		this.propertySheetPanel = propertySheetPanel;

		editorWindowMap = new HashMap<VisualProperty, JDialog>();
	}

	public void manageWindow(final String status, VisualProperty vpt,
			Object source) {
		if (status.equals(EditorManager.EDITOR_WINDOW_OPENED)) {
			this.editorWindowMap.put(vpt, (JDialog) source);
		} else if (status.equals(EditorManager.EDITOR_WINDOW_CLOSED)) {
			final VisualProperty<?> type = vpt;

			/*
			 * Update icon
			 */
			final Property[] props = propertySheetPanel.getProperties();
			VizMapperProperty vprop = null;

			for (Property prop : props) {
				vprop = (VizMapperProperty) prop;

				//FIXME
//				if ((vprop.getHiddenObject() != null)
//						&& (type == vprop.getHiddenObject())) {
//					vprop = (VizMapperProperty) prop;
//
//					break;
//				}
			}

			final Property[] subProps = vprop.getSubProperties();
			vprop = null;

			String name = null;

			for (Property prop : subProps) {
				name = prop.getName();

				if ((name != null) && name.equals(type.getDisplayName())) {
					vprop = (VizMapperProperty) prop;

					break;
				}
			}

			final int width = propertySheetPanel.getTable().getCellRect(0, 1,
					true).width;

			//FIXME
//			final TableCellRenderer cRenderer = editorFactory
//					.getContinuousCellRenderer(type, width, 70);
//			rendReg.registerRenderer(vprop, cRenderer);
			propertySheetPanel.getTable().repaint();
		}
	}

	public void closeAllEditorWindows() {
		Set<VisualProperty> typeSet = editorWindowMap.keySet();
		Set<VisualProperty> keySet = new HashSet<VisualProperty>();

		for (VisualProperty vpt : typeSet) {
			JDialog window = editorWindowMap.get(vpt);
			manageWindow(EditorManager.EDITOR_WINDOW_CLOSED, vpt, null);
			window.dispose();
			keySet.add(vpt);
		}

		for (VisualProperty type : keySet)
			editorWindowMap.remove(type);
	}

	public void removeEditorWindow(VisualProperty type) {
		JDialog editor = editorWindowMap.get(type);
		if (editor == null)
			return;

		editor.dispose();
		editorWindowMap.remove(type);
	}

	public boolean isRegistered(VisualProperty type) {
		if (editorWindowMap.get(type) != null)
			return true;
		else
			return false;
	}

}
