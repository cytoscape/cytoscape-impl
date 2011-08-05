package org.cytoscape.view.vizmap.gui.internal.editor.valueeditor;

import java.awt.Component;
import java.math.BigDecimal;
import java.math.BigInteger;

import javax.swing.JOptionPane;

public class NumericValueEditor extends
		AbstractValueEditor<Number> {

	private static final String MESSAGE = "Please enter new number";
	private static final String ERR_MESSAGE = "Not a valid number.";

	public NumericValueEditor(final Class<Number> type) {
		super(type);
	}

	
	/**
	 * Generic editor for all kinds of numbers.
	 */
	@Override public <S extends Number> Number showEditor(Component parent, S initialValue) {
		if(initialValue == null)
			throw new NullPointerException("Initial value is null.");
		
		Object value = null;
		Number result = null;
		while(result == null) {
			value = JOptionPane.showInputDialog(parent, MESSAGE, initialValue);
			
			// This means cancel.
			if(value == null)
				return null;
			
			
			BigDecimal number;
			try {
				number = new BigDecimal(value.toString());
				result = convert(number, initialValue.getClass());
			} catch (NumberFormatException ne) {
				JOptionPane.showMessageDialog(editorDialog, ERR_MESSAGE, "Invalid Input!", JOptionPane.ERROR_MESSAGE);
			}
		}
		
		return result;
	}
	
	/**
	 * Convert number to correct type.
	 * 
	 * @param value
	 * @return
	 */
	private Number convert (final BigDecimal number, Class<? extends Number> dataType) {
		
		// Check number type.
		if(dataType.equals(Double.class)) {
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
