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

import static javax.swing.GroupLayout.DEFAULT_SIZE;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.AbstractAction;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
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
	
	private final String NO_ERROR_MSG = "<html>&nbsp;</html>";
	private final String ERROR_MSG = "<html><font color=\"red\">Invalid number!</font></html>";
	
	private Number value = null;

	public <S extends Number> NumberValueDialog(final Component parent, final VisualProperty<S> vizProp, final S initialValue) {
		super(JOptionPane.getFrameForComponent(parent), vizProp.getDisplayName(), true);
		setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		setResizable(false);
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				value = null;
				dispose();
			}
		});
		
		final ContinuousRange<S> range = (ContinuousRange<S>) vizProp.getRange();
		final JLabel titleLabel = new JLabel(String.format("Enter %s:", readableRange(range)));
		
		final JLabel errorLabel = new JLabel(NO_ERROR_MSG);
		errorLabel.setHorizontalAlignment(JLabel.CENTER);
		
		final JTextField field = new JTextField(6);
		field.setHorizontalAlignment(JTextField.RIGHT);
		
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
				errorLabel.setText(NO_ERROR_MSG);
				pack();
			}
		});

		final JPanel btnPanel = LookAndFeelUtil.createOkCancelPanel(okBtn, cancelBtn);
		LookAndFeelUtil.setDefaultOkCancelKeyStrokes(getRootPane(), okBtn.getAction(), cancelBtn.getAction());
		
		final GroupLayout layout = new GroupLayout(getContentPane());
		getContentPane().setLayout(layout);
		layout.setAutoCreateContainerGaps(false);
		layout.setAutoCreateGaps(true);
		
		final int HGAP = 20;
		final int VGAP = 10;
		
		layout.setHorizontalGroup(layout.createSequentialGroup()
				.addContainerGap(HGAP, HGAP)
				.addGroup(layout.createParallelGroup(Alignment.CENTER, true)
						.addGroup(layout.createSequentialGroup()
								.addComponent(titleLabel, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
								.addComponent(field)
						)
					.addComponent(errorLabel)
					.addComponent(btnPanel)
				)
				.addContainerGap(HGAP, HGAP)
		);
		layout.setVerticalGroup(layout.createSequentialGroup()
				.addContainerGap(VGAP, VGAP)
				.addGroup(layout.createParallelGroup(Alignment.CENTER, false)
						.addComponent(titleLabel)
						.addComponent(field)
				)
				.addComponent(errorLabel)
				.addComponent(btnPanel)
				.addContainerGap(VGAP, VGAP)
		);
		
		setLocationRelativeTo(parent);
		pack();
		setVisible(true);
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
			errorLabel.setText(ERROR_MSG);
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
