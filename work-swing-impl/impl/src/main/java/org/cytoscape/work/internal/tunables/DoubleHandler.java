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
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.lang.reflect.*;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.Properties;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;

import org.cytoscape.work.Tunable;
import org.cytoscape.work.internal.tunables.utils.GUIDefaults;
import org.cytoscape.work.swing.AbstractGUITunableHandler;


/**
 * Handler for the type <i>Double</i> of <code>Tunable</code>
 *
 * @author pasteur
 */
public class DoubleHandler extends AbstractGUITunableHandler implements ActionListener {
	private JFormattedTextField textField;
	private String newline = System.getProperty("line.separator");

	/**
	 * Constructs the <code>GUIHandler</code> for the <code>Double</code> type
	 *
	 * It creates the Swing component for this Object (JTextField) that contains the initial value of the Double Object annotated as <code>Tunable</code>, its description, and displays it in a proper way
	 *
	 *
	 * @param f field that has been annotated
	 * @param o object contained in <code>f</code>
	 * @param t tunable associated to <code>f</code>
	 */
	public DoubleHandler(Field f, Object o, Tunable t) {
		super(f,o,t);
		init();
	}

	public DoubleHandler(final Method getter, final Method setter, final Object instance, final Tunable tunable) {
		super(getter, setter, instance, tunable);
		init();
	}

	private void init() {
		Double d;
		try {
			d = (Double)getValue();
		} catch(final Exception e) {
			e.printStackTrace();
			d = Double.valueOf(0.0);
		}

		//set Gui
		textField = new JFormattedTextField(d.toString());
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

		// Set the tooltip.  Note that at this point, we're setting
		// the tooltip on the entire panel.  This may or may not be
		// the right thing to do.
		if (getTooltip() != null && getTooltip().length() > 0) {
			final ToolTipManager tipManager = ToolTipManager.sharedInstance();
			tipManager.setInitialDelay(1);
			tipManager.setDismissDelay(7500);
			panel.setToolTipText(getTooltip());
		}
	}

	public void update(){
		Double d;
		try {
			d = (Double)getValue();
			textField.setValue(d);

		} catch(final Exception e) {
			e.printStackTrace();
		}
	}
	
	
	/**
	 * Catches the value inserted in the JTextField, parses it to a <code>Double</code> value, and tries to set it to the initial object. If it can't, throws an exception that displays the source error to the user
	 */
	public void handle() {
		textField.setBackground(Color.white);
		Double d;
		try{
			d = Double.parseDouble(textField.getText());
			try {
				setValue(d);
			} catch (final Exception e) {
				textField.setBackground(Color.red);
				JOptionPane.showMessageDialog(null, "The value entered cannot be set.", "Error", JOptionPane.ERROR_MESSAGE);
				e.printStackTrace();
				textField.setBackground(Color.white);
				return;
			}
		} catch(NumberFormatException nfe) {
			textField.setBackground(Color.red);
			try {
				d = (Double)getValue();
			} catch(final Exception e){
				e.printStackTrace();
				d = Double.valueOf(0.0);
			}
			JOptionPane.showMessageDialog(null,"A double was expected. Value will be set to default: " + d, "Error", JOptionPane.ERROR_MESSAGE);
			try {
				textField.setValue(getValue().toString());
				textField.setBackground(Color.white);
			} catch(final Exception e){
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

		final String text = textField.getText();
		if ( text == null )
			return "";

		try {
			 //d = Double.parseDouble(text);
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



