package org.cytoscape.tableimport.internal.reader;

/*
 * #%L
 * Cytoscape Table Import Impl (table-import-impl)
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

import static org.cytoscape.tableimport.internal.util.AttributeDataType.TYPE_BOOLEAN_LIST;
import static org.cytoscape.tableimport.internal.util.AttributeDataType.TYPE_FLOATING_LIST;
import static org.cytoscape.tableimport.internal.util.AttributeDataType.TYPE_INTEGER_LIST;
import static org.cytoscape.tableimport.internal.util.AttributeDataType.TYPE_LONG_LIST;

import java.util.ArrayList;
import java.util.List;

import org.cytoscape.equations.Equation;
import org.cytoscape.equations.EquationCompiler;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.tableimport.internal.util.AttributeDataType;

public abstract class AbstractLineParser {

	protected CyServiceRegistrar serviceRegistrar;
	private EquationCompiler compiler;

	protected AbstractLineParser(final CyServiceRegistrar serviceRegistrar) {
		this.serviceRegistrar = serviceRegistrar;
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Object parse(final String s, final AttributeDataType type, final String delimiter) {
		Object value = null;
		
		if (s != null && !s.isEmpty() && !"null".equals(s)) {
			try {
				switch (type) {
					case TYPE_BOOLEAN:  return Boolean.valueOf(s.trim());
					case TYPE_INTEGER:  return Integer.valueOf(s.trim());
					case TYPE_LONG:     return Long.valueOf(s.trim());
					case TYPE_FLOATING: return Double.valueOf(s.trim());
					case TYPE_STRING:   return s.trim();
	
					case TYPE_BOOLEAN_LIST:
					case TYPE_INTEGER_LIST:
					case TYPE_LONG_LIST:
					case TYPE_FLOATING_LIST:
					case TYPE_STRING_LIST:
						value = parseList(s, type, delimiter);
						
						if (value instanceof List)
							value = new ArrayList<>((List)value);
						
						break;
				}
			} catch (NumberFormatException e) {
				value = createInvalidNumberEquation(s.trim(), type);
			}
		}
		
		return value;
	}
	
	private Object parseList(final String s, final AttributeDataType type, String delimiter) {
		if (s == null)
			return null;

		final List<Object> list = new ArrayList<>();
		final String[] parts = (s.replace("\"", "")).split(delimiter);

		for (String listItem : parts) {
			try {
				if (type == TYPE_BOOLEAN_LIST)
					list.add(Boolean.valueOf(listItem.trim()));
				else if (type == TYPE_INTEGER_LIST)
					list.add(Integer.valueOf(listItem.trim()));
				else if (type == TYPE_LONG_LIST)
					list.add(Long.valueOf(listItem.trim()));
				else if (type == TYPE_FLOATING_LIST)
					list.add(new Double(listItem.trim()));
				else // TYPE_STRING or unknown
					list.add(listItem.trim());				
			} catch (NumberFormatException e) {
				return createInvalidListEquation(s, listItem.trim(), type);
			}
		}

		return list;
	}
	
	private Equation createInvalidNumberEquation(final String value, final AttributeDataType type) {
		final String text = "=\"" + value + "\"";
		final String msg = "Invalid value: " + value;
		
		return getEquationCompiler().getErrorEquation(text, type.getType(), msg);
	}
	
	private Equation createInvalidListEquation(final String list, final String listItem,
			final AttributeDataType type) {
		final String text = "=\"" + list + "\"";
		final String msg = "Invalid list item: " + listItem;
		
		return getEquationCompiler().getErrorEquation(text, type.getType(), msg);
	}
	
	private EquationCompiler getEquationCompiler() {
		if (compiler == null)
			compiler = serviceRegistrar.getService(EquationCompiler.class);
		
		return compiler;
	}
}
