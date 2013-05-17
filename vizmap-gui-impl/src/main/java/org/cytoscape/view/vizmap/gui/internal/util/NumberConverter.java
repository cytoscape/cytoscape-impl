package org.cytoscape.view.vizmap.gui.internal.util;

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
