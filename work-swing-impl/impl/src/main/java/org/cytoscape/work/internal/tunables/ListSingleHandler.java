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


import static org.cytoscape.work.internal.tunables.utils.GUIDefaults.updateFieldPanel;
import static org.cytoscape.work.internal.tunables.utils.GUIDefaults.setTooltip;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;

import org.cytoscape.work.Tunable;
import org.cytoscape.work.swing.AbstractGUITunableHandler;
import org.cytoscape.work.util.ListChangeListener;
import org.cytoscape.work.util.ListSelection;
import org.cytoscape.work.util.ListSingleSelection;


/**
 * Handler for the type <i>ListSingleSelection</i> of <code>Tunable</code>
 *
 * @author pasteur
 *
 * @param <T> type of items the List contains
 */
public class ListSingleHandler<T> extends AbstractGUITunableHandler
                                  implements ActionListener, ListChangeListener<T> {
	
	private JComboBox<T> combobox;

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
	public ListSingleHandler(Field f, Object o, Tunable t) {
		super(f, o, t);
		init();
	}

	public ListSingleHandler(final Method getter, final Method setter, final Object instance, final Tunable tunable) {
		super(getter, setter, instance, tunable);
		init();
	}

	@SuppressWarnings("unchecked")
	private ListSingleSelection<T> getSingleSelection() {
		try {
			return (ListSingleSelection<T>)getValue();
		} catch(final Exception e) {
			throw new NullPointerException("bad ListSingleSelection object");	
		}
	}

	@SuppressWarnings("unchecked")
	private void init() {
		//set Gui
		final JLabel label = new JLabel(getDescription());
		label.setVerticalTextPosition(SwingConstants.CENTER);

		//add list's items to the combobox
		combobox = new JComboBox<>((T[])getSingleSelection().getPossibleValues().toArray());
		combobox.addActionListener(this);
		combobox.getModel().setSelectedItem(getSingleSelection().getSelectedValue());
		
		updateFieldPanel(panel, label, combobox, horizontal);
		setTooltip(getTooltip(), label, combobox);
		
		combobox.setEnabled(combobox.getModel().getSize() > 1);
		panel.setVisible(combobox.getModel().getSize() > 0);
	}

	@Override
	@SuppressWarnings("unchecked")
	public void update() {
		if (combobox == null) return;
		combobox.setModel(new DefaultComboBoxModel<>((T[]) getSingleSelection().getPossibleValues().toArray()));
		combobox.setSelectedItem(getSingleSelection().getSelectedValue());
		combobox.setEnabled(combobox.getModel().getSize() > 1);
		panel.setVisible(combobox.getModel().getSize() > 0);
	}

	/**
	 * set the item that is currently selected in the ComboBox as the only possible item selected in <code>listSingleSelection</code>
	 */
	@Override
	@SuppressWarnings("unchecked")
	public void handle() {
		if (combobox == null)
			return;
		
		final T selectedItem = (T) combobox.getSelectedItem();
		
		if (selectedItem != null){
			ListSingleSelection<T> singleSelection = getSingleSelection();
			singleSelection.setSelectedValue(selectedItem);
			
			try {
				setValue(singleSelection);
			} catch (final Exception e) {
				combobox.setBackground(Color.RED);
				e.printStackTrace();
				JOptionPane.showMessageDialog(null, "The value entered cannot be set for " + getName() + "!", "Error", JOptionPane.ERROR_MESSAGE);
				combobox.setBackground(UIManager.getColor("ComboBox.background"));
				return;
			}
		}
	}

	/**
	 * To get the item that is currently selected
	 */
	@Override
	@SuppressWarnings("unchecked")
	public String getState() {
		if (combobox == null)
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

	@Override
	public void selectionChanged(ListSelection<T> source) {
		update();
	}

	@Override
	public void listChanged(ListSelection<T> source) {
		update();
	}
}
