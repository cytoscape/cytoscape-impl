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

import static org.cytoscape.work.internal.tunables.utils.GUIDefaults.TEXT_BOX_WIDTH;
import static org.cytoscape.work.internal.tunables.utils.GUIDefaults.setTooltip;
import static org.cytoscape.work.internal.tunables.utils.GUIDefaults.updateFieldPanel;

import java.awt.Dimension;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.text.DefaultFormatter;

import org.cytoscape.work.Tunable;
import org.cytoscape.work.swing.AbstractGUITunableHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Handler for the type <i>String</i> of <code>Tunable</code>
 *
 * @author pasteur
 */
public class StringHandler extends AbstractGUITunableHandler implements FocusListener {
	
	private static final Logger logger = LoggerFactory.getLogger(StringHandler.class);
	
	private JFormattedTextField textField;
	private boolean readOnly = false;
	private boolean isUpdating = false;

	/**
	 * It creates the Swing component for this Object (JTextField) that contains the initial string,
	 * adds its description, and displays it in a proper way.
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
		readOnly = getParams().getProperty("readOnly", "false").equalsIgnoreCase("true");
		String s = null;
		
		try {
			s = (String)getValue();
		} catch (final Exception e) {
			logger.error("Could not initialize String Tunable.", e);
			s = "";
		}

		final DefaultFormatter formatter = new DefaultFormatter();
		formatter.setOverwriteMode(false);
		textField = new JFormattedTextField(formatter);
		textField.setValue(s);
		textField.setPreferredSize(new Dimension(2 * TEXT_BOX_WIDTH, textField.getPreferredSize().height));
		textField.setHorizontalAlignment(JTextField.LEFT);
		textField.addFocusListener(this);

		final JLabel label = new JLabel(getDescription());
		
		updateFieldPanel(panel, label, textField, horizontal);
		setTooltip(getTooltip(), label, textField);
		if (readOnly)
			textField.setEditable(false);
	}
	
	@Override
	public void update(){
		isUpdating = true;
		String s = null;
		try {
			s = (String)getValue();
			textField.setValue(s);
		} catch (final Exception e) {
			logger.error("Could not set String Tunable.", e);
		}
		isUpdating = false;
	}
	
	/**
	 * Catches the value inserted in the JTextField, and tries to set it to the initial object. If it can't, throws an
	 * exception that displays the source error to the user
	 */
	@Override
	public void handle() {
		if(isUpdating)
			return;
		
		final String string = textField.getText();
		try {
			if (string != null)
				setValue(string);
		} catch (final Exception e) {
			logger.error("Could not set String Tunable.", e);
		}
	}
	
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

		try {
			return text;
		} catch (Exception e) {
			logger.warn("Could not set String Tunable.", e);
			return "";
		}
		
	}

	@Override
	public void focusGained(FocusEvent e) {
	}

	@Override
	public void focusLost(FocusEvent e) {
		handle();
	}
}
