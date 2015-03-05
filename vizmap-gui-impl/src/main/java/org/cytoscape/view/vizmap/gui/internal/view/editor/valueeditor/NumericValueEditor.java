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
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.cytoscape.util.swing.LookAndFeelUtil;
import org.cytoscape.view.model.ContinuousRange;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.vizmap.gui.editor.VisualPropertyValueEditor;

public class NumericValueEditor<V extends Number> implements VisualPropertyValueEditor<V> {

	final Class<V> type;

	public NumericValueEditor(final Class<V> type) {
		this.type = type;
	}

	@Override
	public Class<V> getValueType() {
		return type;
	}

	/**
	 * Generic editor for all kinds of numbers.
	 */
	@Override
	public <S extends V> V showEditor(final Component parent, S initialValue, VisualProperty<S> vizProp) {
		final NumberValueDialog d = new NumberValueDialog(parent, vizProp, initialValue);
		if (d.getValue() == null)
			return null;
		else
			return vizProp.getRange().getType().cast(d.getValue());
	}
}

@SuppressWarnings("serial")
class NumberValueDialog extends JDialog {
	
	private Number value = null;

	public <S extends Number> NumberValueDialog(final Component parent, final VisualProperty<S> vizProp, final S initialValue) {
		super(JOptionPane.getFrameForComponent(parent), vizProp.getDisplayName(), true);
		super.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		super.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				value = null;
				dispose();
			}
		});
		
		super.setLayout(new GridBagLayout());
		
		final GridBagConstraints c = new GridBagConstraints();

		final ContinuousRange<S> range = (ContinuousRange<S>) vizProp.getRange();
		final JLabel titleLabel = new JLabel(String.format("Enter %s:", readableRange(range)));
		final JLabel errorLabel = new JLabel("<html><font color=\"red\">Not valid</font></html>");
		errorLabel.setVisible(false);
		final JTextField field = new JTextField(6);
		
		if (initialValue != null)
			field.setText(initialValue.toString());
		
		final JButton okBtn = new JButton(new AbstractAction("OK") {
			@Override
			public void actionPerformed(ActionEvent e) {
				onOkAction(range, errorLabel, field);
			}
		});
		final JButton cancelBtn = new JButton(new AbstractAction("Cancel") {
			@Override
			public void actionPerformed(ActionEvent e) {
				value = null;
				dispose();
			}
		});
		
		field.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				onOkAction(range, errorLabel, field);
			}
		});
		field.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void changedUpdate(DocumentEvent e) { clear(); }
			@Override
			public void removeUpdate(DocumentEvent e) { clear(); }
			@Override
			public void insertUpdate(DocumentEvent e) { clear(); }

			void clear() {
				errorLabel.setVisible(false);
				pack();
			}
		});

		c.gridx = 0;				c.gridy = 0;
		c.gridwidth = 2;		c.gridheight = 1;
		c.weightx = 0.0;		c.weighty = 0.0;
		c.anchor = GridBagConstraints.WEST;
		c.fill = GridBagConstraints.NONE;
		c.insets = new Insets(10, 10, 5, 10);
		super.add(titleLabel, c);

		c.gridx = 0;				c.gridy = 1;
		c.gridwidth = 1;		c.gridheight = 1;
		c.insets = new Insets(0, 30, 20, 10);
		super.add(field, c);

		c.gridx = 1;				c.gridy = 1;
		c.insets = new Insets(0, 0, 20, 10);
		super.add(errorLabel, c);

		c.gridx = 0;				c.gridy = 2;
		c.gridwidth = 2;		c.gridheight = 1;
		c.weightx = 1.0;		c.weighty = 0.0;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.insets = new Insets(0, 10, 10, 10);
		
		final JPanel btnPanel = LookAndFeelUtil.createOkCancelPanel(okBtn, cancelBtn);
		super.add(btnPanel, c);

		LookAndFeelUtil.setDefaultOkCancelKeyStrokes(getRootPane(), okBtn.getAction(), cancelBtn.getAction());
		
		super.setLocationRelativeTo(parent);
		super.pack();
		super.setVisible(true);
	}

	public Number getValue() {
		return value;
	}

	private <S extends Number> void onOkAction(final ContinuousRange<S> range, final JLabel errorLabel,
			final JTextField field) {
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
		final Class<?> type = range.getType();
		final boolean isWholeNumber = (type.equals(Integer.class) || type.equals(Long.class));
		final String numStr = String.format("a %snumber", isWholeNumber ? "whole " : "");
		final S left  = range.getMin();
		final S right = range.getMax();
		final boolean includeLeft  = range.includeMin();
		final boolean includeRight = range.includeMax();
		final boolean leftBounded  = !isUnbounded(left, range.getType());
		final boolean rightBounded = !isUnbounded(right, range.getType());

		if (!leftBounded && !rightBounded) {
			return numStr;
		} else if (leftBounded && !rightBounded) {
			if (includeLeft) {
				return String.format("%s that is %s or greater", numStr, left);
			} else {
				return String.format("%s greater than %s", numStr, left);
			}
		} else if (!leftBounded && rightBounded) {
			if (includeRight) {
				return String.format("%s that is %s or less", numStr, right);
			} else {
				return String.format("%s that is less than %s", numStr, right);
			}
		} else {
			if (includeLeft && includeRight) {
				return String.format("%s between %s and %s", numStr, left, right);
			} else {
				return String.format("%s that is greater than%s %s and less than%s %s", numStr, (includeLeft ? " or equal to": ""), left, (includeRight ? " or equal to": ""), right);
			}
		}
	}

	private static Number parseNumber(final String s, final Class<?> type) {
		try {
			final Double d = new Double(s);
			if (type.equals(Integer.class)) {
				return d.intValue();
			} else if (type.equals(Long.class)) {
				return d.longValue();
			} else if (type.equals(Float.class)) {
				return d.floatValue();
			} else if (type.equals(Double.class)) {
				return d;
			} else {
				throw new IllegalArgumentException("unknown type: " + type);
			}
		} catch (NumberFormatException e) {
			return null;
		}
	}
}
