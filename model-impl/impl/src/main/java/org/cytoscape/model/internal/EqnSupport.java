package org.cytoscape.model.internal;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.cytoscape.equations.Equation;
import org.cytoscape.equations.IdentDescriptor;
import org.cytoscape.equations.Interpreter;

/*
 * #%L
 * Cytoscape Model Impl (model-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2010 - 2019 The Cytoscape Consortium
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

class EqnSupport {
	
	private EqnSupport() { } // Don't ever create an instance of this class!

	static boolean scalarEquationIsCompatible(final Object equationCandidate, final Class<?> targetType) {
		if (!(equationCandidate instanceof Equation))
			return false;

		final Equation equation = (Equation) equationCandidate;
		final Class<?> eqType = equation.getType();

		if (targetType == String.class)
			return true; // Everything can be turned into a String!
		if (targetType == Boolean.class || Number.class.isAssignableFrom(targetType))
			return eqType == Boolean.class || Number.class.isAssignableFrom(eqType);
		
		return false;
	}

	static boolean listEquationIsCompatible(final Equation equation, final Class<?> listElementType) {
		// TODO: We no longer support strongly typed lists so this always
		// returns true.  If we ever re-introduce strongly typed lists, we
		// should modify this to do the appropriate checks.
		return true;
	}

	static Object convertEqnResultToColumnType(final Class<?> columnType, final Object result) {
		final Class<?> resultType = result.getClass();
		if (resultType == columnType)
			return result;
		
		// the equation bundle subclasses ArrayList for its List return values
		if (columnType == List.class && List.class.isAssignableFrom(resultType))
			return result;

		if (columnType == String.class)
			return result.toString();

		if (Number.class.isAssignableFrom(resultType)) {
			if (columnType == Number.class || columnType == Double.class)
				return ((Number)result).doubleValue();
			
			if (columnType == Long.class)
				return ((Number)result).longValue();
			
			if (columnType == Integer.class)
				return ((Number)result).intValue();
		}
		
		if (columnType == Boolean.class && resultType == Long.class)
			return (Long)result == 0 ? Boolean.FALSE : Boolean.TRUE;

		if (columnType == Boolean.class && resultType == Double.class)
			return (Double)result == 0.0 ? Boolean.FALSE : Boolean.TRUE;

		throw new IllegalArgumentException("unexpected equation result type " + resultType
						   + " for a column of type " + columnType + ".");
	}

	static Object evalEquation(final Equation equation, final Object key,
				   final Interpreter interpreter,
				   final Set<String> currentlyActiveAttributes,
				   final String columnName, final Appendable lastInternalError,
				   final CyTableImpl tableImpl)
	{
		if (currentlyActiveAttributes.contains(columnName)) {
			currentlyActiveAttributes.clear();
			try {
				lastInternalError.append("Recursive equation evaluation of \"" + columnName + "\".");
			} catch (Exception e) {
				// Intentionally empty!
			}
			return null;
		} else
			currentlyActiveAttributes.add(columnName);

		final Collection<String> attribReferences = equation.getVariableReferences();
		final Map<String, Object> defaultValues = equation.getDefaultVariableValues();

		final Map<String, IdentDescriptor> nameToDescriptorMap = new TreeMap<>();
		for (final String attribRef : attribReferences) {
			if (attribRef.equals("ID")) {
				nameToDescriptorMap.put("ID", new IdentDescriptor(key));
				continue;
			}

			Object attribValue = tableImpl.getValue(key, attribRef);
			if (attribValue == null) {
				final Object defaultValue = defaultValues.get(attribRef);
				if (defaultValue != null)
					attribValue = defaultValue;
				else {
					currentlyActiveAttributes.clear();
					try {
						lastInternalError.append("Missing value for referenced column \"" + attribRef + "\".");
					} catch (Exception e) {
						// Intentionally empty!
					}
					return null;
				}
			}

			try {
				nameToDescriptorMap.put(attribRef, new IdentDescriptor(attribValue));
			} catch (final Exception e) {
				currentlyActiveAttributes.clear();
				try {
					lastInternalError.append("Bad column reference to \"" + attribRef + "\".");
				} catch (Exception e2) {
					// Intentionally empty!
				}
				return null;
			}
		}

		try {
			final Object result = interpreter.execute(equation, nameToDescriptorMap);
			currentlyActiveAttributes.remove(columnName);
			return result;
		} catch (final Exception e) {
			currentlyActiveAttributes.clear();
			try {
				lastInternalError.append(e.getMessage());
			} catch (Exception e2) {
				// Intentionally empty!
			}
			return null;
		}
	}

}
