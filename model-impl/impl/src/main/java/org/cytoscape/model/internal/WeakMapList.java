
/*
 Copyright (c) 2008, 2010-2011, The Cytoscape Consortium (www.cytoscape.org)

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
package org.cytoscape.model.internal;


import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.events.ColumnCreatedEvent;
import org.cytoscape.model.events.ColumnCreatedListener;

import java.util.List;
import java.util.ArrayList;
import java.util.WeakHashMap;

import java.lang.ref.WeakReference;

/**
 * A WeakHashMap where the keys point to lists of WeakReferences.
 */
class WeakMapList<S,T> {

	WeakHashMap<S,List<WeakReference<T>>> data = new WeakHashMap<S,List<WeakReference<T>>>();

	public List<T> get(S src) {
		List<WeakReference<T>> list = data.get(src);
		List<T> ret = new ArrayList<T>();
		if ( list == null )
			return ret; 
		for (WeakReference<T> ref : list) {
			T t = ref.get();
			if ( t != null )
				ret.add(t);
		}
		return ret;
	}


	public void put(S src, T tgt) {
		List<WeakReference<T>> refs = data.get(src);
		if ( refs == null ) {
			refs = new ArrayList<WeakReference<T>>();
			data.put(src,refs);
		}
		refs.add( new WeakReference<T>( tgt ) );
	}
}

