package org.cytoscape.ding.impl.events;

/*
 * #%L
 * Cytoscape Ding View/Presentation Impl (ding-presentation-impl)
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

import org.cytoscape.ding.GraphViewChangeEvent;
import org.cytoscape.ding.GraphViewChangeListener;


public class GraphViewChangeListenerChain implements GraphViewChangeListener {
	private final GraphViewChangeListener a;
	private final GraphViewChangeListener b;

	private GraphViewChangeListenerChain(GraphViewChangeListener a, GraphViewChangeListener b) {
		this.a = a;
		this.b = b;
	}

	/**
	 * DOCUMENT ME!
	 *
	 * @param evt DOCUMENT ME!
	 */
	public void graphViewChanged(GraphViewChangeEvent evt) {
		a.graphViewChanged(evt);
		b.graphViewChanged(evt);
	}

	public static GraphViewChangeListener add(GraphViewChangeListener a, GraphViewChangeListener b) {
		if (a == null)
			return b;

		if (b == null)
			return a;

		return new GraphViewChangeListenerChain(a, b);
	}

	public static GraphViewChangeListener remove(GraphViewChangeListener l, GraphViewChangeListener oldl) {
		if ((l == oldl) || (l == null))
			return null;
		else if (l instanceof GraphViewChangeListenerChain)
			return ((GraphViewChangeListenerChain) l).remove(oldl);
		else

			return l;
	}

	private GraphViewChangeListener remove(GraphViewChangeListener oldl) {
		if (oldl == a)
			return b;

		if (oldl == b)
			return a;

		GraphViewChangeListener a2 = remove(a, oldl);
		GraphViewChangeListener b2 = remove(b, oldl);

		if ((a2 == a) && (b2 == b))
			return this;

		return add(a2, b2);
	}
}
