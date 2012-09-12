package org.cytoscape.view.vizmap.gui.internal.editor.mappingeditor;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkTableManager;
import org.cytoscape.model.CyTable;
import org.cytoscape.view.vizmap.VisualMappingFunctionFactory;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.gui.editor.EditorManager;
import org.cytoscape.view.vizmap.mappings.ContinuousMapping;

public class C2CEditor<K extends Number, V extends Number> extends AbstractContinuousMappingEditor<K, V> {

	public C2CEditor(final CyNetworkTableManager manager, final CyApplicationManager appManager,
			final EditorManager editorManager, final VisualMappingManager vmm, VisualMappingFunctionFactory continuousMappingFactory) {
		super(manager, appManager, editorManager, vmm, continuousMappingFactory);
	}

	@Override
	public void setValue(Object value) {
		if (value instanceof ContinuousMapping == false)
			throw new IllegalArgumentException("Value should be ContinuousMapping: this is " + value);
		
		final CyNetwork currentNetwork = appManager.getCurrentNetwork();
		if (currentNetwork == null)
			return;

		mapping = (ContinuousMapping<K, V>) value;
		@SuppressWarnings("unchecked")
		Class<? extends CyIdentifiable> type = (Class<? extends CyIdentifiable>) mapping.getVisualProperty()
				.getTargetDataType();
		final CyTable attr = manager.getTable(appManager.getCurrentNetwork(), type, CyNetwork.DEFAULT_ATTRS);
		this.editorPanel = new C2CMappingEditorPanel<K, V>(vmm.getCurrentVisualStyle(), mapping, attr, appManager, vmm, continuousMappingFactory);
	}
}