package org.cytoscape.view.vizmap.gui.internal.editor;

import java.util.Set;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.model.CyNetworkTableManager;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.gui.editor.ContinuousEditorType;
import org.cytoscape.view.vizmap.gui.editor.EditorManager;
import org.cytoscape.view.vizmap.gui.internal.editor.propertyeditor.CyComboBoxPropertyEditor;

/**
 * Editor object for all kinds of discrete values such as Node Shape, Line
 * Stroke, etc.
 * 
 * 
 * @param <T>
 */
public class DiscreteValuePropertyEditor<T> extends BasicVisualPropertyEditor<T> {

	public DiscreteValuePropertyEditor(Class<T> type, Set<T> values, final CyNetworkTableManager manager,
			final CyApplicationManager appManager, final EditorManager editorManager, final VisualMappingManager vmm) {
		super(type, new CyComboBoxPropertyEditor(), ContinuousEditorType.DISCRETE);

		discreteTableCellRenderer = REG.getRenderer(type);

		CyComboBoxPropertyEditor cbe = (CyComboBoxPropertyEditor) propertyEditor;
		cbe.setAvailableValues(values.toArray());
		// this.continuousEditor = new C2DEditor<T>(manager, appManager,
		// selectedManager, editorManager, vmm);
		// this.continuousTableCellRenderer = new
		// ContinuousMappingCellRenderer((AbstractContinuousMappingEditor<?, ?>)
		// continuousEditor);
	}
}
