package org.cytoscape.view.vizmap.gui.internal;

import org.cytoscape.view.vizmap.gui.internal.event.CellType;

import com.l2fprod.common.propertysheet.DefaultProperty;

/**
 * Extended version of DefaultProperty which accepts one more value as hidden
 * object.
 * 
 * Refactored for 3 to keep more information.
 * 
 */
public class VizMapperProperty<K, V, T> extends DefaultProperty {
	
	private final static long serialVersionUID = 1202339868680341L;
	
	private final CellType cellType;
	private final K key;
	
	private T internalValue;
	

	public <S extends V> VizMapperProperty(final CellType cellType, final K key, final Class<S> valueType) {
		super();
		
		if(key == null)
			throw new NullPointerException("Key cannot be null.");
		if(cellType == null)
			throw new NullPointerException("CellType cannot be null.");	
		if(valueType == null)
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
		if(original == null)
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
	@Override public void setName(String name) {
		throw new UnsupportedOperationException("Name is immutable in this implementation.");
	}
	
	@Override public String getName() {
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
