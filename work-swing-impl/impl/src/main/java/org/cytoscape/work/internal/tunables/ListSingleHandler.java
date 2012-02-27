package org.cytoscape.work.internal.tunables;


import java.lang.reflect.*;
import javax.swing.*;

import org.cytoscape.work.Tunable;
import org.cytoscape.work.swing.AbstractGUITunableHandler;
import org.cytoscape.work.util.ListSingleSelection;

import java.awt.*;


/**
 * Handler for the type <i>ListSingleSelection</i> of <code>Tunable</code>
 *
 * @author pasteur
 *
 * @param <T> type of items the List contains
 */
public class ListSingleHandler<T> extends AbstractGUITunableHandler {
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
		panel = new JPanel(new BorderLayout());
		JTextArea textArea = new JTextArea(getDescription());
		textArea.setLineWrap(true);
		textArea.setWrapStyleWord(true);
		panel.add(textArea,BorderLayout.WEST);
		textArea.setBackground(null);
		textArea.setEditable(false);

		//add list's items to the combobox
		combobox = new JComboBox(getSingleSelection().getPossibleValues().toArray());
		combobox.setFont(new Font("sansserif", Font.PLAIN, 11));
		combobox.addActionListener(this);
		panel.add(combobox, BorderLayout.EAST);
		
		combobox.getModel().setSelectedItem(getSingleSelection().getSelectedValue());
	}

	public void update() {
		combobox.removeAllItems();
		for ( T value : getSingleSelection().getPossibleValues() ) 
			combobox.addItem(value);
	}

	/**
	 * set the item that is currently selected in the ComboBox as the only possible item selected in <code>listSingleSelection</code>
	 */
	@SuppressWarnings("unchecked")
	public void handle() {
		if ( combobox == null )
			return;
		
		final T selectedItem = (T)combobox.getSelectedItem();
		if (selectedItem != null) {
			getSingleSelection().setSelectedValue(selectedItem);
			try {
			setValue(null);
			} catch (Exception e) { e.printStackTrace(); }
		}
	}

	/**
	 * To get the item that is currently selected
	 */
	public String getState() {
		if ( combobox == null )
			return "";

		final T selectedItem = (T)combobox.getSelectedItem();
		if ( selectedItem == null )
			return "";

		getSingleSelection().setSelectedValue(selectedItem);
		return selectedItem.toString();
	}
}
