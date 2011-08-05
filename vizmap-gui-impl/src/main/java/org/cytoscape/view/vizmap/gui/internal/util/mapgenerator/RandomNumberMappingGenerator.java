/*
 Copyright (c) 2006, 2007, The Cytoscape Consortium (www.cytoscape.org)

 The Cytoscape Consortium is:
 - Institute for Systems Biology
 - University of California San Diego
 - Memorial Sloan-Kettering Cancer Center
 - Institut Pasteur
 - Agilent Technologies

 This library is free software; you can redistribute it and/or modify it
 under the terms of the GNU Lesser General Public License as published
 by the Free Software Foundation; either version 2.1 of the License, or
 any later version.

 This library is distributed in the hope that it will be useful, but
 WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
 MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
 documentation provided hereunder is on an "as is" basis, and the
 Institute for Systems Biology and the Whitehead Institute
 have no obligations to provide maintenance, support,
 updates, enhancements or modifications.  In no event shall the
 Institute for Systems Biology and the Whitehead Institute
 be liable to any party for direct, indirect, special,
 incidental or consequential damages, including lost profits, arising
 out of the use of this software and its documentation, even if the
 Institute for Systems Biology and the Whitehead Institute
 have been advised of the possibility of such damage.  See
 the GNU Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public License
 along with this library; if not, write to the Free Software Foundation,
 Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 */
package org.cytoscape.view.vizmap.gui.internal.util.mapgenerator;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import javax.swing.JOptionPane;

/**
 *
 */
public class RandomNumberMappingGenerator extends
		AbstractDiscreteMappingGenerator<Number> {
	
	public RandomNumberMappingGenerator(Class<Number> type) {
		super(type);
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
		// Error if attributeSet is empty or null
		if ((attributeSet == null) || (attributeSet.size() == 0))
			return null;

		// Ask user to input number range
		final String range = JOptionPane.showInputDialog(null,
				"Please enter the value range (example: 30-100)",
				"Assign Random Numbers", JOptionPane.PLAIN_MESSAGE);

		String[] rangeVals = range.split("-");

		if (rangeVals.length != 2)
			return null;

		final long seed = System.currentTimeMillis();
		final Random rand = new Random(seed);
		final Map<T, Number> valueMap = new HashMap<T, Number>();

		Float min = Float.valueOf(rangeVals[0]);
		Float max = Float.valueOf(rangeVals[1]);
		Float valueRange = max - min;

		for (T key : attributeSet)
			valueMap.put(key, (rand.nextFloat() * valueRange) + min);

		return valueMap;
	}
}
