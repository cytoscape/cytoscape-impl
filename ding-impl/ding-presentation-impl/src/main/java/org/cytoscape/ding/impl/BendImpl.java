package org.cytoscape.ding.impl;

/*
 * #%L
 * Cytoscape Ding View/Presentation Impl (ding-presentation-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2021 The Cytoscape Consortium
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

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.cytoscape.view.presentation.property.values.Bend;
import org.cytoscape.view.presentation.property.values.Handle;

/**
 * Basic Implementation of edge bends.
 *
 */
public class BendImpl implements Bend {
	
	private static final String DELIMITER ="|";
	
	// List of Handles included in this Bend
	private final List<Handle> handles = new CopyOnWriteArrayList<>();
	
	public BendImpl() {
	}

	public BendImpl(BendImpl bend) {
		if(bend != null) {
			for(Handle h : bend.handles) {
				HandleImpl handle = (HandleImpl)h;
				handles.add( new HandleImpl(handle) );
			}
		}
	}

	@Override
	public List<Handle> getAllHandles() {
		return handles;
	}


	@Override
	public void removeHandleAt(final int handleIndex) {
		if(handleIndex > handles.size())
			throw new IllegalArgumentException("handleIndex is out of range: " + handleIndex);
		
		this.handles.remove(handleIndex);
	}


	@Override
	public void removeAllHandles() {
		handles.clear();
	}

	@Override
	public int getIndex(final Handle handle) {
		return handles.indexOf(handle);
	}

	@Override
	public void insertHandleAt(final int index, final Handle handle) {
		this.handles.add(index, handle);
	}
	
	@Override
	public String toString() {
		return "Handles[ " + handles.size() + " ]" ;
	}

	@Override
	public String getSerializableString() {
		final StringBuilder builder = new StringBuilder();
		for(Handle handle:handles)
			builder.append(handle.getSerializableString() + DELIMITER);
		final String serialized = builder.toString();
		
		//System.out.println("Serialized String: " + serialized);
		
		if (serialized.length() == 0)			return "";
		return serialized.substring(0, serialized.length()-1);
	}
	
	public static Bend parseSerializableString(String strRepresentation) {
		final Bend bend = new BendImpl();
		// Validate
		if (strRepresentation == null)
			return bend;

		final String[] parts = strRepresentation.split("\\|");
		
		for(int i=0; i<parts.length; i++) {
			final String str = parts[i];
			final Handle handle = HandleImpl.parseSerializableString(str);
			if(handle != null)
				bend.insertHandleAt(i, handle);
		}
		return bend;
	}

}
