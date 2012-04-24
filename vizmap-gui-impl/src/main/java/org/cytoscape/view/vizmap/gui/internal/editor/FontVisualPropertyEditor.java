
package org.cytoscape.view.vizmap.gui.internal.editor;


import java.awt.Font;
import java.beans.PropertyEditor;

import javax.swing.table.TableCellRenderer;

import org.cytoscape.view.vizmap.gui.VizMapGUI;
import org.cytoscape.view.vizmap.gui.editor.AbstractVisualPropertyEditor;
import org.cytoscape.view.vizmap.gui.editor.ContinuousEditorType;
import org.cytoscape.view.vizmap.gui.editor.ContinuousMappingEditor;
import org.cytoscape.view.vizmap.gui.editor.EditorManager;
import org.cytoscape.view.vizmap.gui.internal.editor.propertyeditor.CyFontPropertyEditor;


public class FontVisualPropertyEditor extends AbstractVisualPropertyEditor<Font> {

	private final EditorManager editorManager;
	private final VizMapGUI vizMapGUI;
	
	public FontVisualPropertyEditor(Class<Font> type, EditorManager editorManager, VizMapGUI vizMapGUI) {
		super(type, new CyFontPropertyEditor(), ContinuousEditorType.DISCRETE);
		this.editorManager = editorManager;
		this.vizMapGUI = vizMapGUI;
		
//		this.tableCellRenderer = new FontCellRenderer();
//		this.continuousEditor = new C2DMappingEditor<Font>(this.vp, editorManager, vizMapGUI);
	}

	@Override
	public TableCellRenderer getContinuousTableCellRenderer(ContinuousMappingEditor<? extends Number, Font> continuousMappingEditor) {
		// TODO Auto-generated method stub
		//return new C2DMappingEditor<Font>(vp, editorManager, vizMapGUI);
		return null;
	}



}
