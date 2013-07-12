package org.cytoscape.view.vizmap.gui.internal.view.editor.mappingeditor;

/*
 * #%L
 * Cytoscape VizMap GUI Impl (vizmap-gui-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2013 The Cytoscape Consortium
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as 
 * published by the Free Software Foundation, either version 2.1 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */

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

	public C2CEditor(final CyNetworkTableManager manager,
					 final CyApplicationManager appManager,
					 final EditorManager editorManager,
					 final VisualMappingManager vmm,
					 final VisualMappingFunctionFactory continuousMappingFactory) {
		super(manager, appManager, editorManager, vmm, continuousMappingFactory);
	}

	@Override
	@SuppressWarnings("unchecked")
	public void setValue(Object value) {
		if (value instanceof ContinuousMapping == false)
			throw new IllegalArgumentException("Value should be ContinuousMapping: this is " + value);
		
		final CyNetwork currentNetwork = appManager.getCurrentNetwork();
		if (currentNetwork == null)
			return;

		mapping = (ContinuousMapping<K, V>) value;
		Class<? extends CyIdentifiable> type = (Class<? extends CyIdentifiable>) mapping.getVisualProperty()
				.getTargetDataType();
		final CyTable attr = manager.getTable(appManager.getCurrentNetwork(), type, CyNetwork.DEFAULT_ATTRS);
		this.editorPanel = new C2CMappingEditorPanel<K, V>(vmm.getCurrentVisualStyle(), mapping, attr, appManager, vmm, continuousMappingFactory);
	}
}