package org.cytoscape.view.vizmap.gui.internal.editor;

import org.cytoscape.view.presentation.customgraphics.CyCustomGraphics;
import org.cytoscape.view.vizmap.gui.editor.ContinuousEditorType;
import org.cytoscape.view.vizmap.gui.internal.cellrenderer.FontTableCellRenderer;
import org.cytoscape.view.vizmap.gui.internal.editor.propertyeditor.CyFontPropertyEditor;

public class CustomGraphicsVisualPropertyEditor extends BasicVisualPropertyEditor<CyCustomGraphics<?>> {
	
	public CustomGraphicsVisualPropertyEditor(Class<CyCustomGraphics<?>> type, CyFontPropertyEditor fontPropEditor) {
		super(type, fontPropEditor, ContinuousEditorType.DISCRETE);
		discreteTableCellRenderer = new FontTableCellRenderer();
	}
}
