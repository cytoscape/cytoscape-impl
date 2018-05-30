package org.cytoscape.internal.model;

/*
 * #%L
 * Cytoscape Swing Application Impl (swing-application-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2018 The Cytoscape Consortium
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

public enum SelectionMode {
	NODE_SELECTION("Nodes", "NETWORK_NODE_SELECTION"),
	EDGE_SELECTION("Edges", "NETWORK_EDGE_SELECTION"),
	ANNOTATION_SELECTION("Annotations", "NETWORK_ANNOTATION_SELECTION");
	
	private final String text;
	private final String propertyId;

	private SelectionMode(String text, String propertyId) {
		this.text = text;
		this.propertyId = propertyId;
	}
	
	public String getText() {
		return text;
	}
	
	public String getPropertyId() {
		return propertyId;
	}
}
