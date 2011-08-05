package org.cytoscape.view.vizmap.gui.internal;

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
