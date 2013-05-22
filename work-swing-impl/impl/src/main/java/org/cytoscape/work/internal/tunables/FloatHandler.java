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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.*;
import java.text.DecimalFormat;

import javax.swing.*;

import org.cytoscape.work.Tunable;
import org.cytoscape.work.internal.tunables.utils.GUIDefaults;
import org.cytoscape.work.swing.AbstractGUITunableHandler;


public class FloatHandler extends AbstractGUITunableHandler implements ActionListener {
	private JFormattedTextField textField;
	private boolean horizontal = false;

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
		init();
	}

	public FloatHandler(final Method getter, final Method setter, final Object instance, final Tunable tunable) {
		super(getter, setter, instance, tunable);
		init();
	}

	private void init() {
		Float f = null;
		try {
			f = (Float)getValue();
		} catch(final Exception e) {
			e.printStackTrace();
			f = Float.valueOf(0.0f);
		}

		//setup GUI
		textField = new JFormattedTextField(new DecimalFormat());
		textField.setValue(f);
		textField.setPreferredSize(GUIDefaults.TEXT_BOX_DIMENSION);
		panel = new JPanel(new BorderLayout(GUIDefaults.hGap, GUIDefaults.vGap));
		JLabel label = new JLabel(getDescription());
		label.setFont(GUIDefaults.LABEL_FONT);
		label.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		textField.setHorizontalAlignment(JTextField.RIGHT);
		textField.addActionListener(this);

		if (horizontal) {
			panel.add(label, BorderLayout.NORTH);
			panel.add(textField, BorderLayout.SOUTH);
		} else {
			panel.add(label, BorderLayout.WEST);
			panel.add(textField, BorderLayout.EAST);
		}
	}

	public void update(){
		Float f;
		try{
			f= (Float) getValue();
			textField.setValue(f);
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	/**
	 * Catches the value inserted in the JTextField, parses it to a <code>Float</code> value, and tries to set it to the
	 * initial object. If it can't, throws an exception that displays the source error to the user
	 */
	public void handle() {
		textField.setBackground(Color.white);
		Float f;
		try {
			f = Float.parseFloat(textField.getText());
			try {
				setValue(f);
				
			} catch (final Exception e) {
				textField.setBackground(Color.red);
				JOptionPane.showMessageDialog(null, "The value entered cannot be set.", "Error", JOptionPane.ERROR_MESSAGE);
				e.printStackTrace();
				textField.setBackground(Color.white);
				return;
			}
		} catch(final Exception nfe) {
			textField.setBackground(Color.red);
			try{
				f = (Float)getValue();
			} catch(Exception e) {
				e.printStackTrace();
				f = Float.valueOf(0.0f);
			}
			JOptionPane.showMessageDialog(null, "A float was Expected\nValue will be set to the default: " + f,
			                             "Error", JOptionPane.ERROR_MESSAGE);
			try {
				textField.setText(f.toString());
				textField.setBackground(Color.white);
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * To get the item that is currently selected
	 */
	public String getState() {
		if ( textField == null )
			return "";
		try {
			final String text = textField.getText();
			if ( text == null )
				return "";
			return text;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return "";
		}
		
	}
	
	/**
	 *  Action listener event handler.
	 *
	 *  @param ae specifics of the event (ignored!)
	 */
	public void actionPerformed(ActionEvent ae) {
		handle();
	}
}
