package org.cytoscape.cg.internal.editor;

import java.beans.PropertyEditor;

import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.presentation.customgraphics.CyCustomGraphics;
import org.cytoscape.view.vizmap.gui.editor.AbstractVisualPropertyEditor;
import org.cytoscape.view.vizmap.gui.editor.ContinuousEditorType;
import org.cytoscape.view.vizmap.gui.editor.ContinuousMappingCellRendererFactory;
import org.cytoscape.view.vizmap.gui.editor.VisualPropertyEditor2;

@SuppressWarnings("rawtypes")
public class CustomGraphicsVisualPropertyEditor extends AbstractVisualPropertyEditor<CyCustomGraphics>
		implements VisualPropertyEditor2<CyCustomGraphics> {

	public CustomGraphicsVisualPropertyEditor(
			Class<CyCustomGraphics> type,
			CyCustomGraphicsValueEditor valueEditor,
			ContinuousMappingCellRendererFactory cellRendererFactory,
			CyServiceRegistrar serviceRegistrar
	) {
		super(type, new CyCustomGraphicsPropertyEditor(valueEditor, serviceRegistrar), ContinuousEditorType.DISCRETE,
				cellRendererFactory);
		discreteTableCellRenderer = new CyCustomGraphicsCellRenderer();
	}

	@Override
	public PropertyEditor getPropertyEditor() {
		var pe = (CyCustomGraphicsPropertyEditor) super.getPropertyEditor();
		pe.setVisualProperty(null);

		return pe;
	}

	@Override
	public PropertyEditor getPropertyEditor(VisualProperty<CyCustomGraphics> vp) {
		var pe = (CyCustomGraphicsPropertyEditor) super.getPropertyEditor();
		pe.setVisualProperty(vp);

		return pe;
	}
}
