package org.cytoscape.view.vizmap.gui.internal.view.legend;

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

import java.awt.BorderLayout;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import org.cytoscape.view.vizmap.gui.internal.util.ServicesUtil;
import org.cytoscape.view.vizmap.mappings.DiscreteMapping;

@SuppressWarnings("serial")
public class DiscreteLegendPanel extends AbstractMappingLegendPanel {

	public DiscreteLegendPanel(final DiscreteMapping<?, ?> discMapping, final ServicesUtil servicesUtil) {
		super(discMapping, servicesUtil);
		
		add(getTitleLabel(), BorderLayout.NORTH);

		final Map<?, ?> legendMap = new TreeMap<Object, Object>(discMapping.getAll());
		
		// Build Key array
		final Object[][] data = new Object[legendMap.keySet().size()][2];
		final Iterator<?> it = legendMap.keySet().iterator();

		for (int i = 0; i < legendMap.keySet().size(); i++) {
			Object key = it.next();
			data[i][0] = legendMap.get(key);
			data[i][1] = key;
		}
		
		final String columnName = discMapping.getMappingColumnName();
		
		add(LegendTable.getHeader(columnName, visualProperty), BorderLayout.CENTER);
		add(new LegendTable(data, visualProperty, servicesUtil), BorderLayout.SOUTH);
	}
}
