
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

package org.cytoscape.ding.impl.events;

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
