package org.cytoscape.model.internal;

/*
 * #%L
 * Cytoscape Model Impl (model-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2008 - 2013 The Cytoscape Consortium
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

