package org.cytoscape.io.internal.util.vizmap;

/*
 * #%L
 * Cytoscape IO Impl (io-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2010 - 2013 The Cytoscape Consortium
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

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.cytoscape.view.presentation.property.BasicVisualLexicon;

/**
 * Simple factory that creates {@link  org.cytoscape.io.internal.util.vizmap.CalculatorConverter}
 * objects.
 * @author Christian
 */
public class CalculatorConverterFactory {

	/**
	 * @param calcKey The calculator identifier (e.g. "edgeColorCalculator" or "defaultEdgeColor").
	 * @return
	 */
	public Set<CalculatorConverter> getConverters(final String propsKey) {
		Set<CalculatorConverter> convs = new HashSet<>();

		if (CalculatorConverter.isConvertible(propsKey)) {
			Set<String> keys = updateLegacyPropsKey(propsKey);

			for (String k : keys) {
				CalculatorConverter c = new CalculatorConverter(k, propsKey);
				convs.add(c);
			}
		}

		return convs;
	}
	
	/**
	 * Converts the key of an old vizmap properties file into one or more updated keys, if necessary.
	 * @param propsKey Set with one or more updated keys.
	 * @return
	 */
	static Set<String> updateLegacyPropsKey(final String propsKey) {
		final Set<String> keys = new HashSet<>();
		final String[] tokens = propsKey.split("\\.");
		
		if (tokens.length < 3)
			return Collections.singleton(propsKey);
		
		final String prefix = tokens[0] + "." + tokens[1] + ".";
		final String vpId = tokens[2];

		if (vpId.matches("(?i)(default)?(node|edge)LineType(Calculator)?")) {
			// Split in two keys; e.g. defaultEdgeLineStyle + defaultEdgeLineWidth
			keys.add(prefix + vpId.replace("LineType", "LineStyle"));
			keys.add(prefix + vpId.replace("LineType", "LineWidth"));
		} else if (vpId.matches("(?i)(default)?Edge(Source|Target)Arrow(Calculator)?")) {
			// Split in two; e.g. defaultEdgeSourceArrowShape + defaultEdgeSourceArrowColor
			keys.add(prefix + vpId.replace("Arrow", "ArrowColor"));
			keys.add(prefix + vpId.replace("Arrow", "ArrowShape"));
		} else if (vpId.matches("(?i)(default)?EdgeColor(Calculator)?")) {
			// This Cy2 property is equivalent to two properties in Cy3
			String k1 = vpId.replaceAll("(?i)EdgeColor", BasicVisualLexicon.EDGE_UNSELECTED_PAINT.getIdString());
			String k2 = vpId.replaceAll("(?i)EdgeColor", BasicVisualLexicon.EDGE_STROKE_UNSELECTED_PAINT.getIdString());
			keys.add(prefix + k1);
			keys.add(prefix + k2);
		} else {
			// It is NOT an old key
			keys.add(propsKey);
		}
		
		return keys;
	}
}
