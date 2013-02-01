package org.cytoscape.ding.impl;

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

class ContentChangeListenerChain implements ContentChangeListener {
	private final ContentChangeListener a;
	private final ContentChangeListener b;

	private ContentChangeListenerChain(ContentChangeListener a, ContentChangeListener b) {
		this.a = a;
		this.b = b;
	}

	/**
	 * DOCUMENT ME!
	 */
	public void contentChanged() {
		a.contentChanged();
		b.contentChanged();
	}

	static ContentChangeListener add(ContentChangeListener a, ContentChangeListener b) {
		if (a == null)
			return b;

		if (b == null)
			return a;

		return new ContentChangeListenerChain(a, b);
	}

	static ContentChangeListener remove(ContentChangeListener l, ContentChangeListener oldl) {
		if ((l == oldl) || (l == null))
			return null;
		else if (l instanceof ContentChangeListenerChain)
			return ((ContentChangeListenerChain) l).remove(oldl);
		else

			return l;
	}

	private ContentChangeListener remove(ContentChangeListener oldl) {
		if (oldl == a)
			return b;

		if (oldl == b)
			return a;

		ContentChangeListener a2 = remove(a, oldl);
		ContentChangeListener b2 = remove(b, oldl);

		if ((a2 == a) && (b2 == b))
			return this;

		return add(a2, b2);
	}
}
