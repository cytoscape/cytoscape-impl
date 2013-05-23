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


import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import javax.swing.BorderFactory;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.cytoscape.work.Tunable;
import org.cytoscape.work.internal.tunables.utils.GUIDefaults;
import org.cytoscape.work.swing.AbstractGUITunableHandler;


/**
 * Handler for the type <i>Integer</i> of <code>Tunable</code>
 *
 * @author pasteur
 */
public class IntegerHandler extends AbstractGUITunableHandler implements ActionListener {
	private JFormattedTextField textField;
	private boolean horizontal = false;

	/**
	 * Constructs the <code>GUIHandler</code> for the <code>Integer</code> type
	 *
	 * It creates the Swing component for this Object (JTextField) that contains the initial value of the Integer Object annotated as <code>Tunable</code>, its description, and displays it in a proper way
	 *
	 *
	 * @param f field that has been annotated
	 * @param o object contained in <code>f</code>
	 * @param t tunable associated to <code>f</code>
	 */
	public IntegerHandler(Field f, Object o, Tunable t) {
		super(f, o, t);
		init();
	}

	public IntegerHandler(final Method getter, final Method setter, final Object instance, final Tunable tunable) {
		super(getter, setter, instance, tunable);
		init();
	}

	private void init() {
		Integer i = null;
		try{
			i = (Integer)getValue();
		} catch(final Exception e) {
			e.printStackTrace();
			i = Integer.valueOf(0);
		}

		//set GUI
		panel = new JPanel(new BorderLayout(GUIDefaults.hGap, GUIDefaults.vGap));
		JLabel label = new JLabel(getDescription());
		label.setFont(GUIDefaults.LABEL_FONT);
		textField = new JFormattedTextField();
		textField.setPreferredSize(GUIDefaults.TEXT_BOX_DIMENSION);
		textField.setValue(i.toString());
		textField.setHorizontalAlignment(JTextField.RIGHT);
		textField.addActionListener(this);
		label.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		if (horizontal) {
			panel.add(label, BorderLayout.NORTH);
			panel.add(textField, BorderLayout.SOUTH);
		} else {
			panel.add(label, BorderLayout.WEST);
			panel.add(textField, BorderLayout.EAST);
		}
	}

	public void update(){
		Integer i = null;
		try{
			i = (Integer)getValue();
			textField.setValue(i);
		} catch(final Exception e) {
			e.printStackTrace();
		}
	}
	
	
	/**
	 * Catches the value inserted in the JTextField, parses it to a <code>Integer</code> value, and tries to set it to the initial object. If it can't, throws an exception that displays the source error to the user
	 */
	public void handle() {
		Integer newValue = null;
		try {
			textField.setBackground(Color.white);
			newValue = Integer.parseInt(textField.getText());
			
		} catch(final NumberFormatException nfe) {
			try {
				textField.setBackground(Color.red);
				newValue = (Integer)getValue();
				JOptionPane.showMessageDialog(null, "An Integer was expected!\nThe value will be set to the default: "
							      + newValue, "Error", JOptionPane.ERROR_MESSAGE);
				
				textField.setValue(getValue().toString());
				textField.setBackground(Color.white);
			} catch(final Exception e) {
				e.printStackTrace();
			}
		}
		try {
			setValue(newValue);
			
		} catch (final Exception e) {
			textField.setBackground(Color.red);
			JOptionPane.showMessageDialog(null, "The value entered cannot be set.", "Error", JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
			textField.setBackground(Color.white);
			return;
		}
	}


	/**
	 * To get the item that is currently selected
	 */
	public String getState() {
		if ( textField == null )
			return "";

		final String text = textField.getText();
		if ( text == null )
			return "";

		try {
			return text;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return "";
		}
		
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {

		handle();
	}
}
