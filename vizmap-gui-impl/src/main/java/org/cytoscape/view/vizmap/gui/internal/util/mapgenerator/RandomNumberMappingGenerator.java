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
import javax.swing.SwingUtilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public class RandomNumberMappingGenerator extends AbstractDiscreteMappingGenerator<Number> {

	private static final Logger logger = LoggerFactory.getLogger(RandomNumberMappingGenerator.class);
	
	public RandomNumberMappingGenerator() {
		super(Number.class);
	}

	@Override
	public <T> Map<T, Number> generateMap(Set<T> attributeSet) {
		final Map<T, Number> valueMap = new HashMap<T, Number>();
		
		// Error if attributeSet is empty or null
		if ((attributeSet == null) || (attributeSet.size() == 0))
			return valueMap;

		final String[] range = new String[2];
		
		if (SwingUtilities.isEventDispatchThread()) {
			getRange(range);
		} else {
			try {
				SwingUtilities.invokeAndWait(new Runnable() {
					@Override
					public void run() {
						getRange(range);
					}
				});
			} catch (Exception e) {
				logger.error("Error getting range", e);
				return valueMap;
			}
		}
		
		final Double min;
		final Double max;
		
		try {
			min = Double.valueOf(range[0]);
			max = Double.valueOf(range[1]);
		} catch (Exception e) {
			return generateMap(attributeSet);
		}
		
		final Double valueRange = max - min;
		final long seed = System.currentTimeMillis();
		final Random rand = new Random(seed);

		for (T key : attributeSet)
			valueMap.put(key, (rand.nextFloat() * valueRange) + min);

		return valueMap;
	}

	private void getRange(final String[] range) {
		// Ask user to input number range
		final String s = JOptionPane.showInputDialog(null, "Please enter the value range (example: 30-100):",
				"Assign Random Numbers", JOptionPane.PLAIN_MESSAGE);
		
		final String[] split = s.split("-");
		
		if (split.length == 2) {
			range[0] = split[0];
			range[1] = split[1];
		}
	}
}
