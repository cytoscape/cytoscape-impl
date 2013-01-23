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

import java.util.HashSet;
import java.util.Set;

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
	public Set<CalculatorConverter> getConverters(String propsKey) {
		Set<CalculatorConverter> convs = new HashSet<CalculatorConverter>();

		if (CalculatorConverter.isConvertible(propsKey)) {
			Set<String> keys = new HashSet<String>();
			String legacyPropsKey = null;

			// Old/deprecated styles need to be converted to new properties first!
			if (propsKey.matches("(?i).*(default)?(node|edge)LineType(Calculator)?")) {
				// Split in two keys; e.g. defaultEdgeLineStyle + defaultEdgeLineWidth
				legacyPropsKey = propsKey;
				keys.add(propsKey.replace("LineType", "LineStyle"));
				keys.add(propsKey.replace("LineType", "LineWidth"));
			} else if (propsKey.matches("(?i).*(default)?Edge(Source|Target)Arrow(Calculator)?")) {
				// Split in two; e.g. defaultEdgeSourceArrowShape + defaultEdgeSourceArrowColor
				legacyPropsKey = propsKey;
				keys.add(propsKey.replace("Arrow", "ArrowColor"));
				keys.add(propsKey.replace("Arrow", "ArrowShape"));
			} else {
				// It is NOT an old key
				keys.add(propsKey);
			}

			for (String k : keys) {
				CalculatorConverter c = new CalculatorConverter(k, legacyPropsKey);
				convs.add(c);
			}
		}

		return convs;
	}
}
