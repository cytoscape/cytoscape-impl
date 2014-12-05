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
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.ToolTipManager;

import org.cytoscape.work.Tunable;
import org.cytoscape.work.internal.tunables.utils.GUIDefaults;
import org.cytoscape.work.swing.AbstractGUITunableHandler;
import org.cytoscape.work.swing.DirectlyPresentableTunableHandler;


/**
 * Handler for the type <i>Boolean</i> of <code>Tunable</code>
 *
 * @author pasteur
 */
public class BooleanHandler extends AbstractGUITunableHandler implements ActionListener, DirectlyPresentableTunableHandler{
	private JCheckBox checkBox;
	private boolean horizontal = false;
	private JOptionPane optionPane;
	private boolean useOptionPane = false;
	private int selectedOption;
	
	/**
	 * Constructs the <code>GUIHandler</code> for the <code>Boolean</code> type
	 *
	 * It creates the Swing component for this Object (JCheckBox) with its description/initial state,  and displays it
	 *
	 * @param f field that has been annotated
	 * @param o object contained in <code>f</code>
	 * @param t tunable associated to <code>f</code>
	 */
	public BooleanHandler(Field f, Object o, Tunable t) {
		super(f, o, t);
		init();
	}

	public BooleanHandler(final Method getter, final Method setter, final Object instance, final Tunable tunable) {
		super(getter, setter, instance, tunable);
		init();
	}

	private void init() {
		//setup GUI
		panel = new JPanel(new BorderLayout(GUIDefaults.H_GAP, GUIDefaults.V_GAP));
		checkBox = new JCheckBox();
		checkBox.setSelected(getBoolean());
		JLabel label = new JLabel(getDescription());
		label.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		checkBox.addActionListener(this);

		if (horizontal) {
			panel.add(label, BorderLayout.NORTH);
			panel.add(checkBox, BorderLayout.SOUTH);
		} else {
			panel.add(label, BorderLayout.WEST);
			panel.add(checkBox, BorderLayout.EAST);
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

	private boolean getBoolean() {
		try {
			return (Boolean)getValue();
		} catch (final Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	
	public void update(){
		boolean b;
		try{
			b = (Boolean) getValue();
			checkBox.setSelected(b);
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	
	@Override
	public boolean isForcedToSetDirectly() {
		return getParams().getProperty("ForceSetDirectly", "false").equalsIgnoreCase("true");
	}

	@Override
	public boolean setTunableDirectly(Window possibleParent) {
		selectedOption = setOptionPaneGUI(possibleParent);
		useOptionPane = true;
		handle();
		useOptionPane = false;
		return selectedOption != JOptionPane.CANCEL_OPTION;
	}

	@SuppressWarnings("static-access")
	private int setOptionPaneGUI(Window possibleParent) {
		
		//optionPane = new JOptionPane(getDescription());
		//optionPane.setOptionType(JOptionPane.YES_NO_OPTION);
		return  optionPane.showOptionDialog(possibleParent, getDescription(), getParams().getProperty("ForceSetTitle", " "), JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null, null, null);
	}

	/**
	 * To set the current value represented in the <code>GUIHandler</code> (in a <code>JCheckBox</code>)to the value of this <code>Boolean</code> object
	 */
	public void handle() {
		try {
			final Boolean setting;
			if (useOptionPane)
				setting = selectedOption == JOptionPane.YES_OPTION ? true : false;
			else
				setting = checkBox.isSelected();
			setValue(setting);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * To get the state of the value of the <code>BooleanHandler</code> : <code>true</code> or <code>false</code>
	 */
	public String getState() {
		return String.valueOf(checkBox.isSelected());
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		handle();		
	}
}
