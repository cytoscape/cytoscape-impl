package org.cytoscape.view.vizmap.internal.mappings;

/*
 * #%L
 * Cytoscape VizMap Impl (vizmap-impl)
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

import org.cytoscape.view.vizmap.mappings.ValueTranslator;

//TODO: DELETE?
public class NumberTranslator<T extends Number> implements ValueTranslator<Object, T> {

	private final Class<T> translatedValueType;
	
	public NumberTranslator(final Class<T> translatedValueType) {
		this.translatedValueType = translatedValueType;
	}
	
	@Override
	public T translate(Object inputValue) {
		if(inputValue instanceof Number) {
			return (T) inputValue;
		} else {
			return null;
		}
	}

	@Override
	public Class<T> getTranslatedValueType() {
		return translatedValueType;
	}

}
