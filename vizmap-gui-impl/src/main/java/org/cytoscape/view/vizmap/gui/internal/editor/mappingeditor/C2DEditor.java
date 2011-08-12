package org.cytoscape.view.vizmap.gui.internal.editor.mappingeditor;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.gui.SelectedVisualStyleManager;
import org.cytoscape.view.vizmap.gui.editor.EditorManager;
import org.cytoscape.view.vizmap.mappings.ContinuousMapping;

public class C2DEditor<V> extends AbstractContinuousMappingEditor<Number, V> {

	public C2DEditor(final CyTableManager manager,
			final CyApplicationManager appManager,
			final SelectedVisualStyleManager selectedManager,
			final EditorManager editorManager, final VisualMappingManager vmm) {
		super(manager, appManager, selectedManager, editorManager, vmm);
	}

	@Override
	public void setValue(Object value) {
		if (value instanceof ContinuousMapping == false)
			throw new IllegalArgumentException(
					"Value should be ContinuousMapping: this is " + value);
		
		ContinuousMapping<?, ?> mTest = (ContinuousMapping<?, ?>) value;

		// TODO: error chekcing
		mapping = (ContinuousMapping<Number, V>) value;
		final CyTable attr = manager.getTableMap(
				mapping.getVisualProperty().getTargetDataType(),
				appManager.getCurrentNetwork()).get(CyNetwork.DEFAULT_ATTRS);
		this.editorPanel = new C2DMappingEditor(
				selectedManager.getCurrentVisualStyle(), mapping, attr,
				appManager, vmm, editorManager);
	}
}