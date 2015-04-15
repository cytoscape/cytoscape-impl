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


import static org.cytoscape.work.internal.tunables.utils.GUIDefaults.updateFieldPanel;
import static org.cytoscape.work.internal.tunables.utils.GUIDefaults.setTooltip;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.DecimalFormat;
import java.text.ParsePosition;

import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.UIManager;

import org.cytoscape.work.Tunable;
import org.cytoscape.work.internal.tunables.utils.GUIDefaults;
import org.cytoscape.work.swing.AbstractGUITunableHandler;


/**
 * Base class for numeric Tunable handlers
 *
 * @author pasteur
 */
public abstract class AbstractNumberHandler extends AbstractGUITunableHandler implements ActionListener {
	
	private JFormattedTextField textField;
	private DecimalFormat format;

	/**
	 * It creates the Swing component for this Object (JTextField) that contains the initial value
	 * of the Double Object annotated as <code>Tunable</code>, its description, and displays it in a proper way.
	 *
	 * @param f field that has been annotated
	 * @param o object contained in <code>f</code>
	 * @param t tunable associated to <code>f</code>
	 */
	public AbstractNumberHandler(Field f, Object o, Tunable t) {
		super(f,o,t);
		init();
	}

	public AbstractNumberHandler(final Method getter, final Method setter, final Object instance, final Tunable tunable) {
		super(getter, setter, instance, tunable);
		init();
	}

	private void init() {
		format = null;
		
		if (getFormat() != null && getFormat().length() > 0) {
			format = new DecimalFormat(getFormat());
		}

		Number d = null;
		try {
			d = (Number)getNumberValue();
		} catch(final Exception e) {
			e.printStackTrace();
			d = Double.valueOf(0.0);
		}

		// Figure out how to handle the format for this particular value
		if (format == null) {
			double dx = d.doubleValue();
			if (dx > 1000000.0 || dx < 0.001)
				format = new DecimalFormat("0.#####E0");
			else
				format = new DecimalFormat();
		}

		// Set Gui
		textField = new JFormattedTextField(format.format(d));
		textField.setPreferredSize(new Dimension(GUIDefaults.TEXT_BOX_WIDTH, textField.getPreferredSize().height));
		textField.setHorizontalAlignment(JTextField.RIGHT);
		textField.addActionListener(this);
		
		final JLabel label = new JLabel(getDescription());
		
		updateFieldPanel(panel, label, textField, horizontal);
		setTooltip(getTooltip(), label, textField);
	}

	@Override
	public void update(){
		Number d;
		try {
			d = (Number)getNumberValue();
			textField.setText(format.format(d));
		} catch(final Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Catches the value inserted in the JTextField, parses it to a <code>Double</code> value,
	 * and tries to set it to the initial object.
	 * If it can't, throws an exception that displays the source error to the user
	 */
	@Override
	public void handle() {
		textField.setBackground(UIManager.getColor("TextField.background"));
		Number prev = getNumberValue();
		Number d = null;
		
		try {
			d = getFieldValue(textField.getText());
		} catch(NumberFormatException nfe) {
			// Got a format exception -- try parsing it according to the format
			d = format.parse(textField.getText(), new ParsePosition(0));
			
			if (d == null) {
				displayError(prev);
				return;
			}
		}

		// Make sure we got a reasonable value by attempting to set the value
		try {
			setValue(getTypedValue(d));
		} catch (final Exception e) {
			displayError(prev);
		}
	}

	abstract public Number getFieldValue(String text);

	// We need this because format.parse will not necessarily gives us the right
	// type back
	abstract public Number getTypedValue(Number number);

	/**
	 * To get the item that is currently selected
	 */
	@Override
	public String getState() {
		if ( textField == null )
			return "";

		final String text = textField.getText();
		if ( text == null )
			return "";

		return text;
	}

	/**
	 *  Action listener event handler.
	 *
	 *  @param ae specifics of the event (ignored!)
	 */
	@Override
	public void actionPerformed(ActionEvent ae) {
		handle();
	}

	private Number getNumberValue() {
		try {
			return (Number)getValue();
		} catch (Exception e) {
			return (Number)new Double(0.0);
		}
	}

	private void displayError(Number prev) {
		String type = "A floating point number";
		
		if (getType().equals(Integer.class) || 
				getType().equals(Long.class) ||
				getType().equals(int.class) ||
				getType().equals(long.class))
			type = "An integer value";
		
		textField.setBackground(Color.RED);
		JOptionPane.showMessageDialog(
				null,
				type+" was expected. Value will be set to previous value: " + prev, 
				"Error",
				JOptionPane.ERROR_MESSAGE
		);
		textField.setText(format.format(prev));
		textField.setBackground(UIManager.getColor("TextField.background"));
	}
}
