package org.cytoscape.ding.impl.editor;

import org.cytoscape.ding.CyObjectPositionPropertyEditor;
import org.cytoscape.ding.ObjectPosition;
import org.cytoscape.ding.ObjectPositionCellRenderer;
import org.cytoscape.view.vizmap.gui.editor.AbstractVisualPropertyEditor;
import org.cytoscape.view.vizmap.gui.editor.ContinuousEditorType;
import org.cytoscape.view.vizmap.gui.editor.ValueEditor;

public class ObjectPositionEditor extends AbstractVisualPropertyEditor<ObjectPosition> {
	
	public ObjectPositionEditor(final ValueEditor<ObjectPosition> valueEditor) {
		super(ObjectPosition.class, new CyObjectPositionPropertyEditor(valueEditor), ContinuousEditorType.DISCRETE);

		discreteTableCellRenderer = new ObjectPositionCellRenderer();

	}
}
