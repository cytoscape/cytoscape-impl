package org.cytoscape.view.vizmap.gui.internal;

/*
 * #%L
 * Cytoscape VizMap GUI Impl (vizmap-gui-impl)
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

import org.cytoscape.view.vizmap.gui.internal.event.CellType;

import com.l2fprod.common.propertysheet.DefaultProperty;

/**
 * Extended version of DefaultProperty which accepts one more value as hidden object.
 * 
 * Refactored for 3 to keep more information.
 */
public class VizMapperProperty<K, V, T> extends DefaultProperty {
	
	private final static long serialVersionUID = 1202339868680341L;
	
	private final CellType cellType;
	private final K key;
	
	private T internalValue;
	

	public <S extends V> VizMapperProperty(final CellType cellType, final K key, final Class<S> valueType) {
		if (key == null)
			throw new NullPointerException("Key cannot be null.");
		if (cellType == null)
			throw new NullPointerException("CellType cannot be null.");	
		if (valueType == null)
			throw new NullPointerException("Value Type cannot be null.");
		
		this.cellType = cellType;
		this.key = key;
		
		// In fact, this should be a unique ID of this property.
		super.setName(key.toString());
		super.setType(valueType);
	}
	
	/**
	 * Copy constructor
	 * @param original
	 */
	public VizMapperProperty(final VizMapperProperty<K, V, T> original) {
		super();
		
		if (original == null)
			throw new NullPointerException("Original value cannot be null.");
		
		this.cellType = original.getCellType();
		this.key = original.getKey();
		
		super.setName(key.toString());
		super.setType(original.getType());
		
		super.setValue(original.getValue());
		this.internalValue = original.getInternalValue();
	}
	
	/**
	 * Make name immutable.
	 */
	@Override
	public void setName(String name) {
		throw new UnsupportedOperationException("Name is immutable in this implementation.");
	}
	
	@Override
	public String getName() {
		return key.toString();
	}
	
	public void setInternalValue(final T internalValue) {
		this.internalValue = internalValue;
	}
	
	public CellType getCellType() {
		return this.cellType;
	}
	
	public T getInternalValue() {
		return this.internalValue;
	}
	
	public K getKey() {
		return key;
	}
}
