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

public class ViewportChangeListenerChain implements ViewportChangeListener {
	private final ViewportChangeListener a;
	private final ViewportChangeListener b;

	private ViewportChangeListenerChain(ViewportChangeListener a, ViewportChangeListener b) {
		this.a = a;
		this.b = b;
	}

	/**
	 * DOCUMENT ME!
	 *
	 * @param w DOCUMENT ME!
	 * @param h DOCUMENT ME!
	 * @param newXCenter DOCUMENT ME!
	 * @param newYCenter DOCUMENT ME!
	 * @param newScaleFactor DOCUMENT ME!
	 */
	public void viewportChanged(int w, int h, double newXCenter, double newYCenter,
	                            double newScaleFactor) {
		a.viewportChanged(w, h, newXCenter, newYCenter, newScaleFactor);
		b.viewportChanged(w, h, newXCenter, newYCenter, newScaleFactor);
	}

	public static ViewportChangeListener add(ViewportChangeListener a, ViewportChangeListener b) {
		if (a == null)
			return b;

		if (b == null)
			return a;

		return new ViewportChangeListenerChain(a, b);
	}

	public static ViewportChangeListener remove(ViewportChangeListener l, ViewportChangeListener oldl) {
		if ((l == oldl) || (l == null))
			return null;
		else if (l instanceof ViewportChangeListenerChain)
			return ((ViewportChangeListenerChain) l).remove(oldl);
		else

			return l;
	}

	private ViewportChangeListener remove(ViewportChangeListener oldl) {
		if (oldl == a)
			return b;

		if (oldl == b)
			return a;

		ViewportChangeListener a2 = remove(a, oldl);
		ViewportChangeListener b2 = remove(b, oldl);

		if ((a2 == a) && (b2 == b))
			return this;

		return add(a2, b2);
	}
}
