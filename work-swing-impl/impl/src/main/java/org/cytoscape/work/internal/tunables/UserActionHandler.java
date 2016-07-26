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


import static org.cytoscape.work.internal.tunables.utils.GUIDefaults.setTooltip;
import static org.cytoscape.work.internal.tunables.utils.GUIDefaults.updateFieldPanel;

import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.cytoscape.work.Tunable;
import org.cytoscape.work.swing.util.UserAction;
import org.cytoscape.work.swing.AbstractGUITunableHandler;


/**
 * Handler for the type <i>Boolean</i> of <code>Tunable</code>
 * @author pasteur
 */
public class UserActionHandler extends AbstractGUITunableHandler {
	
	private static final Logger logger = LoggerFactory.getLogger(UserActionHandler.class);
	private JButton button;
	
	/**
	 * Constructs the <code>GUIHandler</code> for the <code>UserAction</code> type
	 *
	 * It creates the Swing component for this Object (JButton) with its description/initial state,  and displays it
	 *
	 * @param f field that has been annotated
	 * @param o object contained in <code>f</code>
	 * @param t tunable associated to <code>f</code>
	 */
	public UserActionHandler(Field f, Object o, Tunable t) {
		super(f, o, t);
		init();
	}

	public UserActionHandler(final Method getter, final Method setter, final Object instance, final Tunable tunable) {
		super(getter, setter, instance, tunable);
		init();
	}

	private void init() {
		// setup GUI
		button = new JButton(getDescription());
		try {
			UserAction uAction = (UserAction)getValue();
			if (uAction.getActionListener() != null)
				button.addActionListener(uAction.getActionListener());

			button.setEnabled(uAction.getEnabled());
		} catch (Exception e) {
			logger.error("Can't initialize UserAction tunable: ",e);
		}
		
		updateFieldPanel(panel, button, horizontal);
		setTooltip(getTooltip(), button);
	}

	@Override
	public void update(){
		try {
			UserAction uAction = (UserAction)getValue();
			button.setEnabled(uAction.getEnabled());
		} catch(Exception e){
			e.printStackTrace();
		}
	}
	

	/**
	 * To set the current value represented in the <code>GUIHandler</code> (in a <code>JCheckBox</code>)
	 * to the value of this <code>Boolean</code> object
	 */
	@Override
	public void handle() {
	}

	/**
	 * To get the state of the value of the <code>UserActionHandler</code> : <code>true</code> or <code>false</code>
	 */
	@Override
	public String getState() {
		return "";
	}

}
