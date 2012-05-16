package org.cytoscape.work.internal.tunables;


import java.awt.BorderLayout;
import java.awt.Font;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;

import javax.swing.BorderFactory;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.cytoscape.work.Tunable;
import org.cytoscape.work.internal.tunables.utils.GUIDefaults;
import org.cytoscape.work.swing.AbstractGUITunableHandler;
import org.cytoscape.work.util.ListMultipleSelection;


/**
 * Handler for the type <i>ListMultipleSelection</i> of <code>Tunable</code>
 *
 * @author pasteur
 *
 * @param <T> type of items the List contains
 */
public class ListMultipleHandler<T> extends AbstractGUITunableHandler implements ListSelectionListener {
	private JList itemsContainerList;
	private ListMultipleSelection<T> listMultipleSelection;

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
		try {
			listMultipleSelection = (ListMultipleSelection<T>)getValue();
		} catch (Exception e) {
			e.printStackTrace();
		}

		//create GUI
		if ( listMultipleSelection.getPossibleValues().isEmpty() ) {
			panel = null;
			itemsContainerList = null;
			return;
		}

		panel = new JPanel(new BorderLayout(GUIDefaults.hGap, GUIDefaults.vGap));
		JTextArea jta = new JTextArea(getDescription());
		jta.setLineWrap(true);
		jta.setWrapStyleWord(true);
		jta.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		panel.add(jta,BorderLayout.BEFORE_LINE_BEGINS);
		jta.setBackground(null);
		jta.setEditable(false);

		//put the items in a list
		itemsContainerList = new JList(listMultipleSelection.getPossibleValues().toArray());
		itemsContainerList.setFont(new Font("sansserif",Font.PLAIN,11));
		itemsContainerList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		itemsContainerList.addListSelectionListener(this);

		//use a JscrollPane to visualize the items
		JScrollPane scrollpane = new JScrollPane(itemsContainerList);
		panel.add(scrollpane,BorderLayout.EAST);
	}

	public void update(){
		
	}
	
	/**
	 * set the items that are currently selected in the <code>itemsContainerList</code> as the selected items in <code>listMultipleSelection</code>
	 */
	public void handle() {
		if ( itemsContainerList == null )
			return;

		List selectedItems = Arrays.asList(itemsContainerList.getSelectedValues());
		if (!selectedItems.isEmpty()) {
			listMultipleSelection.setSelectedValues(selectedItems);
		}
		
		try {
			setValue(listMultipleSelection);
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * returns a string representation of all the selected items of <code>listMultipleSelection</code>
	 */
	public String getState() {
		if ( itemsContainerList == null )
			return "";

		final List<T> selection = listMultipleSelection.getSelectedValues();
		return selection == null ? "" : selection.toString();
	}

	@Override
	public void valueChanged(ListSelectionEvent e) {
		handle();
	}
}
