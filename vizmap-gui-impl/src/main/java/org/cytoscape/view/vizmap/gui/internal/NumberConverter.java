package org.cytoscape.view.vizmap.gui.internal;

public final class NumberConverter {
	
	public static final <T> T convert(final Class<T> type, final Number value) {
		T converted = null;
		if(type == Double.class) {
			Double doubleValue = value.doubleValue();
			converted = (T) doubleValue;
		} else if(type == Integer.class) {
			Integer intValue = value.intValue();
			converted = (T) intValue;
		} else if(type == Float.class) {
			Float floatValue = value.floatValue();
			converted = (T) floatValue;
		} else if(type == Byte.class) {
			Byte byteValue = value.byteValue();
			converted = (T) byteValue;
		} else if(type == Long.class){
			Long longValue = value.longValue();
			converted = (T) longValue;
		} else if(type == Short.class) {
			Short shortValue = value.shortValue();
			converted = (T) shortValue;
		} else {
			throw new IllegalStateException("Could not covert Number.");
		}
		
		return converted;
	}

}
