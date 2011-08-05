package org.cytoscape.work.internal.tunables;


import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.lang.reflect.*;
import javax.swing.*;

import org.cytoscape.work.Tunable;
import org.cytoscape.work.swing.AbstractGUITunableHandler;


public class FloatHandler extends AbstractGUITunableHandler {
	private JTextField textField;
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
		textField = new JTextField(f.toString(), 10);
		panel = new JPanel(new BorderLayout());
		JLabel label = new JLabel(getDescription());
		label.setFont(new Font(null, Font.PLAIN, 12));
		textField.setHorizontalAlignment(JTextField.RIGHT);

		if (horizontal) {
			panel.add(label, BorderLayout.NORTH);
			panel.add(textField, BorderLayout.SOUTH);
		} else {
			panel.add(label, BorderLayout.WEST);
			panel.add(textField, BorderLayout.EAST);
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
			setValue(f);
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
}
