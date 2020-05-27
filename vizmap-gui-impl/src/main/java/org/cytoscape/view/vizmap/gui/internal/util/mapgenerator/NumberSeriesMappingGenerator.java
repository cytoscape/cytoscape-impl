package org.cytoscape.view.vizmap.gui.internal.util.mapgenerator;

import static org.cytoscape.view.vizmap.gui.internal.view.util.ViewUtil.invokeOnEDTAndWait;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.swing.JOptionPane;

import org.cytoscape.application.CyUserLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * #%L
 * Cytoscape VizMap GUI Impl (vizmap-gui-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2019 The Cytoscape Consortium
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

public class NumberSeriesMappingGenerator<V extends Number> extends AbstractDiscreteMappingGenerator<V> {

	private static final Logger logger = LoggerFactory.getLogger(CyUserLog.NAME);

	public NumberSeriesMappingGenerator(final Class<V> type) {
		super(type);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> Map<T, V> generateMap(final Set<T> attributeSet) {
		final Map<T, V> valueMap = new HashMap<>();

		// Error check
		if (attributeSet == null || attributeSet.size() == 0)
			return valueMap;

		final Double[] params = new Double[2];
		
		invokeOnEDTAndWait(() -> {
			getStartAndIncrement(params);
		}, logger);

		Double st = params[0];
		Double inc = params[1];
		
		if (st != null && inc != null && inc.doubleValue() >= 0) {
			for (T key : attributeSet) {
				valueMap.put(key, (V) st);
				st = st + inc;
			}
		}

		return valueMap;
	}
	
	private void getStartAndIncrement(final Double[] params) {
		final String start = JOptionPane.showInputDialog(null, "Enter start value (1st number of the series)", "0");
		final String increment = JOptionPane.showInputDialog(null, "Enter increment", "1");
		
		if (start != null) {
			Double st = null;
			Double inc = null;
			
			try {
				st = Double.valueOf(start);
				inc = Double.valueOf(increment);
			} catch (Exception ex) {
				logger.warn("Invalid numeric value.", ex);
			}
	
			if (inc == null)
				inc = 0d;
			
			params[0] = st;
			params[1] = inc;
		}
	}
}
