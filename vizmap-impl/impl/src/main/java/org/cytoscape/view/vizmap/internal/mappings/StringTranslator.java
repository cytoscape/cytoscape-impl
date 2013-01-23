package org.cytoscape.view.vizmap.internal.mappings;

/*
 * #%L
 * Cytoscape VizMap Impl (vizmap-impl)
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

import java.util.List;

import org.cytoscape.view.vizmap.mappings.ValueTranslator;

public class StringTranslator implements ValueTranslator<Object, String>{

	@Override
	public String translate(final Object inputValue) {
		if (inputValue != null) {
			if (inputValue instanceof List) {
				// Special handler for List column.
				final List<?> list = (List<?>)inputValue;
				final StringBuffer sb = new StringBuffer();

				if (list != null && !list.isEmpty()) {
					for (Object item : list)
						sb.append(item.toString() + "\n");

					sb.deleteCharAt(sb.length() - 1);
				}
				
				return sb.toString();
			} else {
				return inputValue.toString();
			}
		} else {
			return null;
		}
	}

	@Override
	public Class<String> getTranslatedValueType() {
		return String.class;
	}
}
