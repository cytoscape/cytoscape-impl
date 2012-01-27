package org.cytoscape.ding.impl.editor;

import org.cytoscape.ding.Bend;
import org.cytoscape.view.vizmap.gui.editor.AbstractVisualPropertyEditor;
import org.cytoscape.view.vizmap.gui.editor.ContinuousEditorType;
import org.cytoscape.view.vizmap.gui.editor.ValueEditor;

public class EdgeBendEditor extends AbstractVisualPropertyEditor<Bend>{

	public EdgeBendEditor(ValueEditor<Bend> valueEditor) {
		super(Bend.class, new EdgeBendPropertyEditor(valueEditor), ContinuousEditorType.DISCRETE);
		
		discreteTableCellRenderer = new EdgeBendCellRenderer();
	}
}
