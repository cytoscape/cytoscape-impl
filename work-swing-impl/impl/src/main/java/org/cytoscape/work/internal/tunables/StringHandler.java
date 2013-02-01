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
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import javax.swing.BorderFactory;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.text.DefaultFormatter;

import org.cytoscape.work.Tunable;
import org.cytoscape.work.internal.tunables.utils.GUIDefaults;
import org.cytoscape.work.swing.AbstractGUITunableHandler;


/**
 * Handler for the type <i>String</i> of <code>Tunable</code>
 *
 * @author pasteur
 */
public class StringHandler extends AbstractGUITunableHandler implements ActionListener {
	
	private static final Font TEXT_FONT = new Font("SansSerif", Font.PLAIN,12);
	private static final Font LABEL_FONT = new Font("SansSerif", Font.BOLD ,13);
	
	private JFormattedTextField textField;

	/**
	 * Constructs the <code>GUIHandler</code> for the <code>String</code> type
	 *
	 * It creates the Swing component for this Object (JTextField) that contains the initial string, adds its description, and displays it in a proper way
	 *
	 *
	 * @param f field that has been annotated
	 * @param o object contained in <code>f</code>
	 * @param t tunable associated to <code>f</code>
	 */
	public StringHandler(Field f, Object o, Tunable t) {
		super(f, o, t);
		init();
	}

	public StringHandler(final Method getter, final Method setter, final Object instance, final Tunable tunable) {
		super(getter, setter, instance, tunable);
		init();
	}

	private void init() {
		String s = null;
		try {
			s = (String)getValue();
		} catch (final Exception e) {
			e.printStackTrace();
			s = "";
		}

		//set Gui
		final DefaultFormatter formatter = new DefaultFormatter();
		formatter.setOverwriteMode(false);
		textField = new JFormattedTextField(formatter);
		textField.setValue(s);
		textField.setPreferredSize(GUIDefaults.TEXT_BOX_DIMENSION);
		panel = new JPanel(new BorderLayout(GUIDefaults.hGap, GUIDefaults.vGap));
		final JLabel label = new JLabel(getDescription());
		label.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));
		label.setFont(LABEL_FONT);
		label.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		textField.setFont(TEXT_FONT);
		
		textField.setHorizontalAlignment(JTextField.LEFT);
		textField.addActionListener(this);
		textField.setPreferredSize(new Dimension(200, 25));

		if (horizontal) {
			panel.add(label, BorderLayout.NORTH);
			panel.add(textField, BorderLayout.SOUTH);
		} else {
			panel.add(label, BorderLayout.WEST );
			panel.add(textField, BorderLayout.EAST);
		}
	}
	
	public void update(){
		String s = null;
		try {
			s = (String)getValue();
			textField.setValue(s);
		} catch (final Exception e) {
			e.printStackTrace();
		}
	}
	

	/**
	 * Catches the value inserted in the JTextField, and tries to set it to the initial object. If it can't, throws an
	 * exception that displays the source error to the user
	 */
	public void handle() {
		final String string = textField.getText();
		try {
			if (string != null)
				setValue(string);
		} catch (final Exception e) {
			e.printStackTrace();
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
