package org.cytoscape.view.vizmap.gui.internal.editor.mappingeditor;

import java.awt.Color;
import java.awt.Paint;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkTableManager;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyTableEntry;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.gui.SelectedVisualStyleManager;
import org.cytoscape.view.vizmap.gui.editor.EditorManager;
import org.cytoscape.view.vizmap.mappings.ContinuousMapping;

public class GradientEditor extends AbstractContinuousMappingEditor<Double, Color> {

	public GradientEditor(final CyNetworkTableManager manager, final CyApplicationManager appManager,
			final SelectedVisualStyleManager selectedManager, final EditorManager editorManager,
			final VisualMappingManager vmm) {
		super(manager, appManager, selectedManager, editorManager, vmm);
	}

	@Override
	public void setValue(Object value) {
		if (value instanceof ContinuousMapping == false)
			throw new IllegalArgumentException("Value should be ContinuousMapping: this is " + value);
		final CyNetwork currentNetwork = appManager.getCurrentNetwork();
		if(currentNetwork == null)
			return;
		
		ContinuousMapping<?, ?> mTest = (ContinuousMapping<?, ?>) value;
		// TODO: error chekcing

		mapping = (ContinuousMapping<Double, Color>) value;
		@SuppressWarnings("unchecked")
		Class<? extends CyTableEntry> type = (Class<? extends CyTableEntry>) mapping.getVisualProperty()
				.getTargetDataType();
		final CyTable attr = manager.getTable(appManager.getCurrentNetwork(), type, CyNetwork.DEFAULT_ATTRS);
		this.editorPanel = new GradientEditorPanel(selectedManager.getCurrentVisualStyle(), mapping, attr, appManager,
				editorManager.getValueEditor(Paint.class), vmm);
	}
}
