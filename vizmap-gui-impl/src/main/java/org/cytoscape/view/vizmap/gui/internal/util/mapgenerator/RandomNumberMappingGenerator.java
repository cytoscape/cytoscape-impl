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
import java.util.Random;
import java.util.Set;

import javax.swing.JOptionPane;

/**
 *
 */
public class RandomNumberMappingGenerator extends AbstractDiscreteMappingGenerator<Number> {

	public RandomNumberMappingGenerator() {
		super(Number.class);
	}

	/**
	 * Generate discrete mapping between any attribute values and numbers.
	 * 
	 * @param attributeSet
	 *            set of attribute values. ? can be anything.
	 * 
	 * @return DOCUMENT ME!
	 */
	public <T> Map<T, Number> generateMap(Set<T> attributeSet) {
		
		final Map<T, Number> valueMap = new HashMap<T, Number>();
		
		// Error if attributeSet is empty or null
		if ((attributeSet == null) || (attributeSet.size() == 0))
			return valueMap;

		// Ask user to input number range
		final String range = JOptionPane.showInputDialog(null, "Please enter the value range (example: 30-100)",
				"Assign Random Numbers", JOptionPane.PLAIN_MESSAGE);

		String[] rangeVals = range.split("-");

		if (rangeVals.length != 2)
			return valueMap;

		final long seed = System.currentTimeMillis();
		final Random rand = new Random(seed);
		
		Double min = Double.valueOf(rangeVals[0]);
		Double max = Double.valueOf(rangeVals[1]);
		Double valueRange = max - min;

		for (T key : attributeSet)
			valueMap.put(key, (rand.nextFloat() * valueRange) + min);

		return valueMap;
	}
}
