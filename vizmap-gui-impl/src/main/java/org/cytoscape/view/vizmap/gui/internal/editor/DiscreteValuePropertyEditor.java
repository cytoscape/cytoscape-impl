package org.cytoscape.view.vizmap.gui.internal.editor;

import java.util.Set;

import org.cytoscape.model.CyTableManager;
import org.cytoscape.session.CyApplicationManager;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.gui.SelectedVisualStyleManager;
import org.cytoscape.view.vizmap.gui.editor.EditorManager;
import org.cytoscape.view.vizmap.gui.internal.cellrenderer.ContinuousMappingCellRenderer;
import org.cytoscape.view.vizmap.gui.internal.editor.mappingeditor.AbstractContinuousMappingEditor;
import org.cytoscape.view.vizmap.gui.internal.editor.mappingeditor.C2DEditor;
import org.cytoscape.view.vizmap.gui.internal.editor.propertyeditor.CyComboBoxPropertyEditor;

/**
 * Editor object for all kinds of discrete values such as Node Shape, Line
 * Stroke, etc.
 * 
 * 
 * @param <T>
 */
public class DiscreteValuePropertyEditor<T> extends BasicVisualPropertyEditor<T> {

	public DiscreteValuePropertyEditor(Class<T> type, Set<T> values, final CyTableManager manager,
			final CyApplicationManager appManager, final SelectedVisualStyleManager selectedManager,
			final EditorManager editorManager, final VisualMappingManager vmm) {
		super(type, new CyComboBoxPropertyEditor());

		discreteTableCellRenderer = REG.getRenderer(type);
		
		CyComboBoxPropertyEditor cbe = (CyComboBoxPropertyEditor) propertyEditor;
		cbe.setAvailableValues(values.toArray());
		this.continuousEditor = new C2DEditor<T>(manager, appManager, selectedManager, editorManager, vmm);
		this.continuousTableCellRenderer = new ContinuousMappingCellRenderer((AbstractContinuousMappingEditor<?, ?>) continuousEditor);
	}
}
