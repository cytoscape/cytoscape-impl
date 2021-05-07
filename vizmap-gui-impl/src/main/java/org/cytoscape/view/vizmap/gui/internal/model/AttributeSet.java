package org.cytoscape.view.vizmap.gui.internal.model;

/*
 * #%L
 * Cytoscape VizMap GUI Impl (vizmap-gui-impl)
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

import java.util.HashMap;
import java.util.Map;

public final class AttributeSet {
	
	// Node, Edge, or Network.
	private final Class<?> targetObjectType;
	private final Map<String, Class<?>> attrNameTypeMap;

	public AttributeSet(Class<?> targetObjectType) {
		this.targetObjectType = targetObjectType;
		this.attrNameTypeMap = new HashMap<>();
	}
	
	public AttributeSet(AttributeSet clone) {
		this(clone.getObjectType());
		attrNameTypeMap.putAll(clone.getAttrMap());
	}
	
	/**
	 * Graph object type
	 */
	public Class<?> getObjectType() {
		return targetObjectType;
	}
	
	/**
	 * Map from column name to column data type
	 */
	public Map<String,Class<?>> getAttrMap() {
		return attrNameTypeMap;
	}
}
