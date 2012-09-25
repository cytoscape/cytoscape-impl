package org.cytoscape.view.vizmap.gui.internal.editor;

import org.cytoscape.view.presentation.customgraphics.CyCustomGraphics;
import org.cytoscape.view.vizmap.gui.editor.ContinuousEditorType;
import org.cytoscape.view.vizmap.gui.editor.ContinuousMappingCellRendererFactory;
import org.cytoscape.view.vizmap.gui.internal.cellrenderer.FontTableCellRenderer;
import org.cytoscape.view.vizmap.gui.internal.editor.propertyeditor.CyFontPropertyEditor;

public class CustomGraphicsVisualPropertyEditor extends BasicVisualPropertyEditor<CyCustomGraphics<?>> {
	
	public CustomGraphicsVisualPropertyEditor(Class<CyCustomGraphics<?>> type, CyFontPropertyEditor fontPropEditor, ContinuousMappingCellRendererFactory cellRendererFactory) {
		super(type, fontPropEditor, ContinuousEditorType.DISCRETE, cellRendererFactory);
		discreteTableCellRenderer = new FontTableCellRenderer();
	}
}
