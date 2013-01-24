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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.swing.JOptionPane;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NumberSeriesMappingGenerator<V extends Number> extends AbstractDiscreteMappingGenerator<V> {

	private static final Logger logger = LoggerFactory.getLogger(NumberSeriesMappingGenerator.class);

	public NumberSeriesMappingGenerator(final Class<V> type) {
		super(type);
	}

	@Override
	public <T> Map<T, V> generateMap(final Set<T> attributeSet) {

		final Map<T, V> valueMap = new HashMap<T, V>();

		// Error check
		if (attributeSet == null || attributeSet.size() == 0)
			return valueMap;

		final String start = JOptionPane.showInputDialog(null, "Enter start value (1st number of the series)", "0");
		final String increment = JOptionPane.showInputDialog(null, "Enter increment", "1");

		if ((increment == null) || (start == null))
			return valueMap;

		Double inc;
		Double st;
		try {
			inc = Double.valueOf(increment);
			st = Double.valueOf(start);
		} catch (Exception ex) {
			logger.error("Invalid value.", ex);
			inc = null;
			st = null;
		}

		if ((inc == null) || (inc.doubleValue() < 0) || (st == null))
			return null;

		for (T key : attributeSet) {
			valueMap.put(key, (V) st);
			st = st + inc;
		}

		return valueMap;
	}

}
