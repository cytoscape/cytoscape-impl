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


import java.lang.reflect.*;
import javax.swing.*;

import org.cytoscape.work.Tunable;
import org.cytoscape.work.internal.tunables.utils.GUIDefaults;
import org.cytoscape.work.swing.AbstractGUITunableHandler;
import org.cytoscape.work.util.ListSingleSelection;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


/**
 * Handler for the type <i>ListSingleSelection</i> of <code>Tunable</code>
 *
 * @author pasteur
 *
 * @param <T> type of items the List contains
 */
public class ListSingleHandler<T> extends AbstractGUITunableHandler implements ActionListener {
	
	private static final Font COMBOBOX_FONT = new Font("SansSerif", Font.PLAIN, 12);
//	private static final Dimension DEF_LABEL_SIZE = new Dimension(300, 25);
	private static final Dimension DEF_COMBOBOX_SIZE = new Dimension(300, 25);
	
	private JComboBox combobox;

	/**
	 * Constructs the <code>GUIHandler</code> for the <code>ListSingleSelection</code> type
	 *
	 * creates a ComboBox to collect all the <code>T</code> items and displays it in the GUI
	 * Informations about the list and its contents are also displayed
	 *
	 * @param f field that has been annotated
	 * @param o object contained in <code>f</code>
	 * @param t tunable associated to <code>f</code>
	 */
	@SuppressWarnings("unchecked")
	public ListSingleHandler(Field f, Object o, Tunable t) {
		super(f, o, t);
		init();
	}

	public ListSingleHandler(final Method getter, final Method setter, final Object instance, final Tunable tunable) {
		super(getter, setter, instance, tunable);
		init();
	}

	private ListSingleSelection<T> getSingleSelection() {
		try {
			return (ListSingleSelection<T>)getValue();
		} catch(final Exception e) {
			throw new NullPointerException("bad ListSingleSelection object");	
		}
	}

	private void init() {

		if ( getSingleSelection().getPossibleValues().isEmpty() ) {
			panel = new JPanel();
			combobox = null;
			return;
		}

		//set Gui
		panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		final JLabel textArea = new JLabel(getDescription());
		textArea.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		textArea.setFont(GUIDefaults.LABEL_FONT);
		textArea.setVerticalTextPosition(SwingConstants.CENTER);
		panel.add(textArea);

		//add list's items to the combobox
		combobox = new JComboBox(getSingleSelection().getPossibleValues().toArray());
		combobox.setPreferredSize(DEF_COMBOBOX_SIZE);
		combobox.setFont(COMBOBOX_FONT);
		combobox.addActionListener(this);
		panel.add(combobox);
		
		combobox.getModel().setSelectedItem(getSingleSelection().getSelectedValue());

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

	public void update() {
		combobox.setModel(new DefaultComboBoxModel(getSingleSelection().getPossibleValues().toArray()));
		combobox.setSelectedItem(getSingleSelection().getSelectedValue());
	}

	/**
	 * set the item that is currently selected in the ComboBox as the only possible item selected in <code>listSingleSelection</code>
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void handle() {
		if ( combobox == null )
			return;
		
		final T selectedItem = (T) combobox.getSelectedItem();
		if (selectedItem != null){
			getSingleSelection().setSelectedValue(selectedItem);
			try {
				setValue(getSingleSelection());
				
			} catch (final Exception e) {
				combobox.setBackground(Color.red);
				e.printStackTrace();
				JOptionPane.showMessageDialog(null, "The value entered cannot be set for " + getName() + "!", "Error", JOptionPane.ERROR_MESSAGE);
				combobox.setBackground(Color.white);
				return;
			}
		}
	}

	/**
	 * To get the item that is currently selected
	 */
	public String getState() {
		if ( combobox == null )
			return "";

		final T selectedItem = (T)combobox.getSelectedItem();
		if (selectedItem == null)
			return "";

		getSingleSelection().setSelectedValue(selectedItem);
		return selectedItem.toString();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		handle();
	}
}
