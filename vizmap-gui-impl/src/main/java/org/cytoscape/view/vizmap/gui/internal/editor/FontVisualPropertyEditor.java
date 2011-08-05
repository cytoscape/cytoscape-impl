
package org.cytoscape.view.vizmap.gui.internal.editor;


import java.awt.Font;

import org.cytoscape.view.vizmap.gui.VizMapGUI;
import org.cytoscape.view.vizmap.gui.editor.AbstractVisualPropertyEditor;
import org.cytoscape.view.vizmap.gui.editor.EditorManager;
import org.cytoscape.view.vizmap.gui.internal.editor.propertyeditor.CyFontPropertyEditor;


public class FontVisualPropertyEditor extends AbstractVisualPropertyEditor<Font> {

	public FontVisualPropertyEditor(Class<Font> type, EditorManager editorManager, VizMapGUI vizMapGUI) {
		super(type, new CyFontPropertyEditor());
//		this.tableCellRenderer = new FontCellRenderer();
//		this.continuousEditor = new C2DMappingEditor<Font>(this.vp, editorManager, vizMapGUI);
	}



}
