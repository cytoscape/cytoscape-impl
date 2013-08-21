package org.cytoscape.view.vizmap.gui.internal.view.editor.mappingeditor;

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

import org.cytoscape.view.model.VisualLexicon;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.gui.internal.util.ServicesUtil;

/**
 *
 */
public class EditorValueRangeTracer {

	private final Map<VisualProperty<?>, Range> rangeMap;

	/**
	 * Creates a new EditorValueRangeTracer object.
	 * 
	 * @param vpCatalog
	 *            DOCUMENT ME!
	 */
	public EditorValueRangeTracer(final ServicesUtil servicesUtil) {

		final VisualMappingManager vmMgr = servicesUtil.get(VisualMappingManager.class);
		final Set<VisualLexicon> lexSet = vmMgr.getAllVisualLexicon();
		rangeMap = new HashMap<VisualProperty<?>, Range>();

		for (VisualLexicon lexicon : lexSet) {
			for (VisualProperty<?> v : lexicon.getAllVisualProperties()) {
				Range r = new Range(0d, 0d);
				rangeMap.put(v, r);
			}
		}
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @param t
	 *            DOCUMENT ME!
	 * 
	 * @return DOCUMENT ME!
	 */
	public Double getRange(VisualProperty<?> t) {
		return rangeMap.get(t).getRange();
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @param t
	 *            DOCUMENT ME!
	 * 
	 * @return DOCUMENT ME!
	 */
	public Double getMin(VisualProperty<?> t) {
		return rangeMap.get(t).getMin();
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @param t
	 *            DOCUMENT ME!
	 * 
	 * @return DOCUMENT ME!
	 */
	public Double getMax(VisualProperty<?> t) {
		return rangeMap.get(t).getMax();
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @param t
	 *            DOCUMENT ME!
	 * @param min
	 *            DOCUMENT ME!
	 */
	public void setMin(VisualProperty<?> t, Double min) {
		rangeMap.get(t).setMin(min);
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @param t
	 *            DOCUMENT ME!
	 * @param max
	 *            DOCUMENT ME!
	 */
	public void setMax(VisualProperty<?> t, Double max) {
		rangeMap.get(t).setMax(max);
	}

	private final class Range {
		private Double min;
		private Double max;

		public Range(Double min, Double max) {
			this.min = min;
			this.max = max;
		}

		public void setMin(final Double min) {
			this.min = min;
		}

		public void setMax(final Double max) {
			this.max = max;
		}

		public Double getMin() {
			return min;
		}

		public Double getMax() {
			return max;
		}

		public Double getRange() {
			return Math.abs(min - max);
		}
	}
}
