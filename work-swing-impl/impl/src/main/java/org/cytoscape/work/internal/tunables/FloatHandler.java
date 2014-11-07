package org.cytoscape.work.internal.tunables;

/*
 * #%L
 * Cytoscape Work Swing Impl (work-swing-impl)
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


import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.DecimalFormat;

import org.cytoscape.work.Tunable;


public class FloatHandler extends AbstractNumberHandler {

	/**
	 * Constructs the <code>GUIHandler</code> for the <code>Float</code> type
	 *
	 * It creates the Swing component for this Object (JTextField) that contains the initial value of the Float Object annotated as <code>Tunable</code>, its description, and displays it in a proper way
	 *
	 *
	 * @param field a field that has been annotated
	 * @param o object containing <code>field</code>
	 * @param t tunable annotating <code>field</code>
	 */
	public FloatHandler(Field field, Object o, Tunable t) {
		super(field, o, t);
	}

	public FloatHandler(final Method getter, final Method setter, final Object instance, final Tunable tunable) {
		super(getter, setter, instance, tunable);
	}

	public Number getFieldValue(String value) throws NumberFormatException {
		Float f = Float.valueOf(value);
		return (Number) f;
	}

	public Number getTypedValue(Number number) {
		return new Float(number.toString());
	}

}
