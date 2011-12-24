package org.cytoscape.ding.impl.editor;

import org.cytoscape.ding.Bend;
import org.cytoscape.view.vizmap.gui.editor.AbstractVisualPropertyEditor;
import org.cytoscape.view.vizmap.gui.editor.ValueEditor;

public class EdgeBendEditor extends AbstractVisualPropertyEditor<Bend>{

	public EdgeBendEditor(Class<Bend> type, ValueEditor<Bend> valueEditor) {
		super(type, new EdgeBendPropertyEditor(valueEditor));
	}

}
