package org.cytoscape.view.vizmap.gui.internal.editor;

import org.cytoscape.view.vizmap.gui.editor.ContinuousEditorType;
import org.cytoscape.view.vizmap.gui.internal.editor.propertyeditor.CyStringPropertyEditor;

public class StringVisualPropertyEditor extends BasicVisualPropertyEditor<String> {

	public StringVisualPropertyEditor() {
		super(String.class, new CyStringPropertyEditor(), ContinuousEditorType.DISCRETE);
		discreteTableCellRenderer = REG.getRenderer(String.class);
	}

}
