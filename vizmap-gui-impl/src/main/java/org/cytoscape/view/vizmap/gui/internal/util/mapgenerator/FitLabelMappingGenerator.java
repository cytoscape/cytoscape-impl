package org.cytoscape.view.vizmap.gui.internal.util.mapgenerator;

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

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.vizmap.VisualMappingFunction;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyle;


/**
 * Generates node width based on label size.  Only accepts SUID as the key.
 *
 */
public class FitLabelMappingGenerator<V extends Number> extends AbstractDiscreteMappingGenerator<V>{

	private final CyApplicationManager appManager;
	private final VisualMappingManager vmm;
	
	public FitLabelMappingGenerator(final Class<V> type, final CyApplicationManager appManager,
			final VisualMappingManager vmm) {
		super(type);
		this.vmm = vmm;
		this.appManager = appManager;
	}

	@Override
	public <T> Map<T, V> generateMap(final Set<T> tableValues) {
		// Generate map for the current network view.
		final CyNetworkView networkView = appManager.getCurrentNetworkView();
		
		// If current view is not available, simply return empty map.
		if(networkView == null)
			return Collections.emptyMap();
		
		// If given set is empty, return empty map.
		if(tableValues == null || tableValues.isEmpty())
			return Collections.emptyMap();
		
		// This only works with NAME column.
		final T testName = tableValues.iterator().next();
		if(testName instanceof String == false)
			throw new IllegalArgumentException("This generator only works with Name column.");
	
		final CyNetwork network = networkView.getModel();
		final CyTable nodeTable = networkView.getModel().getDefaultNodeTable();
		
		final VisualStyle style = vmm.getCurrentVisualStyle();
		// Check label size mapping exists or not
		final VisualMappingFunction<?, Integer> fontSizeMapping = style.getVisualMappingFunction(BasicVisualLexicon.NODE_LABEL_FONT_SIZE);
		// Use default label width for checking.  TODO: should we use mapping?
		final Double maxLabelWidth = style.getDefaultValue(BasicVisualLexicon.NODE_LABEL_WIDTH);
		final Map<T, V> valueMap = new HashMap<T, V>();
		
		for(final T attrVal: tableValues) {
			final Collection<CyRow> rows = nodeTable.getMatchingRows(CyNetwork.NAME, attrVal);
			CyRow row = null;
			if(rows.isEmpty() == false)
				row = rows.iterator().next();
			else
				continue;
			
			final Long suid = row.get(CyIdentifiable.SUID, Long.class);
			final View<CyNode> nodeView = networkView.getNodeView(network.getNode(suid));
			if(nodeView == null)
				continue;
			
			final String labelText = nodeView.getVisualProperty(BasicVisualLexicon.NODE_LABEL);
			final int textLen = labelText.length();
			final int fontSize;
			if(fontSizeMapping == null)
				fontSize = style.getDefaultValue(BasicVisualLexicon.NODE_LABEL_FONT_SIZE);
			else
				fontSize = nodeView.getVisualProperty(BasicVisualLexicon.NODE_LABEL_FONT_SIZE);
			
			final Double width = fontSize*textLen*0.7;
			if(maxLabelWidth>width)
				valueMap.put(attrVal, (V) width);
			else {
				valueMap.put(attrVal, (V) maxLabelWidth);
			}
		}
		
		return valueMap;
	}
}
