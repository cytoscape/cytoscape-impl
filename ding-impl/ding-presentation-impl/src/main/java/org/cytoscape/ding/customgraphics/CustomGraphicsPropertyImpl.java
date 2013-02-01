package org.cytoscape.ding.customgraphics;

/*
 * #%L
 * Cytoscape Ding View/Presentation Impl (ding-presentation-impl)
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

import org.cytoscape.ding.customgraphics.vector.CustomGraphicsProperty;

public class CustomGraphicsPropertyImpl<T> implements CustomGraphicsProperty<T> {
	
	private T value;
	private final T defaultValue;
	
	public CustomGraphicsPropertyImpl(final T defaultValue) {
		this.defaultValue = defaultValue;
		this.value = defaultValue;
	}
	
	public T getDefaultValue() {
		return defaultValue;
	}

	public T getValue() {
		return value;
	}

	public void setValue(Object value) {
		if(this.value.getClass().isAssignableFrom(value.getClass()) == false)
			throw new IllegalArgumentException("The value type is not compatible.");
		else
			this.value = (T) value;
	}

}
