package org.cytoscape.view.vizmap.gui.internal.editor;

import java.beans.PropertyEditor;

import org.cytoscape.view.vizmap.gui.editor.ContinuousEditorType;

public class BooleanVisualPropertyEditor extends BasicVisualPropertyEditor<Boolean> {

	public BooleanVisualPropertyEditor(final PropertyEditor editor) {
		super(Boolean.class, editor, ContinuousEditorType.DISCRETE);
		discreteTableCellRenderer = REG.getRenderer(Boolean.class);
	}

}