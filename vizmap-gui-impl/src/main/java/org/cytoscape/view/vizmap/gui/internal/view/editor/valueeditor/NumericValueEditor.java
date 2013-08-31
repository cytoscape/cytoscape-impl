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
import javax.swing.JPanel;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JButton;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.JTextField;

import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.FlowLayout;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.DocumentEvent;

import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.model.Range;
import org.cytoscape.view.model.ContinuousRange;
import org.cytoscape.view.vizmap.gui.editor.VisualPropertyValueEditor;

public class NumericValueEditor<V extends Number> implements VisualPropertyValueEditor<V> {

	private static final String MESSAGE = "Please enter new number";
	private static final String ERR_MESSAGE = "Not a valid number.";

	final Class<V> type;

	public NumericValueEditor(final Class<V> type) {
		this.type = type;
	}

	public Class<V> getValueType() {
		return type;
	}

	/**
	 * Generic editor for all kinds of numbers.
	 */
	@Override public <S extends V> V showEditor(final Component parent, S initialValue, VisualProperty<S> vizProp) {
		/*
		Object value = null;
		Number result = null;

		System.out.println("Launching ProperBoundedIntervalDialog");
		NumberValueDialog d = new NumberValueDialog(parent, vizProp);
		System.out.println("Done ProperBoundedIntervalDialog");
		System.out.println("VisualPropertyValueEditor: " + vizProp.getDisplayName());
		
		while (result == null) {
			value = JOptionPane.showInputDialog(parent, MESSAGE, initialValue);
			
			// This means cancel.
			if (value == null)
				return null;
			
			try {
				final BigDecimal number = new BigDecimal(value.toString());
				result = convert(number, type);
				final V result2 = type.cast(result);
				final Range<S> range = vizProp.getRange();
				if (!range.inRange((S) result2)) {
					throw new NumberFormatException();
				}
			} catch (NumberFormatException ne) {
				JOptionPane.showMessageDialog(parent, ERR_MESSAGE, "Invalid Input.", JOptionPane.ERROR_MESSAGE);
			}
		}
		
		return type.cast(result);
		*/

		final NumberValueDialog d = new NumberValueDialog(parent, vizProp, initialValue);
		if (d.getValue() == null)
			return null;
		else
			return vizProp.getRange().getType().cast(d.getValue());
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

class NumberValueDialog extends JDialog {
	private Number value = null;

	public <S extends Number> NumberValueDialog(final Component parent, final VisualProperty<S> vizProp, final S initialValue) {
		super(JOptionPane.getFrameForComponent(parent), vizProp.getDisplayName(), true);
		super.setLayout(new GridBagLayout());
		final GridBagConstraints c = new GridBagConstraints();

		final ContinuousRange<S> range = (ContinuousRange<S>) vizProp.getRange();
		final JLabel titleLabel = new JLabel(String.format("Enter %s:", readableRange(range)));
		final JLabel errorLabel = new JLabel("<html><font color=\"red\" size=\"-1\">Not a valid number</font></html>");
		errorLabel.setVisible(false);
		final JTextField field = new JTextField(6);
		if (initialValue != null)
			field.setText(initialValue.toString());
		final JButton okBtn = new JButton("  OK  ");
		final JButton cancelBtn = new JButton("Cancel");
		cancelBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				value = null;
				dispose();
			}
		});
		final ActionListener okAction = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				value = parseNumber(field.getText(), range.getType());
				if (value != null) {
					if (!range.inRange(range.getType().cast(value)))
						value = null;
				}
				if (value == null) {
					errorLabel.setVisible(true);
					pack();
				} else {
					dispose();
				}
			}
		};
		okBtn.addActionListener(okAction);
		field.addActionListener(okAction);
		field.getDocument().addDocumentListener(new DocumentListener() {
			public void changedUpdate(DocumentEvent e) { clear(); }
			public void removeUpdate(DocumentEvent e) { clear(); }
			public void insertUpdate(DocumentEvent e) { clear(); }

			void clear() {
				errorLabel.setVisible(false);
				pack();
			}
		});

		c.gridx = 0;				c.gridy = 0;
		c.gridwidth = 1;		c.gridheight = 1;
		c.weightx = 0.0;		c.weighty = 0.0;
		c.fill = GridBagConstraints.NONE;
		c.insets = new Insets(10, 10, 5, 5);
		super.add(titleLabel, c);

		c.gridx = 1;				c.gridy = 0;
		c.anchor = GridBagConstraints.WEST;
		c.insets = new Insets(10, 0, 5, 10);
		super.add(field, c);

		c.gridx = 1;				c.gridy = 1;
		c.insets = new Insets(0, 0, 10, 10);
		super.add(errorLabel, c);

		c.gridx = 0;				c.gridy = 2;
		c.gridwidth = 2;		c.gridheight = 1;
		c.weightx = 1.0;		c.weighty = 0.0;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.insets = new Insets(0, 10, 10, 10);
		final JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		btnPanel.add(cancelBtn);
		btnPanel.add(okBtn);
		super.add(btnPanel, c);

		super.pack();
		super.setVisible(true);
		super.setLocationRelativeTo(parent);
	}

	public Number getValue() {
		return value;
	}

	private static boolean isUnbounded(final Number value, final Class<?> type) {
		if (type.equals(Integer.class)) {
			return (value.equals(Integer.MIN_VALUE) || value.equals(Integer.MAX_VALUE));
		} else if (type.equals(Long.class)) {
			return (value.equals(Long.MIN_VALUE) || value.equals(Long.MAX_VALUE));
		} else if (type.equals(Float.class)) {
			return ((Float) value).isInfinite();
		} else if (type.equals(Double.class)) {
			return ((Double) value).isInfinite();
		} else {
			throw new IllegalArgumentException("Unknown number type: " + type);
		}
	}

	private static <S extends Number> String readableRange(ContinuousRange<S> range) {
		final S left  = range.getMin();
		final S right = range.getMax();
		final boolean includeLeft  = range.includeMin();
		final boolean includeRight = range.includeMax();
		final boolean leftBounded  = !isUnbounded(left, range.getType());
		final boolean rightBounded = !isUnbounded(right, range.getType());

		if (!leftBounded && !rightBounded) {
			return "a number";
		} else if (leftBounded && !rightBounded) {
			if (includeLeft) {
				return String.format("a number that is %s or greater", left);
			} else {
				return String.format("a number greater than %s", left);
			}
		} else if (!leftBounded && rightBounded) {
			if (includeRight) {
				return String.format("a number that is %s or less", right);
			} else {
				return String.format("a number that is less than %s", right);
			}
		} else {
			if (includeLeft && includeRight) {
				return String.format("a number between %s and %s", left, right);
			} else {
				return String.format("a number that is greater than%s %s and less than%s %s", (includeLeft ? " or equal to": ""), left, (includeRight ? " or equal to": ""), right);
			}
		}
	}

	private static Number parseNumber(final String s, final Class<?> type) {
		try {
			if (type.equals(Integer.class)) {
				return Integer.valueOf(s);
			} else if (type.equals(Long.class)) {
				return Long.valueOf(s);
			} else if (type.equals(Float.class)) {
				return Float.valueOf(s);
			} else if (type.equals(Double.class)) {
				return Double.valueOf(s);
			} else {
				throw new IllegalArgumentException("unknown type: " + type);
			}
		} catch (NumberFormatException e) {
			return null;
		}
	}
}
