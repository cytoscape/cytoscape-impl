package org.cytoscape.ding.impl.editor;

import org.cytoscape.view.presentation.property.values.Bend;
import org.cytoscape.view.vizmap.gui.editor.AbstractVisualPropertyEditor;
import org.cytoscape.view.vizmap.gui.editor.ContinuousEditorType;
import org.cytoscape.view.vizmap.gui.editor.ContinuousMappingCellRendererFactory;
import org.cytoscape.view.vizmap.gui.editor.ValueEditor;

public class EdgeBendEditor extends AbstractVisualPropertyEditor<Bend>{

	public EdgeBendEditor(ValueEditor<Bend> valueEditor, ContinuousMappingCellRendererFactory cellRendererFactory) {
		super(Bend.class, new EdgeBendPropertyEditor(valueEditor), ContinuousEditorType.DISCRETE, cellRendererFactory);
		
		discreteTableCellRenderer = new EdgeBendCellRenderer();
	}
}
