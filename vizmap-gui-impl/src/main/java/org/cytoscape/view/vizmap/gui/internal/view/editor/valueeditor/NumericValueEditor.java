package org.cytoscape.view.vizmap.gui.internal.view.editor.valueeditor;

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

import java.awt.Component;
import java.math.BigDecimal;
import java.math.BigInteger;

import javax.swing.JOptionPane;

public class NumericValueEditor<T extends Number> extends
		AbstractValueEditor<T> {

	private static final String MESSAGE = "Please enter new number";
	private static final String ERR_MESSAGE = "Not a valid number.";

	public NumericValueEditor(final Class<T> type) {
		super(type);
	}

	/**
	 * Generic editor for all kinds of numbers.
	 */
	@Override public <S extends T> T showEditor(final Component parent, S initialValue) {
		Object value = null;
		Number result = null;
		
		while (result == null) {
			value = JOptionPane.showInputDialog(parent, MESSAGE, initialValue);
			
			// This means cancel.
			if (value == null)
				return null;
			
			try {
				final BigDecimal number = new BigDecimal(value.toString());
				result = convert(number, type);
			} catch (NumberFormatException ne) {
				JOptionPane.showMessageDialog(editorDialog, ERR_MESSAGE, "Invalid Input.", JOptionPane.ERROR_MESSAGE);
			}
		}
		
		return type.cast(result);
	}
	
	/**
	 * Convert number to correct type.
	 * 
	 * @param value
	 * @return
	 */
	private Number convert (final BigDecimal number, Class<? extends Number> dataType) {
		if (dataType.equals(Double.class)) {
			return number.doubleValue();
		} else if(dataType.equals(Float.class)) {
			return number.floatValue();
		} else if(dataType.equals(Integer.class)) {
			return number.intValue();
		} else if(dataType.equals(Long.class)) {
			return number.longValue();
		} else if(dataType.equals(Short.class)) {
			return number.shortValue();
		} else if(dataType.equals(BigInteger.class)) {
			return number.toBigInteger();
		} else {
			return number.doubleValue();
		}
	}
}
