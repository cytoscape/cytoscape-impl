package org.cytoscape.view.vizmap.gui.internal.editor;

import java.beans.PropertyEditor;

import org.cytoscape.view.vizmap.gui.editor.ContinuousEditorType;
import org.cytoscape.view.vizmap.gui.editor.ContinuousMappingCellRendererFactory;

public class BooleanVisualPropertyEditor extends BasicVisualPropertyEditor<Boolean> {

	public BooleanVisualPropertyEditor(final PropertyEditor editor, ContinuousMappingCellRendererFactory cellRendererFactory) {
		super(Boolean.class, editor, ContinuousEditorType.DISCRETE, cellRendererFactory);
		
		discreteTableCellRenderer = REG.getRenderer(Boolean.class);
	}

}