package org.cytoscape.ding.impl;

import java.util.ArrayList;
import java.util.List;

import org.cytoscape.ding.Bend;
import org.cytoscape.ding.Handle;

/**
 * Basic Implementation of edge bends.
 *
 */
public class BendImpl implements Bend {
	
	private static final String DELIMITER ="|";
	
	// List of Bends included in this Bend
	private final List<Handle> handles;
	
	public BendImpl() {
		this.handles = new ArrayList<Handle>();
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
		
		if(serialized.length() == 0)
			return "";
		else
			return serialized.substring(0, serialized.length()-1);
	}
	
	public static Bend parseSerializableString(String strRepresentation) {
		
		//System.out.println("Parsing String: " + strRepresentation);
		
		final Bend bend = new BendImpl();
		// Validate
		if (strRepresentation == null)
			return bend;

		final String[] parts = strRepresentation.split("\\|");
		
		for(int i=0; i<parts.length; i++) {
			final String str = parts[i];
			final Handle handle = HandleImpl.parseSerializableString(str);
			//System.out.println("Got handle: " + handle);
			if(handle != null)
				bend.insertHandleAt(i, handle);
		}

		//System.out.println("Got Bend: " + bend.toString());
		return bend;
	}
}
