package org.cytoscape.work.internal.tunables;


import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.*;
import java.text.DecimalFormat;

import javax.swing.*;

import org.cytoscape.work.Tunable;
import org.cytoscape.work.internal.tunables.utils.GUIDefaults;
import org.cytoscape.work.swing.AbstractGUITunableHandler;


/**
 * Handler for the type <i>Long</i> of <code>Tunable</code>
 *
 * @author pasteur
 */
public class LongHandler extends AbstractGUITunableHandler implements ActionListener {
	private JFormattedTextField textField;
	private boolean horizontal = false;

	/**
	 * Constructs the <code>GUIHandler</code> for the <code>Long</code> type
	 *
	 * It creates the Swing component for this Object (JTextField) that contains the initial value of the Long Object
	 * annotated as <code>Tunable</code>, its description, and displays it in a proper way
	 *
	 * @param f field that has been annotated
	 * @param o object contained in <code>f</code>
	 * @param t tunable associated to <code>f</code>
	 */
	public LongHandler(Field f, Object o, Tunable t) {
		super(f, o, t);
		init();
	}

	public LongHandler(final Method getter, final Method setter, final Object instance, final Tunable tunable) {
		super(getter, setter, instance, tunable);
		init();
	}

	private void init() {
		//setup GUI
		textField = new JFormattedTextField(new DecimalFormat());
		textField.setPreferredSize(GUIDefaults.TEXT_BOX_DIMENSION);
		textField.setValue(getLong());
		panel = new JPanel(new BorderLayout(GUIDefaults.hGap, GUIDefaults.vGap));
		JLabel label = new JLabel(getDescription());
		label.setFont(new Font(null, Font.PLAIN,12));
		label.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		textField.setHorizontalAlignment(JTextField.RIGHT);
		textField.addActionListener(this);

		if (horizontal) {
			panel.add(label, BorderLayout.NORTH);
			panel.add(textField, BorderLayout.SOUTH);
		} else {
			panel.add(label, BorderLayout.WEST );
			panel.add(textField, BorderLayout.EAST);
		}
	}

	private Long getLong() {
		try {
			return (Long)getValue();
		} catch (final Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public void update(){
		Long l = null;
		try{
			l = getLong();
			textField.setValue(l);
		} catch(final Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Catches the value inserted in the JTextField, parses it to a <code>Long</code> value, and tries
	 * to set it to the initial object. If it can't, throws an exception that displays the source error to the user
	 */
	public void handle() {
		textField.setBackground(Color.white);
		Long l = null;
		try {
			l = Long.valueOf(textField.getText());
		} catch(final Exception nfe) {
			textField.setBackground(Color.red);
			try {
				l = getLong();
			}catch(Exception e){e.printStackTrace();}
			JOptionPane.showMessageDialog(null, "A long was expected!\nValue will be set to default: "+ l,
			                              "Error", JOptionPane.ERROR_MESSAGE);
			try {
				textField.setText(getLong().toString());
				textField.setBackground(Color.white);
			} catch(final Exception e) {
				e.printStackTrace();
			}
		}
		try {
			setValue(l);
			
		} catch (final Exception e) {
			textField.setBackground(Color.red);
			JOptionPane.showMessageDialog(null, "The value entered cannot be set!", "Error", JOptionPane.ERROR_MESSAGE);
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
