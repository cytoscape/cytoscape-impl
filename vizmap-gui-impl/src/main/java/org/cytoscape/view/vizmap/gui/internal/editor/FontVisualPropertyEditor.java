package org.cytoscape.view.vizmap.gui.internal.editor;

import java.awt.Font;

import org.cytoscape.view.vizmap.gui.editor.ContinuousEditorType;
import org.cytoscape.view.vizmap.gui.editor.ContinuousMappingCellRendererFactory;
import org.cytoscape.view.vizmap.gui.internal.cellrenderer.FontTableCellRenderer;
import org.cytoscape.view.vizmap.gui.internal.editor.propertyeditor.CyFontPropertyEditor;

public class FontVisualPropertyEditor extends BasicVisualPropertyEditor<Font> {
	public FontVisualPropertyEditor(Class<Font> type, CyFontPropertyEditor fontPropEditor, ContinuousMappingCellRendererFactory cellRendererFactory) {
		super(type, fontPropEditor, ContinuousEditorType.DISCRETE, cellRendererFactory);
		discreteTableCellRenderer = new FontTableCellRenderer();
	}
}