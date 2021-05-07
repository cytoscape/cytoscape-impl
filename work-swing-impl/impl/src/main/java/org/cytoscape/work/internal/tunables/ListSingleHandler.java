package org.cytoscape.work.internal.tunables;

import static org.cytoscape.work.internal.tunables.utils.GUIDefaults.setTooltip;
import static org.cytoscape.work.internal.tunables.utils.GUIDefaults.updateFieldPanel;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.SwingConstants;
import javax.swing.UIManager;

import org.cytoscape.util.swing.LookAndFeelUtil;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.swing.AbstractGUITunableHandler;
import org.cytoscape.work.util.ListChangeListener;
import org.cytoscape.work.util.ListSelection;
import org.cytoscape.work.util.ListSingleSelection;

/*
 * #%L
 * Cytoscape Work Swing Impl (work-swing-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2021 The Cytoscape Consortium
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
	private final JLabel label = new JLabel(getDescription());
	private boolean isUpdating;
	private boolean decorated = false;
	private List<T> initialValues;

	/**
	 * Constructs the <code>GUIHandler</code> for the <code>ListSingleSelection</code> type.
	 * Creates a ComboBox to collect all the <code>T</code> items and displays it in the GUI.
	 * Informations about the list and its contents are also displayed
	 *
	 * @param field field that has been annotated
	 * @param instance An object instance that contains the <code>field</code>
	 * @param tunable tunable associated to <code>field</code>
	 */
	public ListSingleHandler(final Field field, final Object instance, final Tunable tunable) {
		super(field, instance, tunable);
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
			throw new IllegalStateException("bad ListSingleSelection object", e);	
		}
	}

	@SuppressWarnings("unchecked")
	private void init() {
		//set Gui
		label.setVerticalTextPosition(SwingConstants.CENTER);

		T[] values;
		T selectedValue = null;

		if (getSingleSelection() == null || getSingleSelection().getPossibleValues() == null) {
			values = (T[])new Object[1];
		} else {
			initialValues = new ArrayList<T>(getSingleSelection().getPossibleValues());
			values = (T[])getSingleSelection().getPossibleValues().toArray();
			selectedValue = getSingleSelection().getSelectedValue();
		}

		//add list's items to the combobox
		combobox = new JComboBox<>(values);
		combobox.getModel().setSelectedItem(selectedValue);
		combobox.addActionListener(this);

		String decorate = getParams().getProperty("lookup");

		if (decorate != null) {
			decorated = true;
			// JComboBoxDecorator<T> decorator = new JComboBoxDecorator();
			if (decorate.equalsIgnoreCase("begins")) {
				JComboBoxDecorator.decorate(combobox, Arrays.asList(values), true);
			} else if (decorate.equalsIgnoreCase("contains")) {
				JComboBoxDecorator.decorate(combobox, Arrays.asList(values), false);
			}
		}

		updateFieldPanel(panel, label, combobox, horizontal);
		setTooltip(getTooltip(), label, combobox);

		combobox.setEnabled(combobox.getModel().getSize() > 1);
		label.setEnabled(combobox.isEnabled());
		panel.setVisible(combobox.getModel().getSize() > 0);

		final ListSingleSelection<T> singleSelection = getSingleSelection();

		if (singleSelection != null) {
			// Make sure we're the only handler for this Tunable that's listening
			// for changes.  If we're in the middle of a refresh, we can sometimes
			// be in a state where there are two...
			ListChangeListener<T> found = null;
			for (ListChangeListener<T> listener: singleSelection.getListeners()) {
				if (listener instanceof AbstractGUITunableHandler &&
						((AbstractGUITunableHandler)listener).getQualifiedName().equals(this.getQualifiedName())) {
					found = listener;
				}
			}
			if (found != null) {
				singleSelection.removeListener(found);
			}
			singleSelection.addListener(this);
		}
	}

	@Override
	public void update() {
		isUpdating = true;
		if (combobox == null) return;
		// combobox.setModel(new DefaultComboBoxModel<T>((T[])getSingleSelection().getPossibleValues().toArray()));
		combobox.setSelectedItem(getSingleSelection().getSelectedValue());
		if (!decorated) {
			combobox.setEnabled(combobox.getModel().getSize() > 1);
			label.setEnabled(combobox.isEnabled());
			panel.setVisible(combobox.getModel().getSize() > 0);
		} else {
			// We can't use the same rules since we might now have a single value after
			// filtering
			combobox.setEnabled(combobox.getModel().getSize() >= 1);
			label.setEnabled(combobox.isEnabled());
			panel.setVisible(combobox.getModel().getSize() > 0);
		}
		isUpdating = false;
	}

	/**
	 * set the item that is currently selected in the ComboBox as the only possible item selected in <code>listSingleSelection</code>
	 */
	@Override
	@SuppressWarnings("unchecked")
	public void handle() {
		if (combobox == null || isUpdating)
			return;
		
		final T selectedItem = (T) combobox.getSelectedItem();
		
		if (selectedItem != null) {
			ListSingleSelection<T> singleSelection = getSingleSelection();

			// If we're decorating, don't set the selected value if it's not
			// one of the options
			if (decorated) {
				if (initialValues.contains(selectedItem))
					singleSelection.setSelectedValue(selectedItem);
			} else {
				singleSelection.setSelectedValue(selectedItem);
			}
			
			try {
				// TODO This is wrong! It should not set the same list of values again,
				//      because only the selected value has changed. It can create infinite loops!
				setValue(singleSelection);
			} catch (final Exception e) {
				combobox.setForeground(LookAndFeelUtil.getErrorColor());
				e.printStackTrace();
				JOptionPane.showMessageDialog(null, "The value entered cannot be set for " + getName() + "!", "Error", JOptionPane.ERROR_MESSAGE);
				combobox.setForeground(UIManager.getColor("ComboBox.foreground"));
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

		if (!decorated || initialValues.contains(selectedItem))
			getSingleSelection().setSelectedValue(selectedItem);
		// return selectedItem.toString();
		return getSingleSelection().getSelectedValue().toString();
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
