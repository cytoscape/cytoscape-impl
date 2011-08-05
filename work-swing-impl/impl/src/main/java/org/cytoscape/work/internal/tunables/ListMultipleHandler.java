package org.cytoscape.work.internal.tunables;


import java.awt.BorderLayout;
import java.awt.Font;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;

import org.cytoscape.work.Tunable;
import org.cytoscape.work.swing.AbstractGUITunableHandler;
import org.cytoscape.work.util.ListMultipleSelection;


/**
 * Handler for the type <i>ListMultipleSelection</i> of <code>Tunable</code>
 *
 * @author pasteur
 *
 * @param <T> type of items the List contains
 */
public class ListMultipleHandler<T> extends AbstractGUITunableHandler {
	private JList itemsContainerList;
	private List<T> selectedItems;

	/**
	 * Constructs the <code>GUIHandler</code> for the <code>ListMultipleSelection</code> type
	 *
	 * creates a list to collect all the <code>T</code> items and displays it in the GUI through a JScrollPane
	 * Informations about the list and its contents are also displayed
	 *
	 * @param f field that has been annotated
	 * @param o object contained in <code>f</code>
	 * @param t tunable associated to <code>f</code>
	 */
	@SuppressWarnings("unchecked")
	public ListMultipleHandler(Field f, Object o, Tunable t) {
		super(f, o, t);
		init();
	}

	public ListMultipleHandler(final Method getter, final Method setter, final Object instance, final Tunable tunable) {
		super(getter, setter, instance, tunable);
		init();
	}

	private void init() {
		//create GUI
		panel = new JPanel(new BorderLayout());
		JTextArea jta = new JTextArea(getDescription());
		jta.setLineWrap(true);
		jta.setWrapStyleWord(true);
		panel.add(jta,BorderLayout.BEFORE_LINE_BEGINS);
		jta.setBackground(null);
		jta.setEditable(false);

		//put the items in a list
		itemsContainerList = new JList(getList().getPossibleValues().toArray());
		itemsContainerList.setFont(new Font("sansserif",Font.PLAIN,11));
		itemsContainerList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		itemsContainerList.addListSelectionListener(this);

		//use a JscrollPane to visualize the items
		JScrollPane scrollpane = new JScrollPane(itemsContainerList);
		panel.add(scrollpane,BorderLayout.EAST);
	}

	private ListMultipleSelection<T> getList() {
		try {
			return (ListMultipleSelection<T>)getValue();
		} catch (final Exception e) {
			e.printStackTrace();
			return new ListMultipleSelection<T>();
		}
	}

	/**
	 * set the items that are currently selected in the <code>itemsContainerList</code> as the selected items in <code>listMultipleSelection</code>
	 */
	public void handle() {
		selectedItems = convertToList(itemsContainerList.getSelectedValues());
		if (!selectedItems.isEmpty()) {
			final ListMultipleSelection<T> listMultipleSelection = getList();
			listMultipleSelection.setSelectedValues(selectedItems);
		}
	}

	//converts the array that contains the selected items into a List to be able to set them in ListMultipleSelection object
	@SuppressWarnings("unchecked")
		private List<T> convertToList(Object[] array) {
		List<T> list = new ArrayList<T>();

		for(int i = 0; i < array.length; i++)
			list.add(i, (T)array[i]);
		return list;
	}

	/**
	 * returns a string representation of all the selected items of <code>listMultipleSelection</code>
	 */
	public String getState() {
		final ListMultipleSelection<T> listMultipleSelection = getList();
		final List<T> selection = listMultipleSelection.getSelectedValues();
		return selection == null ? "" : selection.toString();
	}
}
