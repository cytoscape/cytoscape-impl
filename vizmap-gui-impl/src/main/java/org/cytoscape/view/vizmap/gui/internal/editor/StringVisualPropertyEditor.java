package org.cytoscape.view.vizmap.gui.internal.editor;

import org.cytoscape.view.vizmap.gui.editor.ContinuousEditorType;
import org.cytoscape.view.vizmap.gui.editor.ContinuousMappingCellRendererFactory;
import org.cytoscape.view.vizmap.gui.internal.editor.propertyeditor.CyStringPropertyEditor;

public class StringVisualPropertyEditor extends BasicVisualPropertyEditor<String> {

	public StringVisualPropertyEditor(ContinuousMappingCellRendererFactory cellRendererFactory) {
		super(String.class, new CyStringPropertyEditor(), ContinuousEditorType.DISCRETE, cellRendererFactory);
		discreteTableCellRenderer = REG.getRenderer(String.class);
	}

}
