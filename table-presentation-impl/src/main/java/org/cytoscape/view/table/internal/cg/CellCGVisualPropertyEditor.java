package org.cytoscape.view.table.internal.cg;

import java.beans.PropertyEditor;

import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.presentation.property.table.CellCustomGraphics;
import org.cytoscape.view.vizmap.gui.editor.AbstractVisualPropertyEditor;
import org.cytoscape.view.vizmap.gui.editor.ContinuousEditorType;
import org.cytoscape.view.vizmap.gui.editor.ContinuousMappingCellRendererFactory;
import org.cytoscape.view.vizmap.gui.editor.VisualPropertyEditor2;

public class CellCGVisualPropertyEditor extends AbstractVisualPropertyEditor<CellCustomGraphics>
		implements VisualPropertyEditor2<CellCustomGraphics> {

	public CellCGVisualPropertyEditor(
			Class<CellCustomGraphics> type,
			CellCGValueEditor valueEditor,
			ContinuousMappingCellRendererFactory cellRendererFactory,
			CyServiceRegistrar serviceRegistrar
	) {
		super(type, new CellCGPropertyEditor(valueEditor, serviceRegistrar), ContinuousEditorType.DISCRETE,
				cellRendererFactory);
		discreteTableCellRenderer = new CellCGCellRenderer();
	}

	@Override
	public PropertyEditor getPropertyEditor() {
		var pe = (CellCGPropertyEditor) super.getPropertyEditor();
		pe.setVisualProperty(null);

		return pe;
	}

	@Override
	public PropertyEditor getPropertyEditor(VisualProperty<CellCustomGraphics> vp) {
		var pe = (CellCGPropertyEditor) super.getPropertyEditor();
		pe.setVisualProperty(vp);

		return pe;
	}
}
