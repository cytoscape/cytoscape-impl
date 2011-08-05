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

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Set;

/**
 * Mapping generator from any attributes to random color
 */
public class RandomColorMappingGenerator extends
		AbstractDiscreteMappingGenerator<Color> {

	public RandomColorMappingGenerator(Class<Color> type) {
		super(type);
	}

	private final int MAX_COLOR = 256 * 256 * 256;
	private final long seed = System.currentTimeMillis();
	private final Random rand = new Random(seed);

	/**
	 * From a given set of attributes, create a discrete mapping from the
	 * attribute to random color.
	 * 
	 * @param <T>
	 *            Attribute type
	 * @param attributeSet
	 *            Set of attribute values
	 * 
	 * @return map from T to Color
	 */
	public <T> Map<T, Color> generateMap(Set<T> attributeSet) {
		final Map<T, Color> valueMap = new HashMap<T, Color>();

		for (T key : attributeSet)
			valueMap.put(key, new Color(
					((Number) (rand.nextFloat() * MAX_COLOR)).intValue()));

		return valueMap;
	}
}
