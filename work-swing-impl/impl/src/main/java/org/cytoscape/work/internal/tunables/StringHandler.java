package org.cytoscape.work.internal.tunables;

import java.awt.BorderLayout;
import java.awt.Font;
import java.lang.reflect.*;
import javax.swing.*;

import org.cytoscape.work.Tunable;
import org.cytoscape.work.swing.AbstractGUITunableHandler;


/**
 * Handler for the type <i>String</i> of <code>Tunable</code>
 *
 * @author pasteur
 */
public class StringHandler extends AbstractGUITunableHandler {
	private JTextField textField;
	private boolean horizontal = false;

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
		textField = new JTextField(s, 15);
		panel = new JPanel(new BorderLayout());
		JLabel label = new JLabel(getDescription());
		label.setFont(new Font(null, Font.PLAIN,12));
		textField.setHorizontalAlignment(JTextField.RIGHT);

		if (horizontal) {
			panel.add(label, BorderLayout.NORTH);
			panel.add(textField, BorderLayout.SOUTH);
		} else {
			panel.add(label, BorderLayout.WEST );
			panel.add(textField, BorderLayout.EAST);
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
}
